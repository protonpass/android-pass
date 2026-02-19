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
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.usecases.folders.CreateFolder
import proton.android.pass.crypto.api.usecases.folders.EncryptedFolderData
import proton.android.pass.crypto.api.usecases.folders.MoveFolder
import proton.android.pass.crypto.api.usecases.folders.OpenFolder
import proton.android.pass.crypto.api.usecases.folders.UpdateFolder
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
import proton.android.pass.data.impl.requests.UpdateFolderContent
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
    private val appDispatchers: AppDispatchers,
    private val localFolderDataSource: LocalFolderDataSource,
    private val localFolderKeyDataSource: LocalFolderKeyDataSource,
    private val localShareKeyDataSource: LocalShareKeyDataSource,
    private val remoteFolderDataSource: RemoteFolderDataSource,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val createFolder: CreateFolder,
    private val updateFolder: UpdateFolder,
    private val moveFolder: MoveFolder,
    private val openFolder: OpenFolder,
    private val shareKeyRepository: ShareKeyRepository,
    private val database: PassDatabase
) : FolderRepository {

    private val folderSyncDecryptor = FolderSyncDecryptor(
        appDispatchers = appDispatchers,
        localFolderDataSource = localFolderDataSource,
        localFolderKeyDataSource = localFolderKeyDataSource,
        encryptionContextProvider = encryptionContextProvider,
        openFolder = openFolder,
        database = database
    )

    private suspend fun decryptAndStoreFolders(
        userId: UserId,
        shareId: ShareId,
        folderApiModels: List<FolderApiModel>
    ) = safeRunCatching {
        folderSyncDecryptor.decryptAndStoreFolders(
            userId = userId,
            shareId = shareId,
            folderApiModels = folderApiModels,
            getShareKeyForRotation = { keyRotation ->
                getShareKeyForRotation(userId, shareId, keyRotation)
            }
        )
    }.onFailure { e ->
        PassLogger.w(TAG, "Failed to decrypt and store folders for shareId=${shareId.id}")
        PassLogger.w(TAG, e)
    }.getOrThrow()

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
                localFolderKeyDataSource.getByFolderId(userId, shareId, FolderId(apiModel.parentFolderId))
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
        val openFolderKey = parentKey.clone()
        val openedFolder = try {
            openFolder.open(encryptedData, openFolderKey)
        } finally {
            openFolderKey.clear()
        }

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

        try {
            encryptionContextProvider.withEncryptionContextSuspendable {
                folderEntity.toDomain(this)
            }
        } finally {
            parentKey.clear()
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

    private suspend fun getParentKeyForFolder(
        userId: UserId,
        shareId: ShareId,
        parentFolderId: FolderId?,
        keyRotation: Long
    ): EncryptionKey = if (parentFolderId == null) {
        val shareKey = getShareKeyForRotation(userId, shareId, keyRotation)
        encryptionContextProvider.withEncryptionContextSuspendable {
            EncryptionKey(decrypt(shareKey.key))
        }
    } else {
        val parentFolderKey = localFolderKeyDataSource.getByFolderId(userId, shareId, parentFolderId)
            ?: throw IllegalStateException("Parent folder key not found for folderId=${parentFolderId.id}")
        encryptionContextProvider.withEncryptionContextSuspendable {
            EncryptionKey(decrypt(parentFolderKey.encryptedKey))
        }
    }

    private suspend fun reencryptFolderKeyForMove(
        folderKeyEntity: FolderKeyEntity,
        newParentKey: EncryptionKey
    ): String {
        val decryptedFolderKey = encryptionContextProvider.withEncryptionContextSuspendable {
            decrypt(folderKeyEntity.encryptedKey)
        }

        return moveFolder.reencryptFolderKey(
            folderKey = EncryptionKey(decryptedFolderKey),
            newParentKey = newParentKey
        )
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

    override fun observeFoldersByParentId(
        userId: UserId,
        shareId: ShareId,
        parentFolderId: FolderId?
    ): Flow<List<Folder>> = localFolderDataSource.observeFolders(
        userId = userId,
        shareId = shareId,
        parentFolderId = parentFolderId
    ).map { folderEntities ->
        encryptionContextProvider.withEncryptionContextSuspendable {
            folderEntities.map { it.toDomain(this) }
        }
    }

    override suspend fun getFolderHierarchy(
        userId: UserId,
        shareId: ShareId,
        folderId: FolderId
    ): List<Folder> {
        val visitedFolderIds = mutableSetOf<String>()
        val folderEntities = mutableListOf<FolderEntity>()
        var currentFolderId: FolderId? = folderId

        while (currentFolderId != null && visitedFolderIds.add(currentFolderId.id)) {
            val folderEntity = localFolderDataSource.getById(userId, shareId, currentFolderId) ?: break
            folderEntities += folderEntity
            currentFolderId = folderEntity.parentFolderId?.let(::FolderId)
        }

        return encryptionContextProvider.withEncryptionContextSuspendable {
            folderEntities
                .asReversed()
                .map { it.toDomain(this) }
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
        folderName: String
    ): Folder = safeRunCatching {
        PassLogger.d(
            TAG,
            "Creating folder for shareId=${shareId.id}, parentFolderId=${parentFolderId?.id}"
        )

        val shareKey = shareKeyRepository.getLatestKeyForShare(shareId).firstOrNull()
            ?: throw IllegalStateException("No share key found for shareId=${shareId.id}")

        val (parentKey, keyRotation) = if (parentFolderId != null) {
            val parentFolderKey = localFolderKeyDataSource.getByFolderId(
                userId = userId,
                shareId = shareId,
                folderId = parentFolderId
            ) ?: throw IllegalStateException("No folder key found for parentFolderId=${parentFolderId.id}")

            val decryptedParentKey = encryptionContextProvider.withEncryptionContext {
                EncryptionKey(decrypt(parentFolderKey.encryptedKey))
            }
            Pair(decryptedParentKey, shareKey.rotation)
        } else {
            val decryptedShareKey = encryptionContextProvider.withEncryptionContext {
                EncryptionKey(decrypt(shareKey.key))
            }
            Pair(decryptedShareKey, shareKey.rotation)
        }

        val payload = createFolder.create(parentKey, keyRotation, folderName)

        parentKey.clear()

        val request = CreateFolderRequest(
            parentFolderId = parentFolderId?.id,
            contentFormatVersion = payload.request.contentFormatVersion,
            content = payload.request.content,
            keyRotation = payload.request.keyRotation,
            folderKey = payload.request.folderKey
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
        folderName: String
    ): Folder = safeRunCatching {
        PassLogger.d(TAG, "Updating folder ${folderId.id} in shareId=${shareId.id}")
        val folderKeyEntity = localFolderKeyDataSource.getByFolderId(userId, shareId, folderId)
            ?: throw IllegalStateException("No folder key found for folderId=${folderId.id}")

        val decryptedFolderKey = encryptionContextProvider.withEncryptionContextSuspendable {
            EncryptionKey(decrypt(folderKeyEntity.encryptedKey))
        }
        val payload = updateFolder.update(
            folderKey = decryptedFolderKey,
            keyRotation = folderKeyEntity.keyRotation,
            folderName = folderName
        )

        val request = UpdateFolderRequest(
            content = UpdateFolderContent(
                keyRotation = payload.keyRotation,
                contentFormatVersion = payload.contentFormatVersion,
                content = payload.content
            )
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
        newParentFolderId: FolderId?
    ): Folder = safeRunCatching {
        PassLogger.d(
            TAG,
            "Moving folder ${folderId.id} to parent=${newParentFolderId?.id} in shareId=${shareId.id}"
        )
        val folderKeyEntity = localFolderKeyDataSource.getByFolderId(userId, shareId, folderId)
            ?: throw IllegalStateException("No folder key found for folderId=${folderId.id}")

        val newParentKey = getParentKeyForFolder(
            userId = userId,
            shareId = shareId,
            parentFolderId = newParentFolderId,
            keyRotation = folderKeyEntity.keyRotation
        )

        val reencryptedFolderKey = reencryptFolderKeyForMove(
            folderKeyEntity = folderKeyEntity,
            newParentKey = newParentKey
        )

        newParentKey.clear()

        val request = MoveFolderRequest(
            parentFolderId = newParentFolderId?.id,
            folderKeys = listOf(
                FolderKeyRequest(
                    keyRotation = folderKeyEntity.keyRotation,
                    folderKey = reencryptedFolderKey
                )
            )
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
