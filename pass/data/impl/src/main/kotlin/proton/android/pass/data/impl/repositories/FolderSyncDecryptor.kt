/*
 * Copyright (c) 2026 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.withContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.context.EncryptionTag
import proton.android.pass.crypto.api.usecases.EncryptedFolderData
import proton.android.pass.crypto.api.usecases.OpenFolder
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.db.entities.FolderEntity
import proton.android.pass.data.impl.db.entities.FolderKeyEntity
import proton.android.pass.data.impl.extensions.toEntity
import proton.android.pass.data.impl.local.LocalFolderDataSource
import proton.android.pass.data.impl.local.LocalFolderKeyDataSource
import proton.android.pass.data.impl.responses.FolderApiModel
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.key.ShareKey
import proton.android.pass.log.api.PassLogger
import proton_pass_folder_v1.FolderV1

internal class FolderSyncDecryptor(
    private val appDispatchers: AppDispatchers,
    private val localFolderDataSource: LocalFolderDataSource,
    private val localFolderKeyDataSource: LocalFolderKeyDataSource,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val openFolder: OpenFolder,
    private val database: PassDatabase
) {

    suspend fun decryptAndStoreFolders(
        userId: UserId,
        shareId: ShareId,
        folderApiModels: List<FolderApiModel>,
        getShareKeyForRotation: suspend (Long) -> ShareKey
    ) {
        if (folderApiModels.isEmpty()) return
        val deduplicated = deduplicateFolderApiModelsById(folderApiModels)
        logDuplicateFolderIds(shareId, deduplicated.duplicateFolderIds)

        val childrenMap = deduplicated.uniqueFolders.groupBy { it.parentFolderId }
        val resolvedKeys = resolveAllDecryptionKeys(userId, shareId, deduplicated.uniqueFolders, getShareKeyForRotation)
        try {
            if (resolvedKeys.isEmpty()) {
                PassLogger.w(TAG, "No decryption keys resolved for shareId=${shareId.id}, skipping folder sync")
                return
            }

            val context = FolderDecryptionContext(
                childrenMap = childrenMap,
                userId = userId,
                shareId = shareId,
                rootShareKeyByRotation = resolvedKeys.rootShareKeyByRotation
            )
            val traversalResult = decryptAllTrees(context, resolvedKeys.orphanParentKeyByFolderId)
            logTraversalOutcome(shareId, traversalResult)
            persistDecryptedFolders(shareId, traversalResult.folders)
        } finally {
            clearResolvedKeys(resolvedKeys)
        }
    }

    private suspend fun resolveAllDecryptionKeys(
        userId: UserId,
        shareId: ShareId,
        folders: List<FolderApiModel>,
        getShareKeyForRotation: suspend (Long) -> ShareKey
    ): FolderResolvedKeys {
        val rootKeys = resolveRootShareKeyByRotation(shareId, folders, getShareKeyForRotation)
        val orphanKeys = resolveOrphanParentKeyByFolderId(userId, shareId, folders)
        return FolderResolvedKeys(rootShareKeyByRotation = rootKeys, orphanParentKeyByFolderId = orphanKeys)
    }

    private suspend fun resolveRootShareKeyByRotation(
        shareId: ShareId,
        folders: List<FolderApiModel>,
        getShareKeyForRotation: suspend (Long) -> ShareKey
    ): Map<Long, EncryptionKey> {
        val rootRotations = folders.asSequence()
            .filter { it.parentFolderId == null }
            .map { it.keyRotation }
            .distinct()
            .toList()
        return encryptionContextProvider.withEncryptionContextSuspendable {
            rootRotations.mapNotNull { keyRotation ->
                safeRunCatching {
                    val shareKey = getShareKeyForRotation(keyRotation)
                    keyRotation to EncryptionKey(decrypt(shareKey.key))
                }.onFailure { error ->
                    PassLogger.w(TAG, "Cannot resolve share key for rotation=$keyRotation, shareId=${shareId.id}")
                    PassLogger.w(TAG, error)
                }.getOrNull()
            }.toMap()
        }
    }

    private suspend fun resolveOrphanParentKeyByFolderId(
        userId: UserId,
        shareId: ShareId,
        folders: List<FolderApiModel>
    ): Map<String, EncryptionKey> {
        val batchFolderIds = folders.mapTo(mutableSetOf()) { it.folderId }
        val orphanParentIds = folders.asSequence()
            .mapNotNull { it.parentFolderId }
            .filter { it !in batchFolderIds }
            .distinct()
            .toList()
        return encryptionContextProvider.withEncryptionContextSuspendable {
            orphanParentIds.mapNotNull { parentFolderId ->
                safeRunCatching {
                    val parentFolderKey = localFolderKeyDataSource.getByFolderId(
                        userId = userId,
                        shareId = shareId,
                        folderId = FolderId(parentFolderId)
                    ) ?: throw IllegalStateException("Parent folder key not found for parentId=$parentFolderId")
                    parentFolderId to EncryptionKey(decrypt(parentFolderKey.encryptedKey))
                }.onFailure { error ->
                    PassLogger.w(
                        TAG,
                        "Cannot resolve local parent key for parentId=$parentFolderId, shareId=${shareId.id}"
                    )
                    PassLogger.w(TAG, error)
                }.getOrNull()
            }.toMap()
        }
    }

    private suspend fun decryptAllTrees(
        context: FolderDecryptionContext,
        orphanParentKeyByFolderId: Map<String, EncryptionKey>
    ): FolderTraversalResult = withContext(appDispatchers.default) {
        val mergedResult = mutableTraversalResult()
        mergeTraversalResult(mergedResult, decryptFolderTreeIterative(null, null, context))

        for ((orphanParentId, orphanParentKey) in orphanParentKeyByFolderId) {
            try {
                val orphanResult = decryptFolderTreeIterative(orphanParentId, orphanParentKey, context)
                mergeTraversalResult(mergedResult, orphanResult)
            } finally {
                orphanParentKey.clear()
            }
        }

        mergedResult.toTraversalResult()
    }

    private suspend fun decryptFolderTreeIterative(
        parentFolderId: String?,
        parentKey: EncryptionKey?,
        context: FolderDecryptionContext
    ): FolderTraversalResult {
        val rootChildren = context.childrenMap[parentFolderId] ?: return emptyTraversalResult()
        val stack = ArrayDeque<FolderTraversalFrame>()
        val mutableResult = mutableTraversalResult()
        stack.addLast(FolderTraversalFrame(parentFolderId, parentKey, rootChildren, clearParentKeyOnExit = false))

        try {
            while (stack.isNotEmpty()) {
                val step = nextTraversalStep(stack) ?: continue
                if (mutableResult.processedFolders >= MAX_FOLDERS_PER_TRAVERSAL) {
                    mutableResult.circuitBreakerTriggered = true
                    break
                }
                mutableResult.processedFolders++
                decryptTraversalStep(step, context, mutableResult, stack)
            }
        } finally {
            clearRemainingStackKeys(stack)
        }

        return mutableResult.toTraversalResult()
    }

    private suspend fun decryptTraversalStep(
        step: TraversalStep,
        context: FolderDecryptionContext,
        mutableResult: TraversalAccumulator,
        stack: ArrayDeque<FolderTraversalFrame>
    ) {
        safeRunCatching {
            val decryptionKey = resolveDecryptionKey(step.parentFolderId, step.parentKey, step.apiModel, context)
            val decryptedPair = decryptFolderApiModel(step.apiModel, decryptionKey, context)
            mutableResult.folders.add(decryptedPair)

            val childKey = decryptChildFolderKey(step.apiModel, decryptionKey)
            val childChildren = context.childrenMap[step.apiModel.folderId].orEmpty()
            stack.addLast(
                FolderTraversalFrame(
                    parentFolderId = step.apiModel.folderId,
                    parentKey = childKey,
                    children = childChildren,
                    clearParentKeyOnExit = true
                )
            )
        }.onFailure { error ->
            mutableResult.failedFolderIds.add(step.apiModel.folderId)
            PassLogger.w(TAG, "Failed to decrypt folder ${step.apiModel.folderId}")
            PassLogger.w(TAG, error)
        }
    }

    private fun nextTraversalStep(stack: ArrayDeque<FolderTraversalFrame>): TraversalStep? {
        val frame = stack.last()
        if (frame.nextChildIndex >= frame.children.size) {
            if (frame.clearParentKeyOnExit) frame.parentKey?.clear()
            stack.removeLast()
            return null
        }

        stack.removeLast()
        stack.addLast(frame.copy(nextChildIndex = frame.nextChildIndex + 1))
        val apiModel = frame.children[frame.nextChildIndex]
        return TraversalStep(
            parentFolderId = frame.parentFolderId,
            parentKey = frame.parentKey,
            apiModel = apiModel
        )
    }

    private fun clearRemainingStackKeys(stack: ArrayDeque<FolderTraversalFrame>) {
        stack.forEach { frame ->
            if (frame.clearParentKeyOnExit) frame.parentKey?.clear()
        }
    }

    private fun resolveDecryptionKey(
        parentFolderId: String?,
        parentKey: EncryptionKey?,
        apiModel: FolderApiModel,
        context: FolderDecryptionContext
    ): EncryptionKey = if (parentFolderId == null) {
        context.rootShareKeyByRotation[apiModel.keyRotation]
            ?: throw IllegalStateException(
                "No ShareKey found for shareId=${context.shareId.id}, keyRotation=${apiModel.keyRotation}"
            )
    } else {
        parentKey ?: throw IllegalStateException("Missing parent key for folder ${apiModel.folderId}")
    }

    private suspend fun decryptFolderApiModel(
        apiModel: FolderApiModel,
        decryptionKey: EncryptionKey,
        context: FolderDecryptionContext
    ): Pair<FolderEntity, FolderKeyEntity> {
        val encryptedData = EncryptedFolderData(
            folderId = FolderId(apiModel.folderId),
            parentFolderId = apiModel.parentFolderId?.let(::FolderId),
            keyRotation = apiModel.keyRotation,
            contentFormatVersion = apiModel.contentFormatVersion,
            content = apiModel.content,
            folderKey = apiModel.folderKey
        )
        val openFolderKey = decryptionKey.clone()
        val openedFolder = try {
            openFolder.open(encryptedData, openFolderKey)
        } finally {
            openFolderKey.clear()
        }

        val folderProto = FolderV1.Folder.newBuilder().setName(openedFolder.folderName).build()
        val folderContentProto = encryptionContextProvider.withEncryptionContextSuspendable {
            encrypt(folderProto.toByteArray())
        }

        val folderEntity = apiModel.toEntity(context.userId, context.shareId.id, folderContentProto)
        val folderKeyEntity = FolderKeyEntity(
            folderId = apiModel.folderId,
            userId = context.userId.id,
            shareId = context.shareId.id,
            keyRotation = apiModel.keyRotation,
            encryptedKey = openedFolder.reencryptedFolderKey
        )
        return folderEntity to folderKeyEntity
    }

    private suspend fun decryptChildFolderKey(apiModel: FolderApiModel, parentKey: EncryptionKey): EncryptionKey {
        val decodedFolderKey = Base64.decodeBase64(apiModel.folderKey)
        val parentKeyForDecryption = parentKey.clone()
        val decrypted = encryptionContextProvider.withEncryptionContextSuspendable(parentKeyForDecryption) {
            decrypt(EncryptedByteArray(decodedFolderKey), EncryptionTag.FolderKey)
        }
        return EncryptionKey(decrypted)
    }

    private suspend fun persistDecryptedFolders(shareId: ShareId, folders: List<Pair<FolderEntity, FolderKeyEntity>>) {
        if (folders.isEmpty()) return
        database.inTransaction("upsertFoldersAndKeys") {
            localFolderDataSource.upsertFolders(folders.map { it.first })
            localFolderKeyDataSource.upsertKeys(folders.map { it.second })
        }
        PassLogger.d(TAG, "Decrypted and stored ${folders.size} folders for shareId=${shareId.id}")
    }

    private fun clearResolvedKeys(resolvedKeys: FolderResolvedKeys) {
        resolvedKeys.rootShareKeyByRotation.values.forEach { it.clear() }
        resolvedKeys.orphanParentKeyByFolderId.values.forEach { it.clear() }
    }

    private fun deduplicateFolderApiModelsById(folderApiModels: List<FolderApiModel>): DeduplicatedFolders {
        val seenFolderIds = mutableSetOf<String>()
        val duplicateFolderIds = linkedSetOf<String>()
        val uniqueFolders = folderApiModels.filter { folder ->
            if (seenFolderIds.add(folder.folderId)) {
                true
            } else {
                duplicateFolderIds.add(folder.folderId)
                false
            }
        }
        return DeduplicatedFolders(uniqueFolders = uniqueFolders, duplicateFolderIds = duplicateFolderIds)
    }

    private fun logDuplicateFolderIds(shareId: ShareId, duplicateFolderIds: Set<String>) {
        if (duplicateFolderIds.isEmpty()) return
        PassLogger.w(
            TAG,
            "Skipping duplicate folder IDs in sync for shareId=${shareId.id}: " +
                duplicateFolderIds.joinToString(limit = FAILED_FOLDER_IDS_LOG_LIMIT)
        )
    }

    private fun logTraversalOutcome(shareId: ShareId, result: FolderTraversalResult) {
        if (result.failedFolderIds.isNotEmpty()) {
            PassLogger.w(
                TAG,
                "Failed to decrypt ${result.failedFolderIds.size} folders in shareId=${shareId.id}: " +
                    result.failedFolderIds.joinToString(limit = FAILED_FOLDER_IDS_LOG_LIMIT)
            )
        }
        if (result.circuitBreakerTriggered) {
            PassLogger.w(
                TAG,
                "Folder decryption circuit breaker hit for shareId=${shareId.id}. " +
                    "Processed only first $MAX_FOLDERS_PER_TRAVERSAL folders per tree."
            )
        }
    }

    private fun mutableTraversalResult() = TraversalAccumulator(
        folders = mutableListOf(),
        failedFolderIds = linkedSetOf()
    )

    private fun mergeTraversalResult(target: TraversalAccumulator, source: FolderTraversalResult) {
        target.folders.addAll(source.folders)
        target.failedFolderIds.addAll(source.failedFolderIds)
        target.circuitBreakerTriggered = target.circuitBreakerTriggered || source.circuitBreakerTriggered
    }

    private fun emptyTraversalResult() = FolderTraversalResult(
        folders = emptyList(),
        failedFolderIds = emptySet(),
        circuitBreakerTriggered = false
    )

    private data class DeduplicatedFolders(
        val uniqueFolders: List<FolderApiModel>,
        val duplicateFolderIds: Set<String>
    )

    private data class FolderResolvedKeys(
        val rootShareKeyByRotation: Map<Long, EncryptionKey>,
        val orphanParentKeyByFolderId: Map<String, EncryptionKey>
    ) {
        fun isEmpty(): Boolean = rootShareKeyByRotation.isEmpty() && orphanParentKeyByFolderId.isEmpty()
    }

    private data class FolderDecryptionContext(
        val childrenMap: Map<String?, List<FolderApiModel>>,
        val userId: UserId,
        val shareId: ShareId,
        val rootShareKeyByRotation: Map<Long, EncryptionKey>
    )

    private data class FolderTraversalFrame(
        val parentFolderId: String?,
        val parentKey: EncryptionKey?,
        val children: List<FolderApiModel>,
        val clearParentKeyOnExit: Boolean,
        val nextChildIndex: Int = 0
    )

    private data class TraversalStep(
        val parentFolderId: String?,
        val parentKey: EncryptionKey?,
        val apiModel: FolderApiModel
    )

    private class TraversalAccumulator(
        val folders: MutableList<Pair<FolderEntity, FolderKeyEntity>>,
        val failedFolderIds: MutableSet<String>
    ) {
        var circuitBreakerTriggered: Boolean = false
        var processedFolders: Int = 0

        fun toTraversalResult(): FolderTraversalResult = FolderTraversalResult(
            folders = folders,
            failedFolderIds = failedFolderIds,
            circuitBreakerTriggered = circuitBreakerTriggered
        )
    }

    private data class FolderTraversalResult(
        val folders: List<Pair<FolderEntity, FolderKeyEntity>>,
        val failedFolderIds: Set<String>,
        val circuitBreakerTriggered: Boolean
    )

    private companion object {
        private const val TAG = "FolderSyncDecryptor"
        private const val MAX_FOLDERS_PER_TRAVERSAL = 1_000
        private const val FAILED_FOLDER_IDS_LOG_LIMIT = 20
    }
}
