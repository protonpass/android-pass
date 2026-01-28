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

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.repositories.FolderRepository
import proton.android.pass.data.impl.extensions.toDomain
import proton.android.pass.data.impl.extensions.toEntity
import proton.android.pass.data.impl.local.LocalFolderDataSource
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
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class FolderRepositoryImpl @Inject constructor(
    private val localFolderDataSource: LocalFolderDataSource,
    private val remoteFolderDataSource: RemoteFolderDataSource,
    private val encryptionContextProvider: EncryptionContextProvider
) : FolderRepository {

    private suspend fun encryptFolderContent(base64Content: String): EncryptedByteArray = safeRunCatching {
        val decodedContent = Base64.decodeBase64(base64Content)
        encryptionContextProvider.withEncryptionContextSuspendable {
            encrypt(decodedContent)
        }
    }.onFailure { e ->
        PassLogger.w(TAG, "Failed to decode/encrypt folder content")
        PassLogger.w(TAG, e)
    }.getOrThrow()

    private suspend fun saveAndReturnFolder(
        apiModel: FolderApiModel,
        userId: UserId,
        shareId: ShareId,
        encryptedContent: EncryptedByteArray
    ): Folder {
        val entity = apiModel.toEntity(userId, shareId.id, encryptedContent)
        localFolderDataSource.upsertFolder(entity)
        return encryptionContextProvider.withEncryptionContextSuspendable {
            entity.toDomain(this)
        }
    }

    override fun observeFolders(
        userId: UserId,
        shareId: ShareId,
        sinceToken: String?,
        pageSize: Int?
    ): Flow<List<Folder>> = localFolderDataSource.observeFolders(
        userId = userId,
        shareId = shareId,
        parentFolderId = null
    ).map { folderEntities ->
        encryptionContextProvider.withEncryptionContextSuspendable {
            folderEntities.map { it.toDomain(this) }
        }
    }.onStart {
        coroutineScope {
            launch {
                safeRunCatching {
                    var currentSinceToken: String? = sinceToken
                    val allFolders = mutableListOf<FolderApiModel>()
                    val effectivePageSize = pageSize ?: PAGE_SIZE

                    while (currentCoroutineContext().isActive) {
                        val foldersPage = remoteFolderDataSource.retrieveFolders(
                            userId = userId,
                            shareId = shareId,
                            sinceToken = currentSinceToken,
                            pageSize = effectivePageSize
                        )

                        if (foldersPage.folders.isEmpty()) {
                            break
                        }

                        allFolders.addAll(foldersPage.folders)
                        if (foldersPage.lastToken == null || foldersPage.folders.size < effectivePageSize) {
                            break
                        }

                        currentSinceToken = foldersPage.lastToken
                    }

                    if (allFolders.isNotEmpty()) {
                        val entities = allFolders.map { apiModel ->
                            val encryptedContent = encryptFolderContent(apiModel.content)
                            apiModel.toEntity(userId, shareId.id, encryptedContent)
                        }
                        localFolderDataSource.upsertFolders(entities)
                        PassLogger.d(
                            TAG,
                            "Synced ${entities.size} folders for shareId=${shareId.id}"
                        )
                    }
                }.onFailure { error ->
                    PassLogger.w(TAG, "Background folder sync failed for shareId=${shareId.id}")
                    PassLogger.w(TAG, error)
                }
            }
        }
    }

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
        val encryptedContent = encryptFolderContent(apiModel.content)

        val result = saveAndReturnFolder(apiModel, userId, shareId, encryptedContent)
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
        val encryptedContent = encryptFolderContent(apiModel.content)

        val result = saveAndReturnFolder(apiModel, userId, shareId, encryptedContent)
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
        val existingEntity = localFolderDataSource.getById(userId, shareId, folderId)
        val encryptedContent = if (existingEntity != null) {
            existingEntity.encryptedContent
        } else {
            PassLogger.w(
                TAG,
                "Folder ${folderId.id} not found locally during move, re-encrypting from API"
            )
            encryptFolderContent(apiModel.content)
        }

        val result = saveAndReturnFolder(apiModel, userId, shareId, encryptedContent)
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

    companion object {
        private const val TAG = "FolderRepositoryImpl"
    }
}
