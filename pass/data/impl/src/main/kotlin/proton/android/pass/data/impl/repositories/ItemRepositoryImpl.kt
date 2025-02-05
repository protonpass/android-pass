/*
 * Copyright (c) 2023 Proton AG
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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getAccounts
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.repository.UserAddressRepository
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.transpose
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.usecases.CreateItem
import proton.android.pass.crypto.api.usecases.ItemKeyWithRotation
import proton.android.pass.crypto.api.usecases.MigrateItem
import proton.android.pass.crypto.api.usecases.OpenItem
import proton.android.pass.crypto.api.usecases.UpdateItem
import proton.android.pass.data.api.ItemCountSummary
import proton.android.pass.data.api.ItemPendingEvent
import proton.android.pass.data.api.PendingEventList
import proton.android.pass.data.api.crypto.GetShareAndItemKey
import proton.android.pass.data.api.errors.ItemNotFoundError
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.repositories.ItemRevision
import proton.android.pass.data.api.repositories.MigrateItemsResult
import proton.android.pass.data.api.repositories.PinItemsResult
import proton.android.pass.data.api.repositories.ShareItemCount
import proton.android.pass.data.api.repositories.ShareRepository
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
import proton.android.pass.data.impl.requests.TrashItemRevision
import proton.android.pass.data.impl.requests.TrashItemsRequest
import proton.android.pass.data.impl.requests.UpdateItemFlagsRequest
import proton.android.pass.data.impl.responses.TrashItemsResponse
import proton.android.pass.data.impl.util.TimeUtil
import proton.android.pass.datamodels.api.serializeToProto
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
import proton.android.pass.domain.key.ShareKey
import proton.android.pass.log.api.PassLogger
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
    private val getShareAndItemKey: GetShareAndItemKey
) : BaseRepository(userAddressRepository), ItemRepository {

    @Suppress("TooGenericExceptionCaught")
    override suspend fun createItem(
        userId: UserId,
        share: Share,
        contents: ItemContents
    ): Item = withUserAddress(userId) { userAddress ->
        val shareKey = shareKeyRepository.getLatestKeyForShare(share.id).first()

        val body = try {
            createItem.create(shareKey, contents)
        } catch (e: Exception) {
            PassLogger.w(TAG, "Error creating item")
            PassLogger.w(TAG, e)
            throw e
        }

        val itemResponse =
            remoteItemDataSource.createItem(userId, share.id, body.request.toRequest())
        val entity = itemResponseToEntity(
            userAddress,
            itemResponse,
            share,
            listOf(shareKey)
        )
        localItemDataSource.upsertItem(entity)

        encryptionContextProvider.withEncryptionContextSuspendable {
            entity.toDomain(this)
        }
    }

    override suspend fun createAlias(
        userId: UserId,
        share: Share,
        newAlias: NewAlias
    ): Item = withUserAddress(userId) { userAddress ->
        val shareKey = shareKeyRepository.getLatestKeyForShare(share.id).first()
        val itemContents = ItemContents.Alias(
            title = newAlias.title,
            note = newAlias.note,
            aliasEmail = "" // Not used when creating the payload,
        )
        val body = createItem.create(shareKey, itemContents)

        val mailboxIds = newAlias.mailboxes.map { it.id }
        val requestBody = CreateAliasRequest(
            prefix = newAlias.prefix,
            signedSuffix = newAlias.suffix.signedSuffix,
            mailboxes = mailboxIds,
            aliasName = newAlias.aliasName,
            item = body.request.toRequest()
        )

        val itemResponse = remoteItemDataSource.createAlias(userId, share.id, requestBody)
        val entity = itemResponseToEntity(
            userAddress,
            itemResponse,
            share,
            listOf(shareKey)
        )
        localItemDataSource.upsertItem(entity)
        encryptionContextProvider.withEncryptionContextSuspendable {
            entity.toDomain(this)
        }
    }

    override suspend fun createItemAndAlias(
        userId: UserId,
        shareId: ShareId,
        contents: ItemContents,
        newAlias: NewAlias
    ): Item = withUserAddress(userId) { userAddress ->
        val share = shareRepository.getById(userId, shareId)
        val shareKey = shareKeyRepository.getLatestKeyForShare(shareId).first()
        val request = runCatching {
            val itemBody = createItem.create(shareKey, contents)
            val aliasContents = ItemContents.Alias(
                title = newAlias.title,
                note = newAlias.note,
                aliasEmail = "" // Not used when creating the payload
            )
            val aliasBody = createItem.create(shareKey, aliasContents)

            CreateItemAliasRequest(
                alias = CreateAliasRequest(
                    prefix = newAlias.prefix,
                    signedSuffix = newAlias.suffix.signedSuffix,
                    aliasName = newAlias.aliasName,
                    mailboxes = newAlias.mailboxes.map { it.id },
                    item = aliasBody.request.toRequest()
                ),
                item = itemBody.request.toRequest()
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
                userAddress,
                itemResponse.item.toDomain(),
                share,
                listOf(shareKey)
            )
        val aliasEntity =
            itemResponseToEntity(
                userAddress,
                itemResponse.alias.toDomain(),
                share,
                listOf(shareKey)
            )
        database.inTransaction("createItemAndAlias") {
            localItemDataSource.upsertItem(itemEntity)
            localItemDataSource.upsertItem(aliasEntity)
        }

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
        val localEntity = localItemDataSource.getById(share.id, item.id)
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
            itemContents
        )
    }

    override suspend fun updateItemFlags(
        userId: UserId,
        share: Share,
        itemId: ItemId,
        flag: ItemFlag,
        isFlagEnabled: Boolean
    ): Item = when (flag) {
        ItemFlag.SkipHealthCheck -> UpdateItemFlagsRequest().copy(skipHealthCheck = isFlagEnabled)
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
                userAddress = userAddress,
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
        shareId: ShareId,
        itemId: ItemId,
        flag: ItemFlag,
        isFlagEnabled: Boolean
    ) {
        val item: ItemEntity = localItemDataSource.getById(shareId, itemId)
            ?: throw ItemNotFoundError(itemId, shareId)
        val updatedFlags = if (isFlagEnabled) {
            item.flags or flag.value // set the flag
        } else {
            item.flags and flag.value.inv() // clear the flag
        }
        localItemDataSource.updateItemFlags(shareId, itemId, updatedFlags)
    }

    override suspend fun updateLocalItemsFlags(
        items: List<Pair<ShareId, ItemId>>,
        flag: ItemFlag,
        isFlagEnabled: Boolean
    ) {
        items.groupBy { it.first }
            .map { (shareId, itemIds) ->
                localItemDataSource.getByIdList(shareId, itemIds.map { it.second })
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

    override fun observeItems(
        userId: UserId,
        shareSelection: ShareSelection,
        itemState: ItemState?,
        itemTypeFilter: ItemTypeFilter,
        setFlags: Int?,
        clearFlags: Int?
    ): Flow<List<Item>> = innerObserveItems(
        shareSelection,
        userId,
        itemState,
        itemTypeFilter,
        setFlags,
        clearFlags
    ).map { items ->
        encryptionContextProvider.withEncryptionContextSuspendable {
            items.map { item -> item.toDomain(this) }
        }
    }

    override fun observeEncryptedItems(
        userId: UserId,
        shareSelection: ShareSelection,
        itemState: ItemState?,
        itemTypeFilter: ItemTypeFilter,
        setFlags: Int?,
        clearFlags: Int?
    ): Flow<List<ItemEncrypted>> = innerObserveItems(
        shareSelection,
        userId,
        itemState,
        itemTypeFilter,
        setFlags,
        clearFlags
    ).map { items ->
        items.map(ItemEntity::toEncryptedDomain)
    }

    override fun observeSharedByMeEncryptedItems(userId: UserId, itemState: ItemState?): Flow<List<ItemEncrypted>> =
        shareRepository.observeSharedByMeIds(userId, itemState, null)
            .flatMapLatest { shareIds ->
                localItemDataSource.observeItemsForShares(
                    userId,
                    shareIds,
                    itemState,
                    ItemTypeFilter.All
                )
            }
            .map { itemEntities ->
                itemEntities
                    .filter { it.shareCount > 0 }
                    .map(ItemEntity::toEncryptedDomain)
            }

    override fun observeSharedWithMeEncryptedItems(userId: UserId, itemState: ItemState?): Flow<List<ItemEncrypted>> =
        shareRepository.observeSharedWithMeIds(userId, itemState, null)
            .flatMapLatest { shareIds ->
                localItemDataSource.observeItemsForShares(
                    userId,
                    shareIds,
                    itemState,
                    ItemTypeFilter.All
                )
            }
            .map { itemEntities ->
                itemEntities.map(ItemEntity::toEncryptedDomain)
            }

    private fun innerObserveItems(
        shareSelection: ShareSelection,
        userId: UserId,
        itemState: ItemState?,
        itemTypeFilter: ItemTypeFilter,
        setFlags: Int?,
        clearFlags: Int?
    ) = when (shareSelection) {
        is ShareSelection.Share -> localItemDataSource.observeItemsForShares(
            userId = userId,
            shareIds = listOf(shareSelection.shareId),
            itemState = itemState,
            filter = itemTypeFilter,
            setFlags = setFlags,
            clearFlags = clearFlags
        )

        is ShareSelection.Shares -> localItemDataSource.observeItemsForShares(
            userId = userId,
            shareIds = shareSelection.shareIds,
            itemState = itemState,
            filter = itemTypeFilter,
            setFlags = setFlags,
            clearFlags = clearFlags
        )

        is ShareSelection.AllShares -> localItemDataSource.observeItems(
            userId = userId,
            itemState = itemState,
            filter = itemTypeFilter,
            setFlags = setFlags,
            clearFlags = clearFlags
        )
    }

    override fun observePinnedItems(
        userId: UserId,
        shareSelection: ShareSelection,
        itemTypeFilter: ItemTypeFilter
    ): Flow<List<Item>> = when (shareSelection) {
        is ShareSelection.Share -> localItemDataSource.observeAllPinnedItemsForShares(
            userId = userId,
            shareIds = listOf(shareSelection.shareId),
            filter = itemTypeFilter
        )

        is ShareSelection.Shares -> localItemDataSource.observeAllPinnedItemsForShares(
            userId = userId,
            shareIds = shareSelection.shareIds,
            filter = itemTypeFilter
        )

        is ShareSelection.AllShares -> localItemDataSource.observePinnedItems(
            userId = userId,
            filter = itemTypeFilter
        )
    }.map { items ->
        encryptionContextProvider.withEncryptionContextSuspendable {
            items.map { item -> item.toDomain(this) }
        }
    }

    override fun observeById(shareId: ShareId, itemId: ItemId): Flow<Item> =
        localItemDataSource.observeItem(shareId, itemId).map { itemEntity ->
            encryptionContextProvider.withEncryptionContextSuspendable {
                itemEntity.toDomain(this)
            }
        }

    override suspend fun getById(shareId: ShareId, itemId: ItemId): Item {
        val localItem = localItemDataSource.getById(shareId, itemId)
            ?: throw IllegalStateException("Item not found [shareId=${shareId.id}] [itemId=${itemId.id}]")

        return encryptionContextProvider.withEncryptionContextSuspendable {
            localItem.toDomain(this)
        }
    }

    override suspend fun getByIds(shareId: ShareId, itemIds: List<ItemId>): List<Item> {
        return localItemDataSource.getByIdList(shareId, itemIds)
            .let { itemEntities ->
                encryptionContextProvider.withEncryptionContextSuspendable {
                    itemEntities.map { itemEntity -> itemEntity.toDomain(this) }
                }
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
        }
    }

    private suspend fun trashItemsForShare(
        userId: UserId,
        shareId: ShareId,
        itemIds: List<ItemId>
    ): Result<Unit> = localItemDataSource.getByIdList(shareId, itemIds)
        .chunked(MAX_BATCH_ITEMS_PER_REQUEST)
        .map { items ->
            val body = TrashItemsRequest(
                items.map { TrashItemRevision(it.id, it.revision) }
            )

            runCatching { remoteItemDataSource.sendToTrash(userId, shareId, body) }
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
        }
    }

    private suspend fun untrashItemsForShare(
        userId: UserId,
        shareId: ShareId,
        itemIds: List<ItemId>
    ): Result<Unit> = localItemDataSource.getByIdList(shareId, itemIds)
        .chunked(MAX_BATCH_ITEMS_PER_REQUEST)
        .map { items ->
            val body = TrashItemsRequest(
                items.map { TrashItemRevision(it.id, it.revision) }
            )

            runCatching { remoteItemDataSource.untrash(userId, shareId, body) }
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

    override suspend fun clearTrash(userId: UserId) {
        coroutineScope {
            val trashedItems = localItemDataSource.getTrashedItems(userId)
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

    override suspend fun restoreItems(userId: UserId) {
        coroutineScope {
            val trashedItems = localItemDataSource.getTrashedItems(userId)
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
        }
    }

    private suspend fun deleteItemsForShare(
        userId: UserId,
        shareId: ShareId,
        itemIds: List<ItemId>
    ): Result<Unit> = localItemDataSource.getByIdList(shareId, itemIds)
        .chunked(MAX_BATCH_ITEMS_PER_REQUEST)
        .map { items ->
            val body = TrashItemsRequest(
                items.map { TrashItemRevision(it.id, it.revision) }
            )

            runCatching { remoteItemDataSource.delete(userId, shareId, body) }
                .onSuccess {
                    localItemDataSource.deleteList(shareId, items.map { ItemId(it.id) })
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
        val itemEntity = localItemDataSource.getById(shareId, itemId)
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
            updatedContents
        )
    }

    override suspend fun refreshItems(userId: UserId, share: Share): List<Item> {
        val address = shareRepository.getAddressForShareId(userId, share.id)
        val items = remoteItemDataSource.getItems(address.userId, share.id)
        return decryptItems(address, share, items)
    }

    override suspend fun refreshItems(userId: UserId, shareId: ShareId): List<Item> {
        val share = shareRepository.getById(userId, shareId)
        return refreshItems(userId, share)
    }

    override suspend fun downloadItemsAndObserveProgress(
        userId: UserId,
        shareId: ShareId,
        onProgress: suspend (VaultProgress) -> Unit
    ): List<ItemRevision> {
        val items = mutableListOf<ItemRevision>()
        remoteItemDataSource.observeItems(userId, shareId)
            .catch { error ->
                PassLogger.w(TAG, "Error refreshing and observing items sync progress")
                PassLogger.w(TAG, error)
                throw error
            }
            .collect { itemTotal ->
                items.addAll(itemTotal.items)
                onProgress(
                    VaultProgress(
                        total = itemTotal.total,
                        current = itemTotal.created
                    )
                )
            }

        return items
    }

    override suspend fun applyEvents(
        userId: UserId,
        addressId: AddressId,
        shareId: ShareId,
        events: PendingEventList
    ) {
        val userAddress = requireNotNull(userAddressRepository.getAddress(userId, addressId))
        val share = shareRepository.getById(userId, shareId)
        val shareKeys = shareKeyRepository.getShareKeys(userId, addressId, shareId).first()

        val updateAsEntities = encryptionContextProvider.withEncryptionContextSuspendable {
            events.updatedItems.map {
                itemResponseToEntity(
                    userAddress = userAddress,
                    itemRevision = it.toItemRevision().toDomain(),
                    share = share,
                    shareKeys = shareKeys,
                    encryptionContext = this
                )
            }
        }

        if (updateAsEntities.isNotEmpty() && events.deletedItemIds.isNotEmpty()) {
            database.inTransaction("applyEvents") {
                localItemDataSource.upsertItems(updateAsEntities)
                localItemDataSource.deleteList(
                    shareId,
                    events.deletedItemIds.map(::ItemId)
                )
            }
            return
        }

        if (updateAsEntities.isNotEmpty()) {
            localItemDataSource.upsertItems(updateAsEntities)
            return
        }

        if (events.deletedItemIds.isNotEmpty()) {
            localItemDataSource.deleteList(
                shareId,
                events.deletedItemIds.map(::ItemId)
            )
        }
    }

    override suspend fun setShareItems(
        userId: UserId,
        items: Map<ShareId, List<ItemRevision>>,
        onProgress: suspend (VaultProgress) -> Unit
    ) {
        if (items.isEmpty()) return

        val plans: List<Result<SetShareItemsPlan>> = coroutineScope {
            items.map { (shareId, revisions) ->
                async {
                    calculatePlanForSetShareItems(userId, shareId, revisions)
                }
            }.awaitAll()
        }

        val (successes, failures) = plans.partition { it.isSuccess }

        if (failures.isNotEmpty()) {
            failures.first().exceptionOrNull()?.let {
                PassLogger.w(TAG, "Error calculating plan for setShareItems")
                PassLogger.w(TAG, it)
            }
        }

        val successPlans = successes.mapNotNull { it.getOrNull() }
        val itemsToUpsert = successPlans.flatMap { it.itemsToUpsert }
        val itemsToDelete = successPlans.map { it.itemsToDelete }

        val insertItemCount = itemsToUpsert.size
        val deleteItemCount = itemsToDelete.flatMap { it.second }.size
        PassLogger.i(
            TAG,
            "Going to insert $insertItemCount items and delete $deleteItemCount items"
        )

        val upsertChunks: List<List<ItemEntity>> = itemsToUpsert.chunked(MAX_ITEMS_PER_TRANSACTION)
        upsertChunks.forEachIndexed { idx, chunk ->
            PassLogger.i(TAG, "setShareItems insert(${idx + 1}/${upsertChunks.size})")
            localItemDataSource.upsertItems(chunk)
            val progress = idx * MAX_ITEMS_PER_TRANSACTION + chunk.size
            onProgress(
                VaultProgress(
                    total = insertItemCount,
                    current = progress.coerceAtMost(insertItemCount)
                )
            )
        }

        database.inTransaction("setShareItems delete") {
            itemsToDelete.forEach { (shareId, toDelete) ->
                localItemDataSource.deleteList(shareId, toDelete)
            }
        }
    }

    override suspend fun applyPendingEvent(event: ItemPendingEvent) = with(event) {
        if (!hasPendingItemRevisions) return

        val userAddress = requireNotNull(userAddressRepository.getAddress(userId, addressId))
        val share = shareRepository.getById(userId, shareId)
        val shareKeys = shareKeyRepository.getShareKeys(userId, addressId, shareId).first()

        val items = encryptionContextProvider.withEncryptionContextSuspendable {
            pendingItemRevisions.map { pendingItemRevision ->
                itemResponseToEntity(
                    userAddress = userAddress,
                    itemRevision = pendingItemRevision.toItemRevision().toDomain(),
                    share = share,
                    shareKeys = shareKeys,
                    encryptionContext = this
                )
            }
        }

        localItemDataSource.upsertItems(items)
    }

    override suspend fun purgePendingEvent(event: ItemPendingEvent) = with(event) {
        if (!hasDeletedItemIds) return false

        localItemDataSource.deleteList(shareId, deletedItemIds)
    }

    override fun observeItemCountSummary(
        userId: UserId,
        shareIds: List<ShareId>,
        itemState: ItemState?
    ): Flow<ItemCountSummary> = localItemDataSource.observeItemCountSummary(userId, shareIds, itemState)

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
        destination: Share
    ): MigrateItemsResult = coroutineScope {
        val destinationKey = shareKeyRepository.getLatestKeyForShare(destination.id).first()
        val migratedRevisions: List<Result<List<ItemEntity>>> = items.map { (shareId, items) ->
            async {
                runCatching {
                    val shareItems = localItemDataSource.getByIdList(shareId, items)
                    migrateItemsForShare(
                        userId = userId,
                        source = shareId,
                        destination = destination.id,
                        destinationKey = destinationKey,
                        items = shareItems
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
        destination: ShareId
    ) {
        val items = localItemDataSource.observeItemsForShares(
            userId = userId,
            shareIds = listOf(source),
            itemState = ItemState.Active,
            filter = ItemTypeFilter.All
        ).first()
        val destinationKey = shareKeyRepository.getLatestKeyForShare(destination).first()

        migrateItemsForShare(
            userId = userId,
            source = source,
            destination = destination,
            destinationKey = destinationKey,
            items = items
        )
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
        val itemEntity = localItemDataSource.getById(shareId, itemId)
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
            itemContents = updatedContents
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
    ) = runCatching {
        val share = shareRepository.getById(userId, shareId)
        val address = shareRepository.getAddressForShareId(userId, shareId)
        val localItemsForShare = localItemDataSource.observeItemsForShares(
            userId = userId,
            shareIds = listOf(shareId),
            itemState = null,
            filter = ItemTypeFilter.All
        ).first()

        val itemsNotPresentInRemote = localItemsForShare
            // Filter out items that are not present in the remote
            .filter { localItem -> revisions.none { it.itemId == localItem.id } }
            // Keep only the ItemIDs
            .map { ItemId(it.id) }

        val shareKeys = shareKeyRepository.getShareKeys(
            userId = userId,
            addressId = address.addressId,
            shareId = shareId
        ).first()

        val itemsToUpsert = encryptionContextProvider.withEncryptionContextSuspendable {
            revisions.map {
                itemResponseToEntity(
                    userAddress = address,
                    itemRevision = it,
                    share = share,
                    shareKeys = shareKeys,
                    encryptionContext = this
                )
            }
        }

        SetShareItemsPlan(
            itemsToDelete = shareId to itemsNotPresentInRemote,
            itemsToUpsert = itemsToUpsert
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
            async { runCatching { shareId to block(userId, shareId, itemId) } }
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
            userAddress,
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
        items: List<ItemEntity>
    ): List<ItemEntity> = withContext(Dispatchers.Default) {
        val userAddress = shareRepository.getAddressForShareId(userId, source)
        items.chunked(MAX_BATCH_ITEMS_PER_REQUEST).map { chunk ->
            async {
                migrateChunk(userAddress, source, destination, destinationKey, chunk)
            }
        }.awaitAll().flatten()
    }

    private suspend fun migrateChunk(
        userAddress: UserAddress,
        source: ShareId,
        destination: ShareId,
        destinationKey: ShareKey,
        chunk: List<ItemEntity>
    ): List<ItemEntity> {
        val userId = userAddress.userId
        val migrations = chunk.map { item ->
            val (_, itemKey) = getShareAndItemKey(
                userAddress = userAddress,
                shareId = source,
                itemId = ItemId(item.id)
            )
            val encryptedMigrateItemBody = migrateItem.migrate(
                destinationKey,
                listOf(ItemKeyWithRotation(itemKey.key, itemKey.rotation))
            )
            MigrateItemsBody(
                itemId = item.id,
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

        val destinationShare = shareRepository.getById(userId, destination)

        val res = remoteItemDataSource.migrateItems(userId, source, body)

        val resAsEntities = encryptionContextProvider.withEncryptionContextSuspendable {
            res.map {
                itemResponseToEntity(
                    userAddress = userAddress,
                    itemRevision = it,
                    share = destinationShare,
                    shareKeys = listOf(destinationKey),
                    encryptionContext = this
                )
            }
        }

        val itemIdsToDelete = chunk.map { ItemId(it.id) }
        database.inTransaction("migrateChunk") {
            localItemDataSource.upsertItems(resAsEntities)
            localItemDataSource.deleteList(source, itemIdsToDelete)
        }

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
                localItemDataSource.deleteList(
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
        itemContents: ItemV1.Item
    ): Item = withUserAddress(userId) { userAddress ->
        val (shareKey, itemKey) = getShareAndItemKey(
            userAddress = userAddress,
            shareId = share.id,
            itemId = item.id
        )
        val body = updateItem.createRequest(
            itemKey,
            itemContents,
            item.revision
        )
        val itemResponse = remoteItemDataSource.updateItem(
            userId = userId,
            shareId = share.id,
            itemId = item.id,
            body = body.toRequest()
        )
        val entity = itemResponseToEntity(
            userAddress,
            itemResponse,
            share,
            listOf(shareKey)
        )
        localItemDataSource.upsertItem(entity)
        encryptionContextProvider.withEncryptionContextSuspendable {
            entity.toDomain(this)
        }
    }

    private suspend fun decryptItems(
        userAddress: UserAddress,
        share: Share,
        items: List<ItemRevision>
    ): List<Item> {
        val shareKeys = shareKeyRepository
            .getShareKeys(userAddress.userId, userAddress.addressId, share.id)
            .first()
        val itemsEntities = encryptionContextProvider.withEncryptionContextSuspendable {
            val encryptionContext = this
            withContext(Dispatchers.Default) {
                items.map { item ->
                    async {
                        decryptItem(
                            encryptionContext = encryptionContext,
                            userAddress = userAddress,
                            share = share,
                            item = item,
                            shareKeys = shareKeys
                        )
                    }
                }.awaitAll()
            }
        }

        val entities = itemsEntities.map { it.second }
        localItemDataSource.upsertItems(entities)

        return itemsEntities.map { it.first }
    }

    private suspend fun decryptItem(
        encryptionContext: EncryptionContext,
        userAddress: UserAddress,
        share: Share,
        item: ItemRevision,
        shareKeys: List<ShareKey>
    ): Pair<Item, ItemEntity> {
        val entity = itemResponseToEntity(
            userAddress = userAddress,
            itemRevision = item,
            share = share,
            shareKeys = shareKeys
        )
        return entity.toDomain(encryptionContext) to entity
    }

    private suspend fun itemResponseToEntity(
        userAddress: UserAddress,
        itemRevision: ItemRevision,
        share: Share,
        shareKeys: List<ShareKey>
    ): ItemEntity = encryptionContextProvider.withEncryptionContextSuspendable {
        itemResponseToEntity(
            userAddress = userAddress,
            itemRevision = itemRevision,
            share = share,
            shareKeys = shareKeys,
            encryptionContext = this
        )
    }

    private fun itemResponseToEntity(
        userAddress: UserAddress,
        itemRevision: ItemRevision,
        share: Share,
        shareKeys: List<ShareKey>,
        encryptionContext: EncryptionContext
    ): ItemEntity {
        val output = openItem.open(
            response = itemRevision.toCrypto(),
            share = share,
            shareKeys = shareKeys,
            encryptionContext = encryptionContext
        )
        val hasTotp = output.item.hasTotp(encryptionContext)
        return ItemEntity(
            id = itemRevision.itemId,
            userId = userAddress.userId.id,
            addressId = userAddress.addressId.id,
            shareId = share.id.id,
            revision = itemRevision.revision,
            contentFormatVersion = itemRevision.contentFormatVersion,
            content = itemRevision.content,
            state = itemRevision.state,
            itemType = output.item.itemType.category.value,
            createTime = itemRevision.createTime,
            modifyTime = itemRevision.modifyTime,
            lastUsedTime = itemRevision.lastUseTime,
            encryptedContent = output.item.content,
            encryptedTitle = output.item.title,
            encryptedNote = output.item.note,
            aliasEmail = itemRevision.aliasEmail,
            keyRotation = itemRevision.keyRotation,
            key = itemRevision.itemKey,
            encryptedKey = output.itemKey,
            hasTotp = hasTotp,
            isPinned = itemRevision.isPinned,
            hasPasskeys = output.item.hasPasskeys,
            flags = itemRevision.flags,
            shareCount = itemRevision.shareCount
        )
    }

    private data class SetShareItemsPlan(
        val itemsToUpsert: List<ItemEntity>,
        val itemsToDelete: Pair<ShareId, List<ItemId>>
    )

    companion object {
        const val MAX_BATCH_ITEMS_PER_REQUEST = 50
        const val TAG = "ItemRepositoryImpl"

        // Max items to insert per transaction
        const val MAX_ITEMS_PER_TRANSACTION = 100
    }
}
