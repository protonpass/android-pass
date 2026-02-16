/*
 * Copyright (c) 2025 Proton AG
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

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.context.EncryptionTag
import proton.android.pass.crypto.api.usecases.EncryptedFolderData
import proton.android.pass.crypto.api.usecases.OpenFolder
import proton.android.pass.data.api.repositories.FolderRepository
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.db.entities.FolderEntity
import proton.android.pass.data.impl.db.entities.FolderKeyEntity
import proton.android.pass.data.impl.extensions.toDomain
import proton.android.pass.data.impl.extensions.toEntity
import proton.android.pass.data.impl.local.LocalFolderDataSource
import proton.android.pass.data.impl.local.LocalFolderKeyDataSource
import proton.android.pass.data.impl.local.LocalShareKeyDataSource
import proton.android.pass.data.impl.remote.RemoteDataSourceConstants.PAGE_SIZE
import proton.android.pass.data.impl.remote.RemoteFolderDataSource
import proton.android.pass.data.impl.requests.CreateFolderRequest
import proton.android.pass.data.impl.requests.DeleteFoldersRequest
import proton.android.pass.data.impl.requests.FolderKeyRequest
import proton.android.pass.data.impl.requests.MoveFolderRequest
import proton.android.pass.data.impl.requests.UpdateFolderRequest
import proton.android.pass.data.impl.responses.FolderApiModel
import proton.android.pass.domain.Folder
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.key.ShareKey
import proton.android.pass.log.api.PassLogger
import proton_pass_folder_v1.FolderV1
import javax.inject.Inject

class FolderRepositoryImpl @Inject constructor(
    private val localFolderDataSource: LocalFolderDataSource,
    private val localFolderKeyDataSource: LocalFolderKeyDataSource,
    private val localShareKeyDataSource: LocalShareKeyDataSource,
    private val remoteFolderDataSource: RemoteFolderDataSource,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val openFolder: OpenFolder,
    private val shareKeyRepository: ShareKeyRepository,
    private val database: PassDatabase
) : FolderRepository {

    private suspend fun decryptAndStoreFolders(
        userId: UserId,
        shareId: ShareId,
        folderApiModels: List<FolderApiModel>
    ) = safeRunCatching {
        if (folderApiModels.isEmpty()) return@safeRunCatching
        val childrenMap = folderApiModels.groupBy { it.parentFolderId }
        val rootShareKeyByRotation = resolveRootShareKeyByRotation(userId, shareId, folderApiModels)
        val orphanParentKeyByFolderId = resolveOrphanParentKeyByFolderId(shareId, folderApiModels)

        if (rootShareKeyByRotation.isEmpty() && orphanParentKeyByFolderId.isEmpty()) {
            PassLogger.w(TAG, "No decryption keys resolved for shareId=${shareId.id}, skipping folder sync")
            return@safeRunCatching
        }

        val context = FolderDecryptionContext(
            childrenMap = childrenMap,
            userId = userId,
            shareId = shareId,
            rootShareKeyByRotation = rootShareKeyByRotation
        )

        val decryptedFolders = mutableListOf<Pair<FolderEntity, FolderKeyEntity>>()

        decryptedFolders.addAll(decryptFolderTree(null, null, context))

        for ((parentId, parentKeyBytes) in orphanParentKeyByFolderId) {
            decryptedFolders.addAll(decryptFolderTree(parentId, parentKeyBytes, context))
        }

        if (decryptedFolders.isNotEmpty()) {
            database.inTransaction("upsertFoldersAndKeys") {
                localFolderDataSource.upsertFolders(decryptedFolders.map { it.first })
                localFolderKeyDataSource.upsertKeys(decryptedFolders.map { it.second })
            }
            PassLogger.d(
                TAG,
                "Decrypted and stored ${decryptedFolders.size} folders for shareId=${shareId.id}"
            )
        }
    }.onFailure { e ->
        PassLogger.w(TAG, "Failed to decrypt and store folders for shareId=${shareId.id}")
        PassLogger.w(TAG, e)
    }.getOrThrow()

    private suspend fun resolveRootShareKeyByRotation(
        userId: UserId,
        shareId: ShareId,
        folderApiModels: List<FolderApiModel>
    ): Map<Long, ByteArray> {
        val rootRotations = folderApiModels
            .asSequence()
            .filter { it.parentFolderId == null }
            .map { it.keyRotation }
            .distinct()
            .toList()

        return encryptionContextProvider.withEncryptionContextSuspendable {
            rootRotations.mapNotNull { keyRotation ->
                safeRunCatching {
                    val shareKey = getShareKeyForRotation(userId, shareId, keyRotation)
                    keyRotation to decrypt(shareKey.key).copyOf()
                }.onFailure { e ->
                    PassLogger.w(TAG, "Cannot resolve share key for rotation=$keyRotation, shareId=${shareId.id}")
                    PassLogger.w(TAG, e)
                }.getOrNull()
            }.toMap()
        }
    }

    private suspend fun resolveOrphanParentKeyByFolderId(
        shareId: ShareId,
        folderApiModels: List<FolderApiModel>
    ): Map<String, ByteArray> {
        val batchFolderIds = folderApiModels.mapTo(mutableSetOf()) { it.folderId }
        val orphanParentIds = folderApiModels
            .asSequence()
            .mapNotNull { it.parentFolderId }
            .filter { it !in batchFolderIds }
            .distinct()
            .toList()

        return encryptionContextProvider.withEncryptionContextSuspendable {
            orphanParentIds.mapNotNull { parentFolderId ->
                safeRunCatching {
                    val parentFolderKey = localFolderKeyDataSource.getByFolderId(shareId, FolderId(parentFolderId))
                        ?: throw IllegalStateException("Parent folder key not found for parentId=$parentFolderId")
                    parentFolderId to decrypt(parentFolderKey.encryptedKey).copyOf()
                }.onFailure { e ->
                    PassLogger.w(
                        TAG,
                        "Cannot resolve local parent key for " +
                            "parentId=$parentFolderId, shareId=${shareId.id}"
                    )
                    PassLogger.w(TAG, e)
                }.getOrNull()
            }.toMap()
        }
    }

    private data class FolderDecryptionContext(
        val childrenMap: Map<String?, List<FolderApiModel>>,
        val userId: UserId,
        val shareId: ShareId,
        val rootShareKeyByRotation: Map<Long, ByteArray>
    )

    private suspend fun decryptFolderTree(
        parentFolderId: String?,
        parentKeyBytes: ByteArray?,
        context: FolderDecryptionContext
    ): List<Pair<FolderEntity, FolderKeyEntity>> {
        val children = context.childrenMap[parentFolderId] ?: return emptyList()
        val results = mutableListOf<Pair<FolderEntity, FolderKeyEntity>>()

        for (apiModel in children) {
            safeRunCatching {
                val decryptionKeyBytes = resolveDecryptionKey(
                    parentFolderId = parentFolderId,
                    parentKeyBytes = parentKeyBytes,
                    apiModel = apiModel,
                    context = context
                )
                val (folderEntity, folderKeyEntity) = decryptFolderApiModel(apiModel, decryptionKeyBytes, context)
                results.add(folderEntity to folderKeyEntity)

                val childKeyBytes = decryptChildFolderKey(apiModel, decryptionKeyBytes)
                results.addAll(decryptFolderTree(apiModel.folderId, childKeyBytes, context))
            }.onFailure { e ->
                PassLogger.w(TAG, "Failed to decrypt folder ${apiModel.folderId}")
                PassLogger.w(TAG, e)
            }
        }

        return results
    }

    private fun resolveDecryptionKey(
        parentFolderId: String?,
        parentKeyBytes: ByteArray?,
        apiModel: FolderApiModel,
        context: FolderDecryptionContext
    ): ByteArray = if (parentFolderId == null) {
        context.rootShareKeyByRotation[apiModel.keyRotation]
            ?: throw IllegalStateException(
                "No ShareKey found for shareId=${context.shareId.id}, keyRotation=${apiModel.keyRotation}"
            )
    } else {
        parentKeyBytes
            ?: throw IllegalStateException("Missing parent key for folder ${apiModel.folderId}")
    }

    private suspend fun decryptFolderApiModel(
        apiModel: FolderApiModel,
        decryptionKeyBytes: ByteArray,
        context: FolderDecryptionContext
    ): Pair<FolderEntity, FolderKeyEntity> {
        val parentKey = EncryptionKey(decryptionKeyBytes.copyOf())
        val encryptedData = EncryptedFolderData(
            folderId = FolderId(apiModel.folderId),
            parentFolderId = apiModel.parentFolderId?.let(::FolderId),
            keyRotation = apiModel.keyRotation,
            contentFormatVersion = apiModel.contentFormatVersion,
            content = apiModel.content,
            folderKey = apiModel.folderKey
        )
        val openedFolder = openFolder.open(encryptedData, parentKey)

        val folderProto = FolderV1.Folder.newBuilder()
            .setName(openedFolder.folderName)
            .build()
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

    private suspend fun decryptChildFolderKey(apiModel: FolderApiModel, parentKeyBytes: ByteArray): ByteArray {
        val decodedFolderKey = Base64.decodeBase64(apiModel.folderKey)
        return encryptionContextProvider.withEncryptionContextSuspendable(
            EncryptionKey(parentKeyBytes.copyOf())
        ) {
            decrypt(EncryptedByteArray(decodedFolderKey), EncryptionTag.FolderKey).copyOf()
        }
    }

    private suspend fun decryptAndSaveFolder(
        apiModel: FolderApiModel,
        userId: UserId,
        shareId: ShareId
    ): Folder = safeRunCatching {
        val parentKey = if (apiModel.parentFolderId == null) {
            val shareKey = getShareKeyForRotation(userId, shareId, apiModel.keyRotation)
            encryptionContextProvider.withEncryptionContextSuspendable {
                EncryptionKey(decrypt(shareKey.key))
            }
        } else {
            val parentFolderKey =
                localFolderKeyDataSource.getByFolderId(shareId, FolderId(apiModel.parentFolderId))
                    ?: throw IllegalStateException(
                        "Parent folder key not found for parentId=${apiModel.parentFolderId}"
                    )
            encryptionContextProvider.withEncryptionContextSuspendable {
                EncryptionKey(decrypt(parentFolderKey.encryptedKey))
            }
        }

        val encryptedData = EncryptedFolderData(
            folderId = FolderId(apiModel.folderId),
            parentFolderId = apiModel.parentFolderId?.let(::FolderId),
            keyRotation = apiModel.keyRotation,
            contentFormatVersion = apiModel.contentFormatVersion,
            content = apiModel.content,
            folderKey = apiModel.folderKey
        )
        val openedFolder = openFolder.open(encryptedData, parentKey)

        val folderProto = FolderV1.Folder.newBuilder()
            .setName(openedFolder.folderName)
            .build()
        val folderContentProto = encryptionContextProvider.withEncryptionContextSuspendable {
            encrypt(folderProto.toByteArray())
        }
        val folderEntity = apiModel.toEntity(userId, shareId.id, folderContentProto)
        localFolderDataSource.upsertFolder(folderEntity)

        val folderKeyEntity = FolderKeyEntity(
            folderId = apiModel.folderId,
            userId = userId.id,
            shareId = shareId.id,
            keyRotation = apiModel.keyRotation,
            encryptedKey = openedFolder.reencryptedFolderKey
        )
        localFolderKeyDataSource.upsertKey(folderKeyEntity)

        encryptionContextProvider.withEncryptionContextSuspendable {
            folderEntity.toDomain(this)
        }
    }.onFailure { e ->
        PassLogger.w(TAG, "Failed to decrypt and save folder ${apiModel.folderId}")
        PassLogger.w(TAG, e)
    }.getOrThrow()

    private suspend fun getShareKeyForRotation(
        userId: UserId,
        shareId: ShareId,
        keyRotation: Long
    ): ShareKey {
        val shareKeyEntity = localShareKeyDataSource
            .getForShareAndRotation(userId, shareId, keyRotation)
            .firstOrNull()

        if (shareKeyEntity != null) return shareKeyEntity.toDomain()

        val latestKey = shareKeyRepository.getLatestKeyForShare(shareId).firstOrNull()
        if (latestKey != null && latestKey.rotation == keyRotation) return latestKey

        throw IllegalStateException("No ShareKey found for shareId=${shareId.id}, keyRotation=$keyRotation")
    }

    override fun observeFolders(userId: UserId, shareId: ShareId): Flow<List<Folder>> =
        localFolderDataSource.observeFolders(
            userId = userId,
            shareId = shareId,
            parentFolderId = null
        ).map { folderEntities ->
            encryptionContextProvider.withEncryptionContextSuspendable {
                folderEntities.map { it.toDomain(this) }
            }
        }

    override suspend fun refreshFolders(userId: UserId, shareId: ShareId) = safeRunCatching {
        var currentSinceToken: String? = null
        val allFolders = mutableListOf<FolderApiModel>()

        while (currentCoroutineContext().isActive) {
            val foldersPage = remoteFolderDataSource.retrieveFolders(
                userId = userId,
                shareId = shareId,
                sinceToken = currentSinceToken,
                pageSize = PAGE_SIZE
            )

            if (foldersPage.folders.isEmpty()) {
                break
            }

            allFolders.addAll(foldersPage.folders)
            if (foldersPage.lastToken == null || foldersPage.folders.size < PAGE_SIZE) {
                break
            }

            currentSinceToken = foldersPage.lastToken
        }

        PassLogger.i(
            TAG,
            "Total folders fetched: ${allFolders.size} for shareId=${shareId.id}"
        )
        if (allFolders.isNotEmpty()) {
            decryptAndStoreFolders(userId, shareId, allFolders)
        }
    }.onFailure { error ->
        PassLogger.w(TAG, "Folder sync failed for shareId=${shareId.id}")
        PassLogger.w(TAG, error)
    }.getOrThrow()

    override fun observeFolder(
        userId: UserId,
        shareId: ShareId,
        folderId: FolderId
    ): Flow<Folder?> = localFolderDataSource.observeFolder(
        userId = userId,
        shareId = shareId,
        folderId = folderId
    ).map { folderEntity ->
        folderEntity?.let {
            encryptionContextProvider.withEncryptionContextSuspendable {
                it.toDomain(this)
            }
        }
    }

    override suspend fun createFolder(
        userId: UserId,
        shareId: ShareId,
        parentFolderId: FolderId?,
        keyRotation: Long,
        contentFormatVersion: Int,
        content: String,
        folderKey: String
    ): Folder = safeRunCatching {
        PassLogger.d(
            TAG,
            "Creating folder for shareId=${shareId.id}, parentFolderId=${parentFolderId?.id}"
        )

        val request = CreateFolderRequest(
            parentFolderId = parentFolderId?.id,
            contentFormatVersion = contentFormatVersion,
            content = content,
            keyRotation = keyRotation,
            folderKey = folderKey
        )
        val apiModel = remoteFolderDataSource.createFolder(userId, shareId, request)

        val result = decryptAndSaveFolder(apiModel, userId, shareId)
        PassLogger.i(TAG, "Successfully created folder ${apiModel.folderId}")
        result
    }.onFailure { e ->
        PassLogger.w(
            TAG,
            "Failed to create folder for shareId=${shareId.id}, parentFolderId=${parentFolderId?.id}"
        )
        PassLogger.w(TAG, e)
    }.getOrThrow()

    override suspend fun updateFolder(
        userId: UserId,
        shareId: ShareId,
        folderId: FolderId,
        keyRotation: Long,
        contentFormatVersion: Int,
        content: String
    ): Folder = safeRunCatching {
        PassLogger.d(TAG, "Updating folder ${folderId.id} in shareId=${shareId.id}")

        val request = UpdateFolderRequest(
            keyRotation = keyRotation,
            contentFormatVersion = contentFormatVersion,
            content = content
        )
        val apiModel = remoteFolderDataSource.updateFolder(userId, shareId, folderId, request)

        val result = decryptAndSaveFolder(apiModel, userId, shareId)
        PassLogger.i(TAG, "Successfully updated folder ${folderId.id}")
        result
    }.onFailure { e ->
        PassLogger.w(TAG, "Failed to update folder ${folderId.id} in shareId=${shareId.id}")
        PassLogger.w(TAG, e)
    }.getOrThrow()

    override suspend fun moveFolder(
        userId: UserId,
        shareId: ShareId,
        folderId: FolderId,
        parentFolderId: FolderId?,
        folderKeys: List<Pair<Long, String>>
    ): Folder = safeRunCatching {
        PassLogger.d(
            TAG,
            "Moving folder ${folderId.id} to parent=${parentFolderId?.id} in shareId=${shareId.id}"
        )

        val request = MoveFolderRequest(
            parentFolderId = parentFolderId?.id,
            folderKeys = folderKeys.map { (keyRotation, folderKey) ->
                FolderKeyRequest(keyRotation, folderKey)
            }
        )
        val apiModel = remoteFolderDataSource.moveFolder(userId, shareId, folderId, request)

        val result = decryptAndSaveFolder(apiModel, userId, shareId)
        PassLogger.i(TAG, "Successfully moved folder ${folderId.id}")
        result
    }.onFailure { e ->
        PassLogger.w(TAG, "Failed to move folder ${folderId.id} in shareId=${shareId.id}")
        PassLogger.w(TAG, e)
    }.getOrThrow()

    override suspend fun deleteFolders(
        userId: UserId,
        shareId: ShareId,
        folderIds: List<FolderId>
    ) = safeRunCatching {
        PassLogger.d(TAG, "Deleting ${folderIds.size} folders from shareId=${shareId.id}")

        val request = DeleteFoldersRequest(folderIds.map { it.id })
        remoteFolderDataSource.deleteFolders(userId, shareId, request)

        val deleted = localFolderDataSource.deleteFolders(userId, shareId, folderIds)
        if (!deleted) {
            PassLogger.w(TAG, "Failed to delete folders locally: ${folderIds.map { it.id }}")
        } else {
            PassLogger.i(TAG, "Successfully deleted ${folderIds.size} folders")
        }
    }.onFailure { e ->
        PassLogger.w(TAG, "Failed to delete folders from shareId=${shareId.id}")
        PassLogger.w(TAG, e)
    }.getOrThrow()

    override suspend fun deleteFoldersLocally(
        userId: UserId,
        shareId: ShareId,
        folderIds: List<FolderId>
    ) = safeRunCatching {
        if (folderIds.isEmpty()) return@safeRunCatching
        val deleted = localFolderDataSource.deleteFolders(userId, shareId, folderIds)
        if (!deleted) {
            PassLogger.w(TAG, "Failed to delete folders locally: ${folderIds.map { it.id }}")
        } else {
            PassLogger.i(TAG, "Successfully deleted ${folderIds.size} folders locally")
        }
    }.onFailure { e ->
        PassLogger.w(TAG, "Failed to delete folders locally from shareId=${shareId.id}")
        PassLogger.w(TAG, e)
    }.getOrThrow()

    companion object {
        private const val TAG = "FolderRepositoryImpl"
    }
}
