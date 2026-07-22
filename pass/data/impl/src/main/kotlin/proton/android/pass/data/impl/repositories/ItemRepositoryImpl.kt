/*
 * Copyright (c) 2023-2026 Proton AG
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

import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getAccounts
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.repository.UserAddressRepository
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.common.api.transpose
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.impl.extensions.isDomainMatchingEnabled
import proton.android.pass.crypto.api.usecases.CreateItem
import proton.android.pass.crypto.api.usecases.ItemKeyWithRotation
import proton.android.pass.crypto.api.usecases.MigrateItem
import proton.android.pass.crypto.api.usecases.OpenItem
import proton.android.pass.crypto.api.usecases.UpdateItem
import proton.android.pass.data.api.ItemCountSummary
import proton.android.pass.data.api.ItemPendingEvent
import proton.android.pass.data.api.crypto.GetShareAndItemKey
import proton.android.pass.data.api.errors.ItemNotFoundError
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.repositories.ItemRevision
import proton.android.pass.data.api.repositories.MigrateItemsResult
import proton.android.pass.data.api.repositories.SearchIndexRepository
import proton.android.pass.data.api.repositories.PinItemsResult
import proton.android.pass.data.api.repositories.ShareItemCount
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.repositories.SetShareItemsResult
import proton.android.pass.data.api.repositories.VaultProgress
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.db.entities.ItemEntity
import proton.android.pass.data.impl.extensions.hasPackageName
import proton.android.pass.data.impl.extensions.hasTotp
import proton.android.pass.data.impl.extensions.hasWebsite
import proton.android.pass.data.impl.extensions.toCrypto
import proton.android.pass.data.impl.extensions.toDomain
import proton.android.pass.data.impl.extensions.toEncryptedDomain
import proton.android.pass.data.impl.extensions.toItemRevision
import proton.android.pass.data.impl.extensions.toRequest
import proton.android.pass.data.impl.extensions.with
import proton.android.pass.data.impl.extensions.withPasskey
import proton.android.pass.data.impl.extensions.withUrl
import proton.android.pass.data.impl.local.LocalItemDataSource
import proton.android.pass.data.impl.remote.RemoteItemDataSource
import proton.android.pass.data.impl.requests.CreateAliasRequest
import proton.android.pass.data.impl.requests.CreateItemAliasRequest
import proton.android.pass.data.impl.requests.ItemKeyBody
import proton.android.pass.data.impl.requests.MigrateItemsBody
import proton.android.pass.data.impl.requests.MigrateItemsRequest
import proton.android.pass.data.impl.requests.MoveItemsToFolderRequest
import proton.android.pass.data.impl.requests.TrashItemRevision
import proton.android.pass.data.impl.requests.TrashItemsRequest
import proton.android.pass.data.impl.requests.UpdateItemFlagsRequest
import proton.android.pass.data.impl.responses.TrashItemsResponse
import proton.android.pass.data.impl.util.TimeUtil
import proton.android.pass.datamodels.api.serializeToProto
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemEncrypted
import proton.android.pass.domain.ItemFlag
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.Passkey
import proton.android.pass.domain.Share
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareSelection
import proton.android.pass.domain.VaultId
import proton.android.pass.domain.entity.NewAlias
import proton.android.pass.domain.entity.PackageInfo
import proton.android.pass.domain.events.EventToken
import proton.android.pass.domain.events.SyncEventShareItem
import proton.android.pass.domain.key.FolderKey
import proton.android.pass.domain.key.InviteKey
import proton.android.pass.domain.key.ShareKey
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton_pass_item_v1.ItemV1
import javax.inject.Inject

@Suppress("LongParameterList", "TooManyFunctions", "LargeClass")
class ItemRepositoryImpl @Inject constructor(
    private val database: PassDatabase,
    private val accountManager: AccountManager,
    override val userAddressRepository: UserAddressRepository,
    private val shareRepository: ShareRepository,
    private val createItem: CreateItem,
    private val updateItem: UpdateItem,
    private val localItemDataSource: LocalItemDataSource,
    private val remoteItemDataSource: RemoteItemDataSource,
    private val shareKeyRepository: ShareKeyRepository,
    private val openItem: OpenItem,
    private val migrateItem: MigrateItem,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val getShareAndItemKey: GetShareAndItemKey,
    private val folderKeyRepository: FolderKeyRepository,
    private val appDispatchers: AppDispatchers,
    private val featureFlagsRepository: FeatureFlagsPreferencesRepository,
    private val searchIndexRepository: SearchIndexRepository
) : BaseRepository(userAddressRepository), ItemRepository {

    private suspend fun isDomainMatchingEnabled() = featureFlagsRepository.isDomainMatchingEnabled()

    @Suppress("TooGenericExceptionCaught")
    override suspend fun createItem(
        userId: UserId,
        share: Share,
        folderId: FolderId?,
        contents: ItemContents
    ): Item = withUserAddress(userId) { userAddress ->
        val shareKey = shareKeyRepository.getLatestKeyForShare(share.id).first()
        val parentKey = folderId?.let {
            folderKeyRepository.getFolderKey(userId, share.id, it)
                ?: throw IllegalStateException("No folder key found for folderId=${it.id}")
        } ?: shareKey

        val body = try {
            createItem.create(parentKey, contents, isDomainMatchingEnabled())
        } catch (e: Exception) {
            PassLogger.w(TAG, "Error creating item")
            PassLogger.w(TAG, e)
            throw e
        }

        val itemResponse =
            remoteItemDataSource.createItem(userId, share.id, body.toRequest(folderId))
        val entity = itemResponseToEntity(
            userAddress.userId,
            userAddress.addressId,
            itemResponse,
            share,
            listOf(shareKey)
        )
        localItemDataSource.upsertItem(entity)

        // Index the new item for search
        searchIndexRepository.indexItem(userId, share.id, ItemId(entity.id))

        encryptionContextProvider.withEncryptionContextSuspendable {
            entity.toDomain(this)
        }
    }

    override suspend fun createAlias(
        userId: UserId,
        share: Share,
        folderId: FolderId?,
        newAlias: NewAlias
    ): Item = withUserAddress(userId) { userAddress ->
        val shareKey = shareKeyRepository.getLatestKeyForShare(share.id).first()
        val itemContents = ItemContents.Alias(
            title = newAlias.contents.title,
            note = newAlias.contents.note,
            customFields = newAlias.contents.customFields,
            aliasEmail = "" // Not used when creating the payload,
        )
        val parentKey = folderId?.let {
            folderKeyRepository.getFolderKey(userId, share.id, it)
                ?: throw IllegalStateException("No folder key found for folderId=${it.id}")
        } ?: shareKey
        val body = createItem.create(parentKey, itemContents, isDomainMatchingEnabled())

        val mailboxIds = newAlias.mailboxes.map { it.id }
        val requestBody = CreateAliasRequest(
            prefix = newAlias.prefix,
            signedSuffix = newAlias.suffix.signedSuffix,
            mailboxes = mailboxIds,
            aliasName = newAlias.aliasName,
            item = body.toRequest(folderId)
        )

        val itemResponse = remoteItemDataSource.createAlias(userId, share.id, requestBody)
        val entity = itemResponseToEntity(
            userAddress.userId,
            userAddress.addressId,
            itemResponse,
            share,
            listOf(shareKey)
        )
        localItemDataSource.upsertItem(entity)

        // Index the new alias for search
        searchIndexRepository.indexItem(userId, share.id, ItemId(entity.id))

        encryptionContextProvider.withEncryptionContextSuspendable {
            entity.toDomain(this)
        }
    }

    @SuppressWarnings("LongMethod")
    override suspend fun createLoginAndAlias(
        userId: UserId,
        shareId: ShareId,
        folderId: FolderId?,
        contents: ItemContents.Login,
        newAlias: NewAlias
    ): Item = withUserAddress(userId) { userAddress ->
        val share = shareRepository.getById(userId, shareId)
        val shareKey = shareKeyRepository.getLatestKeyForShare(shareId).first()
        val parentKey = folderId?.let {
            folderKeyRepository.getFolderKey(userId, shareId, it)
                ?: throw IllegalStateException("No folder key found for folderId=${it.id}")
        } ?: shareKey
        val domainMatchingEnabled = isDomainMatchingEnabled()
        val request = safeRunCatching {
            val itemBody = createItem.create(parentKey, contents, domainMatchingEnabled)
            val aliasContents = ItemContents.Alias(
                title = contents.title,
                note = newAlias.contents.note,
                customFields = newAlias.contents.customFields,
                aliasEmail = "" // Not used when creating the payload
            )
            val aliasBody = createItem.create(shareKey, aliasContents, domainMatchingEnabled)

            CreateItemAliasRequest(
                alias = CreateAliasRequest(
                    prefix = newAlias.prefix,
                    signedSuffix = newAlias.suffix.signedSuffix,
                    aliasName = newAlias.aliasName,
                    mailboxes = newAlias.mailboxes.map { it.id },
                    item = aliasBody.toRequest()
                ),
                item = itemBody.toRequest(folderId)
            )
        }.fold(
            onSuccess = { it },
            onFailure = {
                PassLogger.w(TAG, "Error creating item")
                PassLogger.e(TAG, it)
                throw it
            }
        )

        val itemResponse = remoteItemDataSource.createItemAndAlias(userId, shareId, request)
        val itemEntity =
            itemResponseToEntity(
                userAddress.userId,
                userAddress.addressId,
                itemResponse.item.toDomain(),
                share,
                listOf(shareKey)
            )
        val aliasEntity =
            itemResponseToEntity(
                userAddress.userId,
                userAddress.addressId,
                itemResponse.alias.toDomain(),
                share,
                listOf(shareKey)
            )
        database.inTransaction("createItemAndAlias") {
            localItemDataSource.upsertItem(itemEntity)
            localItemDataSource.upsertItem(aliasEntity)
        }

        // Index both items for search
        searchIndexRepository.indexItems(
            userId,
            listOf(
                share.id to ItemId(itemEntity.id),
                share.id to ItemId(aliasEntity.id)
            )
        )

        encryptionContextProvider.withEncryptionContextSuspendable {
            itemEntity.toDomain(this)
        }
    }

    override suspend fun updateItem(
        userId: UserId,
        share: Share,
        item: Item,
        contents: ItemContents
    ): Item {
        val localEntity = localItemDataSource.getById(userId, share.id, item.id)
            ?: throw IllegalStateException("Item not found in local database")

        val decryptedProto = encryptionContextProvider.withEncryptionContextSuspendable {
            decrypt(localEntity.encryptedContent)
        }

        val decodedProto = ItemV1.Item.parseFrom(decryptedProto)
        val itemContents = encryptionContextProvider.withEncryptionContextSuspendable {
            contents.serializeToProto(
                itemUuid = item.itemUuid,
                builder = decodedProto.toBuilder(),
                encryptionContext = this
            )
        }

        return performUpdate(
            userId,
            share,
            item,
            itemContents,
            isDomainMatchingEnabled()
        )
    }

    override suspend fun updateItemFlags(
        userId: UserId,
        share: Share,
        itemId: ItemId,
        flag: ItemFlag,
        isFlagEnabled: Boolean
    ): Item = when (flag) {
        ItemFlag.SkipHealthCheck -> UpdateItemFlagsRequest(skipHealthCheck = isFlagEnabled)
        ItemFlag.SkipWeakPasswordCheck -> UpdateItemFlagsRequest(skipWeakPasswordCheck = isFlagEnabled)
        ItemFlag.SkipCompromisedPasswordCheck ->
            UpdateItemFlagsRequest(skipCompromisedPasswordCheck = isFlagEnabled)
        ItemFlag.SkipReusedPasswordCheck -> UpdateItemFlagsRequest(skipReusedPasswordCheck = isFlagEnabled)
        ItemFlag.Skip2FACheck -> UpdateItemFlagsRequest(skip2FACheck = isFlagEnabled)
        ItemFlag.EmailBreached,
        ItemFlag.AliasDisabled,
        ItemFlag.HasAttachments,
        ItemFlag.HasHadAttachments -> UpdateItemFlagsRequest()
    }.let { updateItemFlagsRequest ->
        remoteItemDataSource.updateItemFlags(
            userId = userId,
            shareId = share.id,
            itemId = itemId,
            body = updateItemFlagsRequest
        )
    }.let { itemRevision ->
        withUserAddress(userId) { userAddress ->
            itemResponseToEntity(
                userId = userAddress.userId,
                addressId = userAddress.addressId,
                itemRevision = itemRevision,
                share = share,
                shareKeys = listOf(shareKeyRepository.getLatestKeyForShare(share.id).first())
            )
        }
    }.let { itemEntity ->
        localItemDataSource.upsertItem(itemEntity)

        encryptionContextProvider.withEncryptionContextSuspendable {
            itemEntity.toDomain(this)
        }
    }

    override suspend fun updateLocalItemFlags(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        flag: ItemFlag,
        isFlagEnabled: Boolean
    ) {
        val item: ItemEntity = localItemDataSource.getById(userId, shareId, itemId)
            ?: throw ItemNotFoundError(itemId, shareId)
        val updatedFlags = if (isFlagEnabled) {
            item.flags or flag.value // set the flag
        } else {
            item.flags and flag.value.inv() // clear the flag
        }
        localItemDataSource.updateItemFlags(shareId, itemId, updatedFlags)
    }

    override suspend fun updateLocalItemsFlags(
        userId: UserId,
        items: List<Pair<ShareId, ItemId>>,
        flag: ItemFlag,
        isFlagEnabled: Boolean
    ) {
        items.groupBy { it.first }
            .map { (shareId, itemIds) ->
                localItemDataSource.getByIdList(userId, shareId, itemIds.map { it.second })
            }
            .flatten()
            .forEach {
                val updatedFlags = if (!isFlagEnabled) {
                    it.flags or flag.value
                } else {
                    it.flags and flag.value.inv()
                }
                localItemDataSource.updateItemFlags(
                    ShareId(it.shareId),
                    ItemId(it.id),
                    updatedFlags
                )
            }
    }

    override suspend fun refreshItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        eventToken: EventToken
    ) {
        val itemEntity = fetchItemEntity(userId, shareId, itemId)
        localItemDataSource.upsertItem(itemEntity)

        // Update search index for remote item changes
        searchIndexRepository.indexItem(userId, shareId, itemId)
    }

    override suspend fun refreshItems(userId: UserId, items: List<SyncEventShareItem>) {
        if (items.isEmpty()) return
        val entities = coroutineScope {
            items.chunked(MAX_CONCURRENT_ITEM_REFRESHES).flatMap { chunk ->
                chunk.map { (shareId, itemId, _) ->
                    async {
                        safeRunCatching { fetchItemEntity(userId, shareId, itemId) }
                            .onFailure { err ->
                                PassLogger.w(TAG, "Failed to fetch item ${itemId.id} in share ${shareId.id}")
                                PassLogger.w(TAG, err)
                            }
                            .getOrNull()
                    }
                }.awaitAll().filterNotNull()
            }
        }
        if (entities.isNotEmpty()) {
            database.inTransaction("refreshItems") {
                localItemDataSource.upsertItems(entities)
            }
        }
    }

    private suspend fun fetchItemEntity(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): ItemEntity {
        val itemRevision = remoteItemDataSource.getItem(
            userId = userId,
            shareId = shareId,
            itemId = itemId
        )
        val share = shareRepository.getById(userId, shareId)
        val shareKeys = shareKeyRepository.getShareKeys(
            userId = userId,
            addressId = share.addressId,
            shareId = shareId,
            groupEmail = share.groupEmail
        ).firstOrNull() ?: throw IllegalStateException("No ShareKey found for item")

        val folderKey = itemRevision.folderId?.let { folderId ->
            folderKeyRepository.getFolderKey(
                userId = userId,
                shareId = shareId,
                folderId = FolderId(folderId)
            )
        }

        return encryptionContextProvider.withEncryptionContextSuspendable {
            itemResponseToEntity(
                userId = share.userId,
                addressId = share.addressId,
                itemRevision = itemRevision,
                share = share,
                shareKeys = shareKeys,
                folderKey = folderKey,
                encryptionContext = this
            )
        }
    }

    override fun observeItems(
        userId: UserId,
        shareSelection: ShareSelection,
        itemState: ItemState?,
        itemTypeFilter: ItemTypeFilter,
        itemFlags: Map<ItemFlag, Boolean>,
        anyFlags: List<ItemFlag>,
        includeHidden: Boolean
    ): Flow<List<Item>> = innerObserveItems(
        shareSelection,
        userId,
        itemState,
        itemTypeFilter,
        itemFlags,
        anyFlags,
        includeHidden
    ).conflate().map { items ->
        encryptionContextProvider.withEncryptionContextSuspendable {
            items.map { item -> item.toDomain(this) }
        }
    }

    override fun observeItemsPaging(
        userId: UserId,
        shareSelection: ShareSelection,
        itemState: ItemState?,
        itemTypeFilter: ItemTypeFilter,
        itemFlags: Map<ItemFlag, Boolean>,
        includeHidden: Boolean
    ): Flow<PagingData<Item>> = innerObserveItemsPaging(
        shareSelection,
        userId,
        itemState,
        itemTypeFilter,
        itemFlags,
        includeHidden
    ).map { pagingData ->
        pagingData.map { itemEntity ->
            encryptionContextProvider.withEncryptionContext {
                itemEntity.toDomain(this)
            }
        }
    }

    override fun observeEncryptedItems(
        userId: UserId,
        shareSelection: ShareSelection,
        itemState: ItemState?,
        itemTypeFilter: ItemTypeFilter,
        itemFlags: Map<ItemFlag, Boolean>,
        includeHidden: Boolean
    ): Flow<List<ItemEncrypted>> = innerObserveItems(
        shareSelection,
        userId,
        itemState,
        itemTypeFilter,
        itemFlags,
        emptyList(),
        includeHidden
    ).conflate().map { items ->
        items.map(ItemEntity::toEncryptedDomain)
    }

    override fun observeEncryptedItemsPaging(
        userId: UserId,
        shareSelection: ShareSelection,
        itemState: ItemState?,
        itemTypeFilter: ItemTypeFilter,
        itemFlags: Map<ItemFlag, Boolean>,
        includeHidden: Boolean
    ): Flow<PagingData<ItemEncrypted>> = innerObserveItemsPaging(
        shareSelection,
        userId,
        itemState,
        itemTypeFilter,
        itemFlags,
        includeHidden
    ).map { pagingData ->
        pagingData.map(ItemEntity::toEncryptedDomain)
    }

    override fun observeSharedByMeEncryptedItems(
        userId: UserId,
        itemState: ItemState?,
        includeHiddenVault: Boolean
    ): Flow<List<ItemEncrypted>> = shareRepository.observeSharedByMeIds(userId, includeHiddenVault)
        .flatMapLatest { shareIds ->
            localItemDataSource.observeItems(
                userId,
                shareIds,
                itemState,
                ItemTypeFilter.All,
                emptyMap()
            )
        }
        .map { itemEntities ->
            itemEntities
                .filter { it.shareCount > 0 }
                .map(ItemEntity::toEncryptedDomain)
        }

    override fun observeSharedByMeEncryptedItemsPaging(
        userId: UserId,
        itemState: ItemState?,
        includeHiddenVault: Boolean
    ): Flow<PagingData<ItemEncrypted>> = shareRepository.observeSharedByMeIds(userId, includeHiddenVault)
        .flatMapLatest { shareIds ->
            localItemDataSource.observeItemsPaging(
                userId,
                shareIds,
                itemState,
                ItemTypeFilter.All,
                emptyMap()
            )
        }
        .map { pagingData ->
            pagingData.map(ItemEntity::toEncryptedDomain)
        }

    override fun observeSharedWithMeEncryptedItems(
        userId: UserId,
        itemState: ItemState?,
        includeHiddenVault: Boolean
    ): Flow<List<ItemEncrypted>> = shareRepository.observeSharedWithMeIds(userId, includeHiddenVault)
        .flatMapLatest { shareIds ->
            localItemDataSource.observeItems(
                userId,
                shareIds,
                itemState,
                ItemTypeFilter.All,
                emptyMap()
            )
        }
        .map { itemEntities ->
            itemEntities.map(ItemEntity::toEncryptedDomain)
        }

    override fun observeSharedWithMeEncryptedItemsPaging(
        userId: UserId,
        itemState: ItemState?,
        includeHiddenVault: Boolean
    ): Flow<PagingData<ItemEncrypted>> = shareRepository.observeSharedWithMeIds(userId, includeHiddenVault)
        .flatMapLatest { shareIds ->
            localItemDataSource.observeItemsPaging(
                userId,
                shareIds,
                itemState,
                ItemTypeFilter.All,
                emptyMap()
            )
        }
        .map { pagingData ->
            pagingData.map(ItemEntity::toEncryptedDomain)
        }

    override fun observeRecentSearchItems(userId: UserId, shareId: ShareId?): Flow<List<ItemEncrypted>> =
        localItemDataSource.observeRecentSearchItems(
            userId = userId,
            shareId = shareId
        ).map { itemEntities ->
            itemEntities.map(ItemEntity::toEncryptedDomain)
        }

    @Suppress("LongParameterList")
    private fun innerObserveItems(
        shareSelection: ShareSelection,
        userId: UserId,
        itemState: ItemState?,
        itemTypeFilter: ItemTypeFilter,
        itemFlags: Map<ItemFlag, Boolean>,
        anyFlags: List<ItemFlag>,
        includeHidden: Boolean
    ) = when (shareSelection) {
        is ShareSelection.Share -> localItemDataSource.observeItems(
            userId = userId,
            shareIds = listOf(shareSelection.shareId),
            itemState = itemState,
            filter = itemTypeFilter,
            itemFlags = itemFlags,
            anyFlags = anyFlags
        )

        is ShareSelection.Shares -> localItemDataSource.observeItems(
            userId = userId,
            shareIds = shareSelection.shareIds,
            itemState = itemState,
            filter = itemTypeFilter,
            itemFlags = itemFlags,
            anyFlags = anyFlags
        )

        is ShareSelection.AllShares -> shareRepository.observeAllUsableShareIds(
            userId,
            includeHidden
        )
            .flatMapLatest {
                localItemDataSource.observeItems(
                    userId = userId,
                    shareIds = it,
                    itemState = itemState,
                    filter = itemTypeFilter,
                    itemFlags = itemFlags,
                    anyFlags = anyFlags
                )
            }

        is ShareSelection.Folder -> localItemDataSource.observeItemsByFolder(
            userId = userId,
            shareId = shareSelection.shareId,
            folderId = shareSelection.folderId,
            itemState = itemState,
            filter = itemTypeFilter,
            itemFlags = itemFlags
        )
    }

    private fun innerObserveItemsPaging(
        shareSelection: ShareSelection,
        userId: UserId,
        itemState: ItemState?,
        itemTypeFilter: ItemTypeFilter,
        itemFlags: Map<ItemFlag, Boolean>,
        includeHidden: Boolean
    ) = when (shareSelection) {
        is ShareSelection.Share -> localItemDataSource.observeItemsPaging(
            userId = userId,
            shareIds = listOf(shareSelection.shareId),
            itemState = itemState,
            filter = itemTypeFilter,
            itemFlags = itemFlags
        )

        is ShareSelection.Shares -> localItemDataSource.observeItemsPaging(
            userId = userId,
            shareIds = shareSelection.shareIds,
            itemState = itemState,
            filter = itemTypeFilter,
            itemFlags = itemFlags
        )

        is ShareSelection.AllShares -> shareRepository.observeAllUsableShareIds(
            userId,
            includeHidden
        )
            .flatMapLatest {
                localItemDataSource.observeItemsPaging(
                    userId = userId,
                    shareIds = it,
                    itemState = itemState,
                    filter = itemTypeFilter,
                    itemFlags = itemFlags
                )
            }

        is ShareSelection.Folder -> {
            // Folders
            localItemDataSource.observeItemsPaging(
                userId = userId,
                shareIds = listOf(shareSelection.shareId),
                itemState = itemState,
                filter = itemTypeFilter,
                itemFlags = itemFlags
            )

        }
    }

    override fun observePinnedItems(
        userId: UserId,
        shareSelection: ShareSelection,
        itemTypeFilter: ItemTypeFilter,
        includeHidden: Boolean
    ): Flow<List<Item>> = when (shareSelection) {
        is ShareSelection.Share -> localItemDataSource.observePinnedItems(
            userId = userId,
            shareIds = listOf(shareSelection.shareId),
            filter = itemTypeFilter
        )

        is ShareSelection.Shares -> localItemDataSource.observePinnedItems(
            userId = userId,
            shareIds = shareSelection.shareIds,
            filter = itemTypeFilter
        )

        is ShareSelection.AllShares -> shareRepository.observeAllUsableShareIds(
            userId,
            includeHidden
        )
            .flatMapLatest {
                localItemDataSource.observePinnedItems(
                    userId = userId,
                    shareIds = it,
                    filter = itemTypeFilter
                )
            }

        is ShareSelection.Folder -> emptyFlow() // observePinnedItems for folders
    }.map { items ->
        encryptionContextProvider.withEncryptionContextSuspendable {
            items.map { item -> item.toDomain(this) }
        }
    }

    override fun observeById(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Flow<Item?> = localItemDataSource.observeItem(userId, shareId, itemId).map { itemEntity ->
        itemEntity?.let {
            encryptionContextProvider.withEncryptionContextSuspendable {
                it.toDomain(this)
            }
        }
    }

    override suspend fun getById(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Item {
        val localItem = localItemDataSource.getById(userId, shareId, itemId)
            ?: throw IllegalStateException("Item not found [shareId=${shareId.id}] [itemId=${itemId.id}]")

        return encryptionContextProvider.withEncryptionContextSuspendable {
            localItem.toDomain(this)
        }
    }

    override suspend fun getByIds(
        userId: UserId,
        shareId: ShareId,
        itemIds: List<ItemId>
    ): List<Item> = localItemDataSource.getByIdList(userId, shareId, itemIds)
        .let { itemEntities ->
            encryptionContextProvider.withEncryptionContextSuspendable {
                itemEntities.map { itemEntity -> itemEntity.toDomain(this) }
            }
        }

    override suspend fun trashItems(userId: UserId, items: Map<ShareId, List<ItemId>>) {
        coroutineScope {
            val results = items.map { entry ->
                async {
                    trashItemsForShare(userId, entry.key, entry.value)
                }
            }.awaitAll().transpose()

            results.onFailure {
                throw it
            }

            // Update item state in search index to Trashed
            items.forEach { (shareId, itemIds) ->
                itemIds.forEach { itemId ->
                    searchIndexRepository.updateItemState(shareId, itemId, ItemState.Trashed)
                }
            }
        }
    }

    private suspend fun trashItemsForShare(
        userId: UserId,
        shareId: ShareId,
        itemIds: List<ItemId>
    ): Result<Unit> = localItemDataSource.getByIdList(userId, shareId, itemIds)
        .chunked(MAX_BATCH_ITEMS_PER_REQUEST)
        .map { items ->
            val body = TrashItemsRequest(
                items.map { TrashItemRevision(it.id, it.revision) }
            )

            safeRunCatching { remoteItemDataSource.sendToTrash(userId, shareId, body) }
                .onSuccess { trashItemsResponse ->
                    handleTrashItemsResponse(items, trashItemsResponse)
                }
                .onFailure {
                    PassLogger.w(TAG, "Error trashing items for share")
                    PassLogger.w(TAG, it)
                }
        }
        .transpose()
        .map { }

    override suspend fun untrashItems(userId: UserId, items: Map<ShareId, List<ItemId>>) {
        coroutineScope {
            val results = items.map { entry ->
                async { untrashItemsForShare(userId, entry.key, entry.value) }
            }.awaitAll().transpose()

            results.onFailure {
                throw it
            }

            // Update item state in search index to Active
            items.forEach { (shareId, itemIds) ->
                itemIds.forEach { itemId ->
                    searchIndexRepository.updateItemState(shareId, itemId, ItemState.Active)
                }
            }
        }
    }

    private suspend fun untrashItemsForShare(
        userId: UserId,
        shareId: ShareId,
        itemIds: List<ItemId>
    ): Result<Unit> = localItemDataSource.getByIdList(userId, shareId, itemIds)
        .chunked(MAX_BATCH_ITEMS_PER_REQUEST)
        .map { items ->
            val body = TrashItemsRequest(
                items.map { TrashItemRevision(it.id, it.revision) }
            )

            safeRunCatching { remoteItemDataSource.untrash(userId, shareId, body) }
                .onSuccess { trashItemsResponse ->
                    handleTrashItemsResponse(items, trashItemsResponse)
                }
                .onFailure {
                    PassLogger.w(TAG, "Error untrashing items for share")
                    PassLogger.w(TAG, it)
                }
        }
        .transpose()
        .map { }

    private suspend fun handleTrashItemsResponse(
        itemEntities: List<ItemEntity>,
        trashItemsResponse: TrashItemsResponse
    ) {
        itemEntities
            .associateBy { itemEntity ->
                itemEntity.id
            }
            .let { itemEntitiesMap ->
                trashItemsResponse.items.mapNotNull { trashItemRevision ->
                    itemEntitiesMap[trashItemRevision.itemId]?.copy(
                        createTime = trashItemRevision.revisionTime.toLong(),
                        modifyTime = trashItemRevision.modifyTime.toLong(),
                        revision = trashItemRevision.revision,
                        state = trashItemRevision.state,
                        flags = trashItemRevision.flags
                    )
                }
            }
            .also { updatedItemEntities ->
                localItemDataSource.upsertItems(updatedItemEntities)
            }
    }

    override suspend fun clearTrash(userId: UserId, includeHidden: Boolean) {
        coroutineScope {
            val shareIds =
                shareRepository.observeAllUsableShareIds(userId, includeHidden).firstOrNull()
                    ?: emptyList()
            val trashedItems = localItemDataSource.getTrashedItems(userId, shareIds)
            val trashedPerShare = trashedItems.groupBy { it.shareId }
            val results = trashedPerShare
                .map { entry ->
                    async {
                        clearItemsForShare(
                            shareId = ShareId(entry.key),
                            shareItems = entry.value,
                            userId = userId
                        )
                    }
                }
                .awaitAll()
                .transpose()

            results.onFailure {
                throw it
            }
        }
    }

    override suspend fun restoreItems(userId: UserId, includeHidden: Boolean) {
        coroutineScope {
            val shareIds =
                shareRepository.observeAllUsableShareIds(userId, includeHidden).firstOrNull()
                    ?: emptyList()
            val trashedItems = localItemDataSource.getTrashedItems(userId, shareIds)
            val trashedPerShare = trashedItems.groupBy { it.shareId }
            val results = trashedPerShare
                .map { entry ->
                    async {
                        restoreItemsForShare(
                            userId = userId,
                            shareId = ShareId(entry.key),
                            shareItems = entry.value
                        )
                    }
                }
                .awaitAll()
                .transpose()

            results.onFailure {
                throw it
            }
        }
    }

    override suspend fun deleteItems(userId: UserId, items: Map<ShareId, List<ItemId>>) {
        coroutineScope {
            val results = items.map { entry ->
                async { deleteItemsForShare(userId, entry.key, entry.value) }
            }.awaitAll().transpose()

            results.onFailure {
                throw it
            }

            // Remove items from search index (safety measure, should already be removed when trashed)
            items.forEach { (shareId, itemIds) ->
                itemIds.forEach { itemId ->
                    searchIndexRepository.removeItemFromIndex(shareId, itemId)
                }
            }
        }
    }

    override suspend fun deleteLocalItems(userId: UserId, items: Map<ShareId, List<ItemId>>) {
        database.inTransaction("deleteLocalItems") {
            items.forEach { (shareId, list) ->
                localItemDataSource.delete(userId, shareId, list)
            }
        }
        items.forEach { (shareId, list) -> removeEventItemsFromIndex(shareId, list) }
    }

    private suspend fun deleteItemsForShare(
        userId: UserId,
        shareId: ShareId,
        itemIds: List<ItemId>
    ): Result<Unit> = localItemDataSource.getByIdList(userId, shareId, itemIds)
        .chunked(MAX_BATCH_ITEMS_PER_REQUEST)
        .map { items ->
            val body = TrashItemsRequest(
                items.map { TrashItemRevision(it.id, it.revision) }
            )

            safeRunCatching { remoteItemDataSource.delete(userId, shareId, body) }
                .onSuccess {
                    localItemDataSource.delete(userId, shareId, items.map { ItemId(it.id) })
                }
                .onFailure {
                    PassLogger.w(TAG, "Error deleting item")
                    PassLogger.w(TAG, it)
                }
        }
        .transpose()
        .map { }

    @Suppress("ReturnCount")
    override suspend fun addPackageAndUrlToItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        packageInfo: Option<PackageInfo>,
        url: Option<String>
    ): Item {
        val itemEntity = localItemDataSource.getById(userId, shareId, itemId)
            ?: throw ItemNotFoundError(itemId, shareId)

        val (item, itemProto) = encryptionContextProvider.withEncryptionContextSuspendable {
            val item = itemEntity.toDomain(this)
            val itemContents = decrypt(item.content)
            item to ItemV1.Item.parseFrom(itemContents)
        }

        val (needsToUpdate, updatedContents) = updateItemContents(
            item,
            itemProto,
            packageInfo,
            url
        )

        if (!needsToUpdate) {
            PassLogger.i(TAG, "Did not need to perform any update")
            return item
        }

        val share = shareRepository.getById(userId, shareId)
        return performUpdate(
            userId,
            share,
            item,
            updatedContents,
            isDomainMatchingEnabled()
        )
    }

    override suspend fun downloadItemsAndObserveProgress(
        userId: UserId,
        shareId: ShareId,
        eventToken: EventToken?,
        onProgress: suspend (VaultProgress) -> Unit
    ): List<ItemRevision> {
        val items = mutableListOf<ItemRevision>()
        var sinceToken: String? = null
        var itemsRetrieved = 0

        safeRunCatching {
            while (currentCoroutineContext().isActive) {
                val page = remoteItemDataSource.getItemsPage(
                    userId = userId,
                    shareId = shareId,
                    sinceToken = sinceToken
                )

                items.addAll(page.items)
                itemsRetrieved += page.items.size

                onProgress(
                    VaultProgress(
                        total = page.total,
                        current = itemsRetrieved
                    )
                )

                if (page.lastToken == null) {
                    break
                }

                sinceToken = page.lastToken
            }
        }.onFailure { error ->
            PassLogger.w(TAG, "Error refreshing and observing items sync progress")
            PassLogger.w(TAG, error)
            throw error
        }

        return items
    }

    override suspend fun setShareItems(
        userId: UserId,
        items: Map<ShareId, List<ItemRevision>>,
        onProgress: suspend (VaultProgress) -> Unit
    ): SetShareItemsResult {
        if (items.isEmpty()) return SetShareItemsResult(emptySet(), emptyMap())

        val plans: List<Pair<ShareId, Result<SetShareItemsPlan>>> = coroutineScope {
            items.map { (shareId, revisions) ->
                async {
                    shareId to calculatePlanForSetShareItems(userId, shareId, revisions)
                }
            }.awaitAll()
        }

        val (successes, failures) = plans.partition { (_, result) -> result.isSuccess }
        val failedShareIds: MutableSet<ShareId> = failures
            .map { (shareId, _) -> shareId }
            .toMutableSet()

        if (failures.isNotEmpty()) {
            failures.forEach { (shareId, result) ->
                result.exceptionOrNull()?.let {
                    PassLogger.w(TAG, "Error calculating plan for setShareItems (shareId: ${shareId.id})")
                    PassLogger.w(TAG, it)
                }
            }
        }

        val successPlans: List<Pair<ShareId, SetShareItemsPlan>> = successes.mapNotNull { (shareId, result) ->
            result.getOrNull()?.let { shareId to it }
        }
        failedShareIds += successPlans
            .filter { (_, plan) -> plan.hasFailedItems }
            .map { (shareId, _) -> shareId }

        val insertedCountByShare = successPlans.associate { (shareId, plan) -> shareId to plan.itemsToUpsert.size }

        val itemsToUpsert = successPlans.flatMap { (_, plan) -> plan.itemsToUpsert }
        val itemsToDelete = successPlans.map { (_, plan) -> plan.itemsToDelete }

        persistShareItemsData(userId, itemsToUpsert, itemsToDelete, onProgress)

        if (failedShareIds.isNotEmpty()) {
            PassLogger.w(
                TAG,
                "setShareItems finished with failed shares: " +
                    failedShareIds.joinToString(separator = ",") { it.id }
            )
        }

        return SetShareItemsResult(failedShareIds, insertedCountByShare)
    }

    override suspend fun applyPendingEvent(event: ItemPendingEvent) = with(event) {
        if (!hasPendingItemRevisions) return

        val share = shareRepository.getById(userId, shareId)
        val shareKeys = shareKeyRepository.getShareKeys(
            userId = userId,
            addressId = addressId,
            shareId = shareId,
            groupEmail = share.groupEmail
        ).first()

        val pendingRevisions = pendingItemRevisions.map { it.toItemRevision().toDomain() }
        val folderIds = pendingRevisions.mapNotNull { it.folderId?.let(::FolderId) }.distinct()

        val folderKeysMap: Map<FolderId, FolderKey> = folderKeyRepository.getFolderKeys(
            userId = userId,
            shareId = shareId,
            folderIds = folderIds
        )

        val items = decryptPendingItemRevisions(
            shareId = shareId,
            pendingRevisions = pendingRevisions,
            userId = userId,
            addressId = addressId,
            share = share,
            shareKeys = shareKeys,
            folderKeysMap = folderKeysMap
        )
        localItemDataSource.upsertItems(items)
        indexEventItems(userId, items)
    }

    private suspend fun indexEventItems(userId: UserId, items: List<ItemEntity>) {
        safeRunCatching {
            searchIndexRepository.indexItems(userId, items.map { ShareId(it.shareId) to ItemId(it.id) })
        }.onFailure { PassLogger.w(TAG, "Failed to index synced items: ${it.message}") }
    }

    private suspend fun removeEventItemsFromIndex(shareId: ShareId, itemIds: List<ItemId>) {
        safeRunCatching {
            itemIds.forEach { searchIndexRepository.removeItemFromIndex(shareId, it) }
        }.onFailure { PassLogger.w(TAG, "Failed to remove synced items from index: ${it.message}") }
    }

    override suspend fun purgePendingEvent(event: ItemPendingEvent) = with(event) {
        if (!hasDeletedItemIds) return false

        val deleted = localItemDataSource.delete(userId, shareId, deletedItemIds)
        removeEventItemsFromIndex(shareId, deletedItemIds)
        deleted
    }

    override fun observeItemCountSummary(
        userId: UserId,
        shareIds: List<ShareId>,
        itemState: ItemState?,
        onlyShared: Boolean,
        applyItemStateToSharedItems: Boolean,
        includeHiddenVault: Boolean
    ): Flow<ItemCountSummary> = localItemDataSource.observeItemCountSummary(
        userId = userId,
        shareIds = shareIds,
        itemState = itemState,
        onlyShared = onlyShared,
        applyItemStateToSharedItems = applyItemStateToSharedItems,
        includeHiddenVault = includeHiddenVault
    )

    override suspend fun updateItemLastUsed(vaultId: VaultId, itemId: ItemId) {
        val readyUsers = accountManager.getAccounts(AccountState.Ready).firstOrNull() ?: emptyList()
        PassLogger.i(TAG, "Updating last used time [vaultId=$vaultId][itemId=$itemId]")

        val now = TimeUtil.getNowUtc()
        val items =
            localItemDataSource.getByVaultIdAndItemId(readyUsers.map { it.userId }, vaultId, itemId)
        items.forEach {
            localItemDataSource.updateLastUsedTime(ShareId(it.shareId), ItemId(it.id), now)
            remoteItemDataSource.updateLastUsedTime(
                UserId(it.userId),
                ShareId(it.shareId),
                ItemId(it.id),
                now
            )
            PassLogger.i(TAG, "Updated last used time [shareId=${it.shareId}][itemId=$itemId]")
        }
    }

    override fun observeItemCount(shareIds: List<ShareId>): Flow<Map<ShareId, ShareItemCount>> =
        localItemDataSource.observeItemCount(shareIds)

    override suspend fun migrateItems(
        userId: UserId,
        items: Map<ShareId, List<ItemId>>,
        destination: Share,
        destinationFolderId: FolderId?
    ): MigrateItemsResult = coroutineScope {
        val destinationKey = shareKeyRepository.getLatestKeyForShare(destination.id).first()
        val migratedRevisions: List<Result<List<ItemEntity>>> = items.map { (shareId, items) ->
            async {
                safeRunCatching {
                    val shareItems = localItemDataSource.getByIdList(userId, shareId, items)
                    migrateItemsForShare(
                        userId = userId,
                        source = shareId,
                        destination = destination.id,
                        destinationKey = destinationKey,
                        items = shareItems,
                        destinationFolderId = destinationFolderId
                    )
                }
            }
        }.awaitAll()

        val (successes, failures) = migratedRevisions.partition { it.isSuccess }
        when {
            // Happy path
            successes.isNotEmpty() && failures.isEmpty() -> {
                val migrated = successes.mapNotNull { it.getOrNull() }.flatten()
                val migratedItemsMapped =
                    encryptionContextProvider.withEncryptionContextSuspendable {
                        migrated.map { it.toDomain(this) }
                    }

                MigrateItemsResult.AllMigrated(migratedItemsMapped)
            }

            // Some succeeded
            successes.isNotEmpty() -> {
                val firstFailure = failures.first().exceptionOrNull()
                    ?: IllegalStateException("Error migrating items. Could not migrate some")
                PassLogger.w(TAG, "Error migrating items. Could not migrate some")
                PassLogger.w(TAG, firstFailure)

                val migrated = successes.mapNotNull { it.getOrNull() }.flatten()
                val migratedItemsMapped =
                    encryptionContextProvider.withEncryptionContextSuspendable {
                        migrated.map { it.toDomain(this) }
                    }
                MigrateItemsResult.SomeMigrated(migratedItemsMapped)
            }

            // None succeeded
            failures.isNotEmpty() -> {
                val firstFailure = failures.first().exceptionOrNull()
                    ?: IllegalStateException("Error migrating items. Could not migrate any")
                PassLogger.w(TAG, "Error migrating items. Could not migrate any")
                PassLogger.w(TAG, firstFailure)
                MigrateItemsResult.NoneMigrated(firstFailure)
            }

            // Should never happen
            else -> MigrateItemsResult.AllMigrated(emptyList())

        }
    }

    override suspend fun migrateAllVaultItems(
        userId: UserId,
        source: ShareId,
        destination: ShareId,
        destinationFolderId: FolderId?
    ) {
        val items = localItemDataSource.observeItems(
            userId = userId,
            shareIds = listOf(source),
            itemState = ItemState.Active,
            filter = ItemTypeFilter.All,
            itemFlags = emptyMap()
        ).first()

        if (source == destination) {
            val itemIds = items.map { ItemId(it.id) }
            moveItemsInsideShare(
                userId = userId,
                shareId = source,
                folderId = destinationFolderId,
                itemIds = itemIds
            )
            return
        }

        val destinationKey = shareKeyRepository.getLatestKeyForShare(destination).first()
        migrateItemsForShare(
            userId = userId,
            source = source,
            destination = destination,
            destinationKey = destinationKey,
            items = items,
            destinationFolderId = destinationFolderId
        )
    }

    @Suppress("LongMethod")
    override suspend fun moveItemsInsideShare(
        userId: UserId,
        shareId: ShareId,
        folderId: FolderId?,
        itemIds: List<ItemId>
    ) {
        val userAddress = shareRepository.getAddressForShareId(userId, shareId)
        val destinationKey = if (folderId != null) {
            folderKeyRepository.getFolderKey(userId, shareId, folderId)
                ?: throw IllegalStateException("FolderKey not found for folderId=${folderId.id}")
        } else {
            shareKeyRepository.getLatestKeyForShare(shareId).first()
        }

        val items = localItemDataSource.getByIdList(userId, shareId, itemIds)

        val uniqueSourceFolderIds = items.mapNotNull { it.folderId?.let(::FolderId) }.distinct()
        val sourceFolderKeys: Map<FolderId, FolderKey> = folderKeyRepository.getFolderKeys(
            userId = userId,
            shareId = shareId,
            folderIds = uniqueSourceFolderIds
        )

        val moveItems = items.map { item ->
            val currentFolderKey = resolveSourceFolderKey(
                sourceFolderId = item.folderId,
                sourceFolderKeys = sourceFolderKeys
            )
            val (_, itemKey) = getShareAndItemKey(
                userAddress = userAddress,
                shareId = shareId,
                itemId = ItemId(item.id),
                decryptionKeyOverride = currentFolderKey
            )
            val reencryptedKeys = migrateItem.migrate(
                destinationKey = destinationKey,
                itemKeys = listOf(ItemKeyWithRotation(itemKey.key, itemKey.rotation))
            )
            MigrateItemsBody(
                itemId = item.id,
                itemKeys = reencryptedKeys.map { k ->
                    ItemKeyBody(
                        key = Base64.encodeBase64String(k.itemKey.array),
                        keyRotation = k.keyRotation
                    )
                }
            )
        }

        val request = MoveItemsToFolderRequest(
            folderId = folderId?.id,
            items = moveItems
        )

        val response = remoteItemDataSource.moveItemsToFolder(userId, shareId, request)

        val reencryptedKeyByItemId = moveItems.associateBy { it.itemId }
        val entityByItemId = items.associateBy { it.id }

        val updatedEntities = response.map { moveRevision ->
            val entity = entityByItemId[moveRevision.itemId]
                ?: throw IllegalStateException("ItemEntity not found for itemId=${moveRevision.itemId}")
            val reencryptedItemKey = reencryptedKeyByItemId[moveRevision.itemId]
                ?.itemKeys
                ?.firstOrNull()
            entity.copy(
                folderId = moveRevision.folderId,
                revision = moveRevision.revision,
                state = moveRevision.state,
                flags = moveRevision.flags,
                modifyTime = moveRevision.modifyTime,
                key = reencryptedItemKey?.key ?: entity.key,
                keyRotation = reencryptedItemKey?.keyRotation ?: entity.keyRotation
            )
        }

        localItemDataSource.upsertItems(updatedEntities)

        // Refresh the search index so the new folderId is reflected in paginated/search results
        searchIndexRepository.indexItems(userId, itemIds.map { shareId to it })
    }

    override suspend fun getItemByAliasEmail(userId: UserId, aliasEmail: String): Item? {
        val item = localItemDataSource.getItemByAliasEmail(userId, aliasEmail) ?: return null

        return encryptionContextProvider.withEncryptionContextSuspendable {
            item.toDomain(this)
        }
    }

    override suspend fun pinItems(items: List<Pair<ShareId, ItemId>>): PinItemsResult =
        handleItemPinning(items, remoteItemDataSource::pinItem)

    override suspend fun unpinItems(items: List<Pair<ShareId, ItemId>>): PinItemsResult =
        handleItemPinning(items, remoteItemDataSource::unpinItem)

    override suspend fun addPasskeyToItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        passkey: Passkey
    ) {
        val share = shareRepository.getById(userId, shareId)
        val itemEntity = localItemDataSource.getById(userId, shareId, itemId)
            ?: throw ItemNotFoundError(itemId, shareId)

        val (itemContents, item) = encryptionContextProvider.withEncryptionContextSuspendable {
            decrypt(itemEntity.encryptedContent) to itemEntity.toDomain(this)
        }

        val parsed = ItemV1.Item.parseFrom(itemContents)
        val updatedContents = parsed.withPasskey(passkey)

        performUpdate(
            userId = userId,
            share = share,
            item = item,
            itemContents = updatedContents,
            isDomainMatchingEnabled = isDomainMatchingEnabled()
        )
    }

    override suspend fun findUserId(shareId: ShareId, itemId: ItemId): Option<UserId> =
        localItemDataSource.findUserId(shareId, itemId)

    override suspend fun deleteItemRevisions(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ) {
        val revision = remoteItemDataSource.deleteItemRevisions(userId, shareId, itemId)
        val share = shareRepository.getById(userId, shareId)
        val entity = createItemEntity(userId, revision, share)
        localItemDataSource.upsertItem(entity)
    }

    private suspend fun calculatePlanForSetShareItems(
        userId: UserId,
        shareId: ShareId,
        revisions: List<ItemRevision>
    ) = safeRunCatching {
        val share = shareRepository.getById(userId, shareId)
        val localItemsForShare = localItemDataSource.observeItems(
            userId = userId,
            shareIds = listOf(shareId),
            itemState = null,
            filter = ItemTypeFilter.All,
            itemFlags = emptyMap()
        ).first()

        val itemsNotPresentInRemote = localItemsForShare
            // Filter out items that are not present in the remote
            .filter { localItem -> revisions.none { it.itemId == localItem.id } }
            // Keep only the ItemIDs
            .map { ItemId(it.id) }

        val shareKeys = shareKeyRepository.getShareKeys(
            userId = userId,
            addressId = share.addressId,
            shareId = shareId,
            groupEmail = share.groupEmail
        ).first()

        val folderIds = revisions.mapNotNull { it.folderId?.let(::FolderId) }.distinct()

        val folderKeysMap: Map<FolderId, FolderKey> = folderKeyRepository.getFolderKeys(
            userId = userId,
            shareId = shareId,
            folderIds = folderIds
        )

        val (itemsToUpsert, hasFailedItems) = decryptItemRevisions(
            shareId = shareId,
            revisions = revisions,
            userId = userId,
            addressId = share.addressId,
            share = share,
            shareKeys = shareKeys,
            folderKeysMap = folderKeysMap
        )
        SetShareItemsPlan(
            itemsToDelete = shareId to itemsNotPresentInRemote,
            itemsToUpsert = itemsToUpsert,
            hasFailedItems = hasFailedItems
        )
    }.onFailure {
        PassLogger.w(TAG, "Error calculating plan for setShareItems (shareId: ${shareId.id})")
        PassLogger.w(TAG, it)
    }


    private suspend fun handleItemPinning(
        items: List<Pair<ShareId, ItemId>>,
        block: suspend (userId: UserId, shareId: ShareId, itemId: ItemId) -> ItemRevision
    ): PinItemsResult = coroutineScope {
        val userId = requireNotNull(accountManager.getPrimaryUserId().first())

        val pinResults: List<Result<Pair<ShareId, ItemRevision>>> = items.map { (shareId, itemId) ->
            async { safeRunCatching { shareId to block(userId, shareId, itemId) } }
        }.awaitAll().toList()

        generateItemPinningResponse(userId, items, pinResults)
    }

    private suspend fun generateItemPinningResponse(
        userId: UserId,
        items: List<Pair<ShareId, ItemId>>,
        pinResults: List<Result<Pair<ShareId, ItemRevision>>>
    ): PinItemsResult = coroutineScope {

        // Obtain all the shares for the items being pinned
        val allShareIds = items.map { it.first }.toSet()
        val shares: Map<ShareId, Share> = allShareIds.map {
            async { it to shareRepository.getById(userId, it) }
        }.awaitAll().toMap()

        // Function that mapps from a given ItemRevision to an ItemEntity
        val revisionToEntity: suspend (ShareId, ItemRevision) -> ItemEntity = { shareId, revision ->
            val share = shares[shareId] ?: throw IllegalStateException("Could not find share")
            createItemEntity(userId, revision, share)
        }

        val (successes, failures) = pinResults.partition { it.isSuccess }
        when {
            // Happy path
            successes.isNotEmpty() && failures.isEmpty() -> {
                val pinnedRevisions: List<Pair<ShareId, ItemRevision>> = successes.mapNotNull {
                    it.getOrNull()
                }

                val pinned: List<ItemEntity> = database.inTransaction {
                    pinnedRevisions.map { (shareId, revision) ->
                        revisionToEntity(shareId, revision)
                    }
                }
                localItemDataSource.upsertItems(pinned)

                val pinnedItemsMapped = encryptionContextProvider.withEncryptionContextSuspendable {
                    pinned.map { it.toDomain(this) }
                }

                PinItemsResult.AllPinned(pinnedItemsMapped)
            }

            // Some succeeded
            successes.isNotEmpty() -> {
                val firstFailure = failures.first().exceptionOrNull()
                    ?: IllegalStateException("Error pinning items. Could not pin some")
                PassLogger.w(TAG, "Error pinning items. Could not pin some")
                PassLogger.w(TAG, firstFailure)

                val pinnedRevisions: List<Pair<ShareId, ItemRevision>> = successes.mapNotNull {
                    it.getOrNull()
                }

                val pinned: List<ItemEntity> = database.inTransaction {
                    pinnedRevisions.map { (shareId, revision) ->
                        revisionToEntity(shareId, revision)
                    }
                }
                localItemDataSource.upsertItems(pinned)

                val pinnedItemsMapped = encryptionContextProvider.withEncryptionContextSuspendable {
                    pinned.map { it.toDomain(this) }
                }

                PinItemsResult.SomePinned(pinnedItemsMapped)
            }

            // None succeeded
            failures.isNotEmpty() -> {
                val firstFailure = failures.first().exceptionOrNull()
                    ?: IllegalStateException("Error pinning items. Could not pin any")
                PassLogger.w(TAG, "Error pinning items. Could not pin any")
                PassLogger.w(TAG, firstFailure)
                PinItemsResult.NonePinned(firstFailure)
            }

            // Should never happen
            else -> PinItemsResult.AllPinned(emptyList())
        }
    }

    override suspend fun getItemRevisions(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ) = remoteItemDataSource.fetchItemRevisions(userId, shareId, itemId)

    private suspend fun createItemEntity(
        userId: UserId,
        itemRevision: ItemRevision,
        share: Share
    ) = withUserAddress(userId) { userAddress ->
        itemResponseToEntity(
            userAddress.userId,
            userAddress.addressId,
            itemRevision,
            share,
            listOf(shareKeyRepository.getLatestKeyForShare(share.id).first())
        )
    }

    private suspend fun migrateItemsForShare(
        userId: UserId,
        source: ShareId,
        destination: ShareId,
        destinationKey: ShareKey,
        items: List<ItemEntity>,
        destinationFolderId: FolderId? = null
    ): List<ItemEntity> = withContext(Dispatchers.Default) {
        val userAddress = shareRepository.getAddressForShareId(userId, source)
        items.chunked(MAX_BATCH_ITEMS_PER_REQUEST).map { chunk ->
            async {
                migrateChunk(
                    userAddress,
                    source,
                    destination,
                    destinationKey,
                    chunk,
                    destinationFolderId
                )
            }
        }.awaitAll().flatten()
    }


    @Suppress("LongMethod")
    private suspend fun migrateChunk(
        userAddress: UserAddress,
        source: ShareId,
        destination: ShareId,
        destinationKey: ShareKey,
        chunk: List<ItemEntity>,
        destinationFolderId: FolderId? = null
    ): List<ItemEntity> {
        val destFolderKey: FolderKey? = destinationFolderId?.let {
            folderKeyRepository.getFolderKey(userAddress.userId, destination, it)
                ?: throw IllegalStateException("FolderKey not found for destination folderId=${it.id}")
        }
        val encryptionKey: InviteKey = destFolderKey ?: destinationKey

        // Pre-fetch folder keys for source items that are currently in a folder.
        val uniqueSourceFolderIds = chunk.mapNotNull { it.folderId?.let(::FolderId) }.distinct()
        val sourceFolderKeys: Map<FolderId, FolderKey> = folderKeyRepository.getFolderKeys(
            userId = userAddress.userId,
            shareId = source,
            folderIds = uniqueSourceFolderIds
        )

        val migrations = chunk.map { item ->
            val currentFolderKey = resolveSourceFolderKey(
                sourceFolderId = item.folderId,
                sourceFolderKeys = sourceFolderKeys
            )
            val (_, itemKey) = getShareAndItemKey(
                userAddress = userAddress,
                shareId = source,
                itemId = ItemId(item.id),
                decryptionKeyOverride = currentFolderKey
            )
            val encryptedMigrateItemBody = migrateItem.migrate(
                encryptionKey,
                listOf(ItemKeyWithRotation(itemKey.key, itemKey.rotation))
            )
            MigrateItemsBody(
                itemId = item.id,
                destinationFolderId = destinationFolderId?.id,
                itemKeys = encryptedMigrateItemBody.map {
                    ItemKeyBody(
                        key = Base64.encodeBase64String(it.itemKey.array),
                        keyRotation = it.keyRotation
                    )
                }
            )
        }

        val body = MigrateItemsRequest(
            shareId = destination.id,
            items = migrations
        )

        val destinationShare = shareRepository.getById(userAddress.userId, destination)

        val res = remoteItemDataSource.migrateItems(userAddress.userId, source, body)

        val folderIds = res.mapNotNull { it.folderId?.let(::FolderId) }.distinct()

        val folderKeysMap: Map<FolderId, FolderKey> = folderKeyRepository.getFolderKeys(
            userId = userAddress.userId,
            shareId = destination,
            folderIds = folderIds
        )

        val resAsEntities = encryptionContextProvider.withEncryptionContextSuspendable {
            res.map { revision ->
                val folderKey: FolderKey? = revision.folderId?.let { folderId ->
                    folderKeysMap[FolderId(folderId)]
                        ?: throw IllegalStateException("FolderKey not found in the database")
                }
                itemResponseToEntity(
                    userId = userAddress.userId,
                    addressId = userAddress.addressId,
                    itemRevision = revision,
                    share = destinationShare,
                    shareKeys = listOf(destinationKey),
                    folderKey = folderKey,
                    encryptionContext = this
                )
            }
        }

        val itemIdsToDelete = chunk.map { ItemId(it.id) }
        database.inTransaction("migrateChunk") {
            localItemDataSource.upsertItems(resAsEntities)
            localItemDataSource.delete(userAddress.userId, source, itemIdsToDelete)
        }

        // Update search index: remove from source, add to destination
        itemIdsToDelete.forEach { itemId ->
            searchIndexRepository.removeItemFromIndex(source, itemId)
        }
        searchIndexRepository.indexItems(
            userAddress.userId,
            resAsEntities.map { destination to ItemId(it.id) }
        )

        return resAsEntities
    }

    private suspend fun restoreItemsForShare(
        userId: UserId,
        shareId: ShareId,
        shareItems: List<ItemEntity>
    ): Result<Unit> = shareItems.chunked(MAX_BATCH_ITEMS_PER_REQUEST).map { items ->
        val body = TrashItemsRequest(
            items.map {
                TrashItemRevision(
                    it.id,
                    it.revision
                )
            }
        )
        runCatching { remoteItemDataSource.untrash(userId, shareId, body) }
            .onSuccess { trashItemsResponse ->
                handleTrashItemsResponse(items, trashItemsResponse)
            }
            .onFailure { error ->
                PassLogger.w(TAG, "Error restoring items for share")
                PassLogger.w(TAG, error)
            }
    }.transpose().map { }

    private suspend fun clearItemsForShare(
        userId: UserId,
        shareId: ShareId,
        shareItems: List<ItemEntity>
    ): Result<Unit> = shareItems.chunked(MAX_BATCH_ITEMS_PER_REQUEST).map { items ->
        val body =
            TrashItemsRequest(
                items.map {
                    TrashItemRevision(
                        it.id,
                        it.revision
                    )
                }
            )

        runCatching { remoteItemDataSource.delete(userId, shareId, body) }
            .onSuccess {
                localItemDataSource.delete(
                    userId,
                    shareId,
                    items.map { ItemId(it.id) }
                )
            }
            .onFailure {
                PassLogger.w(TAG, "Error clearing items for share")
                PassLogger.w(TAG, it)
            }
    }.transpose().map { }

    private fun updateItemContents(
        item: Item,
        itemProto: ItemV1.Item,
        packageInfoOption: Option<PackageInfo>,
        url: Option<String>
    ): Pair<Boolean, ItemV1.Item> {
        var needsToUpdate = false

        val itemContentsWithPackageName = when (packageInfoOption) {
            None -> itemProto
            is Some -> {
                if (itemProto.hasPackageName(packageInfoOption.value.packageName)) {
                    PassLogger.i(
                        TAG,
                        "Item already has this package name " +
                            "[shareId=${item.shareId}] [itemId=${item.id}] [packageName=$packageInfoOption]"
                    )
                    itemProto
                } else {
                    needsToUpdate = true
                    itemProto.with(packageInfoOption.value)
                }
            }
        }

        val updatedContents = when (url) {
            None -> itemContentsWithPackageName
            is Some -> when (val loginItem = item.itemType) {
                is ItemType.Login -> {
                    if (loginItem.hasWebsite(url.value)) {
                        // Item already has the URL, not doing anything
                        PassLogger.i(
                            TAG,
                            "Item already has the URL in the websites list, not performing any update"
                        )
                        itemContentsWithPackageName
                    } else {
                        // Item does not have the URL, adding it
                        needsToUpdate = true
                        itemContentsWithPackageName.withUrl(url.value)
                    }
                }

                else -> {
                    PassLogger.i(
                        TAG,
                        "Not performing any update, as we can only add urls to ItemType.Login"
                    )
                    itemContentsWithPackageName
                }
            }
        }

        return needsToUpdate to updatedContents
    }

    private suspend fun performUpdate(
        userId: UserId,
        share: Share,
        item: Item,
        itemContents: ItemV1.Item,
        isDomainMatchingEnabled: Boolean
    ): Item = withUserAddress(userId) { userAddress ->
        val folderKeyOverride = item.folderId?.let { folderId ->
            folderKeyRepository.getFolderKey(
                userId = userId,
                shareId = share.id,
                folderId = folderId
            ) ?: throw IllegalStateException("FolderKey not found for folderId=${folderId.id}")
        }
        val (shareKey, itemKey) = getShareAndItemKey(
            userAddress = userAddress,
            shareId = share.id,
            itemId = item.id,
            decryptionKeyOverride = folderKeyOverride
        )
        val body = updateItem.createRequest(
            itemKey,
            itemContents,
            item.revision,
            isDomainMatchingEnabled
        )
        val itemResponse = remoteItemDataSource.updateItem(
            userId = userId,
            shareId = share.id,
            itemId = item.id,
            body = body.toRequest()
        )
        val entity = itemResponseToEntity(
            userAddress.userId,
            userAddress.addressId,
            itemResponse,
            share,
            listOf(shareKey)
        )
        localItemDataSource.upsertItem(entity)

        // Re-index the updated item for search
        searchIndexRepository.indexItem(userId, share.id, ItemId(entity.id))

        encryptionContextProvider.withEncryptionContextSuspendable {
            entity.toDomain(this)
        }
    }

    private fun resolveSourceFolderKey(
        sourceFolderId: String?,
        sourceFolderKeys: Map<FolderId, FolderKey>
    ): FolderKey? = sourceFolderId?.let {
        sourceFolderKeys[FolderId(it)]
            ?: throw IllegalStateException("FolderKey not found for source folderId=$it")
    }

    private suspend fun itemResponseToEntity(
        userId: UserId,
        addressId: AddressId,
        itemRevision: ItemRevision,
        share: Share,
        shareKeys: List<ShareKey>
    ): ItemEntity = encryptionContextProvider.withEncryptionContextSuspendable {
        val folderKey: FolderKey? = itemRevision.folderId?.let {
            folderKeyRepository.getFolderKey(
                userId = userId,
                shareId = share.id,
                folderId = FolderId(it)
            )
        }

        itemResponseToEntity(
            userId = userId,
            addressId = addressId,
            itemRevision = itemRevision,
            share = share,
            shareKeys = shareKeys,
            folderKey = folderKey,
            encryptionContext = this
        )
    }

    private fun itemResponseToEntity(
        userId: UserId,
        addressId: AddressId,
        itemRevision: ItemRevision,
        share: Share,
        shareKeys: List<ShareKey>,
        folderKey: FolderKey?,
        encryptionContext: EncryptionContext
    ): ItemEntity {
        val output = openItem.open(
            response = itemRevision.toCrypto(),
            share = share,
            shareKeys = shareKeys,
            folderKey = folderKey,
            encryptionContext = encryptionContext
        )
        val hasTotp = output.item.hasTotp(encryptionContext)
        return ItemEntity(
            id = itemRevision.itemId,
            userId = userId.id,
            addressId = addressId.id,
            shareId = share.id.id,
            revision = itemRevision.revision,
            contentFormatVersion = itemRevision.contentFormatVersion,
            keyRotation = itemRevision.keyRotation,
            content = itemRevision.content,
            key = itemRevision.itemKey,
            state = itemRevision.state,
            itemType = output.item.itemType.category.value,
            aliasEmail = itemRevision.aliasEmail,
            createTime = itemRevision.createTime,
            modifyTime = itemRevision.modifyTime,
            lastUsedTime = itemRevision.lastUseTime,
            hasTotp = hasTotp,
            isPinned = itemRevision.isPinned,
            pinTime = itemRevision.pinTime,
            hasPasskeys = output.item.hasPasskeys,
            flags = itemRevision.flags,
            shareCount = itemRevision.shareCount,
            folderId = itemRevision.folderId,
            encryptedTitle = output.item.title,
            encryptedNote = output.item.note,
            encryptedContent = output.item.content,
            encryptedKey = output.itemKey
        )
    }

    private suspend fun persistShareItemsData(
        userId: UserId,
        itemsToUpsert: List<ItemEntity>,
        itemsToDelete: List<Pair<ShareId, List<ItemId>>>,
        onProgress: suspend (VaultProgress) -> Unit
    ) {
        val insertItemCount = itemsToUpsert.size
        val deleteItemCount = itemsToDelete.flatMap { it.second }.size
        PassLogger.i(TAG, "Going to insert $insertItemCount items and delete $deleteItemCount items")
        val upsertChunks = itemsToUpsert.chunked(MAX_ITEMS_PER_TRANSACTION)
        upsertChunks.forEachIndexed { idx, chunk ->
            PassLogger.i(TAG, "setShareItems insert(${idx + 1}/${upsertChunks.size})")
            localItemDataSource.upsertItems(chunk)
            val progress = idx * MAX_ITEMS_PER_TRANSACTION + chunk.size
            onProgress(VaultProgress(total = insertItemCount, current = progress.coerceAtMost(insertItemCount)))
        }
        database.inTransaction("setShareItems delete") {
            itemsToDelete.forEach { (shareId, toDelete) ->
                localItemDataSource.delete(userId, shareId, toDelete)
            }
        }
    }

    private suspend fun decryptPendingItemRevisions(
        shareId: ShareId,
        pendingRevisions: List<ItemRevision>,
        userId: UserId,
        addressId: AddressId,
        share: Share,
        shareKeys: List<ShareKey>,
        folderKeysMap: Map<FolderId, FolderKey>
    ): List<ItemEntity> = decryptRevisions(
        shareId = shareId,
        revisions = pendingRevisions,
        userId = userId,
        addressId = addressId,
        share = share,
        shareKeys = shareKeys,
        folderKeysMap = folderKeysMap,
        callerTag = "applyPendingEvent"
    ).first

    private suspend fun decryptItemRevisions(
        shareId: ShareId,
        revisions: List<ItemRevision>,
        userId: UserId,
        addressId: AddressId,
        share: Share,
        shareKeys: List<ShareKey>,
        folderKeysMap: Map<FolderId, FolderKey>
    ): Pair<List<ItemEntity>, Boolean> {
        val (items, failedItemIds) = decryptRevisions(
            shareId = shareId,
            revisions = revisions,
            userId = userId,
            addressId = addressId,
            share = share,
            shareKeys = shareKeys,
            folderKeysMap = folderKeysMap,
            callerTag = "setShareItems"
        )
        return items to failedItemIds.isNotEmpty()
    }

    private suspend fun decryptRevisions(
        shareId: ShareId,
        revisions: List<ItemRevision>,
        userId: UserId,
        addressId: AddressId,
        share: Share,
        shareKeys: List<ShareKey>,
        folderKeysMap: Map<FolderId, FolderKey>,
        callerTag: String
    ): Pair<List<ItemEntity>, List<ItemId>> {
        val failedItemIds = mutableListOf<ItemId>()
        val items = encryptionContextProvider.withEncryptionContextSuspendable {
            revisions.mapNotNull { revision ->
                safeRunCatching {
                    val folderKey: FolderKey? = revision.folderId?.let { folderId ->
                        folderKeysMap[FolderId(folderId)]
                            ?: throw IllegalStateException("FolderKey not found in the database")
                    }
                    itemResponseToEntity(
                        userId = userId,
                        addressId = addressId,
                        itemRevision = revision,
                        share = share,
                        shareKeys = shareKeys,
                        folderKey = folderKey,
                        encryptionContext = this
                    )
                }.onFailure { error ->
                    failedItemIds += ItemId(revision.itemId)
                    PassLogger.w(
                        TAG,
                        "$callerTag decrypt failed " +
                            "(shareId: ${shareId.id}, itemId: ${revision.itemId}, " +
                            "revision: ${revision.revision}, state: ${revision.state}, " +
                            "folderId: ${revision.folderId})"
                    )
                    PassLogger.w(TAG, error)
                }.getOrNull()
            }
        }
        if (failedItemIds.isNotEmpty()) {
            val failedPreview = failedItemIds.joinToString(separator = ",", limit = 5, truncated = "...")
            PassLogger.w(
                TAG,
                "$callerTag skipped ${failedItemIds.size}/${revisions.size} items " +
                    "for shareId=${shareId.id} (itemIds=$failedPreview)"
            )
        }
        return items to failedItemIds
    }

    private data class SetShareItemsPlan(
        val itemsToUpsert: List<ItemEntity>,
        val itemsToDelete: Pair<ShareId, List<ItemId>>,
        val hasFailedItems: Boolean
    )

    companion object {
        const val MAX_BATCH_ITEMS_PER_REQUEST = 50
        const val TAG = "ItemRepositoryImpl"

        // Max items to insert per transaction
        const val MAX_ITEMS_PER_TRANSACTION = 100

        private const val MAX_CONCURRENT_ITEM_REFRESHES = 5
    }
}
