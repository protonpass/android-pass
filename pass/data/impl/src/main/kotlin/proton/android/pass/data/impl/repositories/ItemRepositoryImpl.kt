package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.extension.primary
import me.proton.core.user.domain.repository.UserAddressRepository
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.map
import proton.android.pass.common.api.toOption
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.error.CryptoException
import proton.android.pass.crypto.api.extensions.serializeToProto
import proton.android.pass.crypto.api.usecases.CreateItem
import proton.android.pass.crypto.api.usecases.MigrateItem
import proton.android.pass.crypto.api.usecases.OpenItem
import proton.android.pass.crypto.api.usecases.UpdateItem
import proton.android.pass.data.api.ItemCountSummary
import proton.android.pass.data.api.PendingEventList
import proton.android.pass.data.api.errors.CannotRemoveNotTrashedItemError
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.repositories.ShareItemCount
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.db.entities.ItemEntity
import proton.android.pass.data.impl.extensions.allowedApps
import proton.android.pass.data.impl.extensions.fromParsed
import proton.android.pass.data.impl.extensions.hasPackageName
import proton.android.pass.data.impl.extensions.hasWebsite
import proton.android.pass.data.impl.extensions.toCrypto
import proton.android.pass.data.impl.extensions.toItemRevision
import proton.android.pass.data.impl.extensions.toRequest
import proton.android.pass.data.impl.extensions.with
import proton.android.pass.data.impl.extensions.withUrl
import proton.android.pass.data.impl.local.LocalItemDataSource
import proton.android.pass.data.impl.remote.RemoteItemDataSource
import proton.android.pass.data.impl.requests.CreateAliasRequest
import proton.android.pass.data.impl.requests.CreateItemAliasRequest
import proton.android.pass.data.impl.requests.MigrateItemRequest
import proton.android.pass.data.impl.requests.TrashItemRevision
import proton.android.pass.data.impl.requests.TrashItemsRequest
import proton.android.pass.data.impl.responses.ItemRevision
import proton.android.pass.data.impl.util.TimeUtil
import proton.android.pass.log.api.PassLogger
import proton.pass.domain.Item
import proton.pass.domain.ItemContents
import proton.pass.domain.ItemId
import proton.pass.domain.ItemState
import proton.pass.domain.ItemType
import proton.pass.domain.Share
import proton.pass.domain.ShareId
import proton.pass.domain.ShareSelection
import proton.pass.domain.entity.NewAlias
import proton.pass.domain.entity.PackageInfo
import proton.pass.domain.key.ShareKey
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
    private val itemKeyRepository: ItemKeyRepository,
    private val encryptionContextProvider: EncryptionContextProvider
) : BaseRepository(userAddressRepository), ItemRepository {

    @Suppress("TooGenericExceptionCaught")
    override suspend fun createItem(
        userId: UserId,
        share: Share,
        contents: ItemContents
    ): Item = withContext(Dispatchers.IO) {
        withUserAddress(userId) { userAddress ->
            val shareKey = shareKeyRepository.getLatestKeyForShare(share.id).first()

            val body = try {
                createItem.create(shareKey, contents)
            } catch (e: RuntimeException) {
                PassLogger.w(TAG, e, "Error creating item")
                throw e
            }

            val itemResponse = remoteItemDataSource.createItem(userId, share.id, body.request.toRequest())
            val entity = itemResponseToEntity(
                userAddress,
                itemResponse,
                share,
                listOf(shareKey)
            )
            localItemDataSource.upsertItem(entity)

            encryptionContextProvider.withEncryptionContext {
                entityToDomain(this@withEncryptionContext, entity)
            }
        }
    }

    override suspend fun createAlias(
        userId: UserId,
        share: Share,
        newAlias: NewAlias
    ): Item = withContext(Dispatchers.IO) {
        withUserAddress(userId) { userAddress ->
            val shareKey = shareKeyRepository.getLatestKeyForShare(share.id).first()
            val itemContents = ItemContents.Alias(title = newAlias.title, note = newAlias.note)
            val body = createItem.create(shareKey, itemContents)

            val mailboxIds = newAlias.mailboxes.map { it.id }
            val requestBody = CreateAliasRequest(
                prefix = newAlias.prefix,
                signedSuffix = newAlias.suffix.signedSuffix,
                mailboxes = mailboxIds,
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
            encryptionContextProvider.withEncryptionContext {
                entityToDomain(this@withEncryptionContext, entity)
            }
        }
    }

    override suspend fun createItemAndAlias(
        userId: UserId,
        shareId: ShareId,
        contents: ItemContents,
        newAlias: NewAlias
    ): Item = withContext(Dispatchers.IO) {
        withUserAddress(userId) { userAddress ->
            val share = shareRepository.getById(userId, shareId)
            val shareKey = shareKeyRepository.getLatestKeyForShare(shareId).first()
            val request = runCatching {
                val itemBody = createItem.create(shareKey, contents)
                val aliasContents = ItemContents.Alias(title = newAlias.title, note = newAlias.note)
                val aliasBody = createItem.create(shareKey, aliasContents)

                CreateItemAliasRequest(
                    alias = CreateAliasRequest(
                        prefix = newAlias.prefix,
                        signedSuffix = newAlias.suffix.signedSuffix,
                        mailboxes = newAlias.mailboxes.map { it.id },
                        item = aliasBody.request.toRequest()
                    ),
                    item = itemBody.request.toRequest()
                )
            }.fold(
                onSuccess = { it },
                onFailure = {
                    PassLogger.e(TAG, it, "Error creating item")
                    throw it
                }
            )

            val itemResponse = remoteItemDataSource.createItemAndAlias(userId, shareId, request)
            val itemEntity = itemResponseToEntity(userAddress, itemResponse.item, share, listOf(shareKey))
            val aliasEntity = itemResponseToEntity(userAddress, itemResponse.alias, share, listOf(shareKey))
            database.inTransaction {
                localItemDataSource.upsertItem(itemEntity)
                localItemDataSource.upsertItem(aliasEntity)
            }

            encryptionContextProvider.withEncryptionContext {
                entityToDomain(this@withEncryptionContext, itemEntity)
            }
        }
    }

    override suspend fun updateItem(
        userId: UserId,
        share: Share,
        item: Item,
        contents: ItemContents
    ): Item = withContext(Dispatchers.IO) {
        performUpdate(
            userId,
            share,
            item,
            contents.serializeToProto(itemUuid = item.itemUuid)
        )
    }

    override fun observeItems(
        userId: UserId,
        shareSelection: ShareSelection,
        itemState: ItemState,
        itemTypeFilter: ItemTypeFilter
    ): Flow<List<Item>> =
        when (shareSelection) {
            is ShareSelection.Share -> localItemDataSource.observeItemsForShare(
                userId = userId,
                shareId = shareSelection.shareId,
                itemState = itemState,
                filter = itemTypeFilter
            )
            is ShareSelection.AllShares -> localItemDataSource.observeItems(
                userId = userId,
                itemState = itemState,
                filter = itemTypeFilter
            )
        }
            .map { items ->
                // Detect if we have received the update from a logout
                val isAccountStillAvailable = accountManager.getAccount(userId).first() != null
                if (!isAccountStillAvailable) return@map emptyList()
                encryptionContextProvider.withEncryptionContext {
                    items.map { entityToDomain(this@withEncryptionContext, it) }
                }
            }
            .flowOn(Dispatchers.IO)

    override suspend fun getById(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Item =
        withContext(Dispatchers.IO) {
            val item = localItemDataSource.getById(shareId, itemId)
            requireNotNull(item)
            encryptionContextProvider.withEncryptionContext {
                entityToDomain(this@withEncryptionContext, item)
            }
        }

    override suspend fun trashItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): LoadingResult<Unit> =
        withContext(Dispatchers.IO) {
            val item = requireNotNull(localItemDataSource.getById(shareId, itemId))
            if (item.state == ItemState.Trashed.value) {
                return@withContext LoadingResult.Error(CannotRemoveNotTrashedItemError())
            }

            val body = TrashItemsRequest(
                listOf(TrashItemRevision(itemId = item.id, revision = item.revision))
            )

            return@withContext remoteItemDataSource.sendToTrash(userId, shareId, body)
                .map { response ->
                    database.inTransaction {
                        response.items.find { it.itemId == item.id }
                            ?.let {
                                val updatedItem = item.copy(
                                    revision = it.revision,
                                    state = ItemState.Trashed.value
                                )
                                localItemDataSource.upsertItem(updatedItem)
                            }
                            ?: Unit
                    }
                }
        }

    override suspend fun untrashItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): LoadingResult<Unit> = withContext(Dispatchers.IO) {
        // Optimistically update the local database
        val originalItem: ItemEntity = database.inTransaction {
            val item = requireNotNull(localItemDataSource.getById(shareId, itemId))
            if (item.state == ItemState.Active.value) return@inTransaction null
            val updatedItem = item.copy(
                state = ItemState.Active.value
            )
            localItemDataSource.upsertItem(updatedItem)
            item
        } ?: return@withContext LoadingResult.Error(
            IllegalStateException("UnTrash item could not be updated locally")
        )

        // Perform the network request
        val body = TrashItemsRequest(
            listOf(TrashItemRevision(originalItem.id, originalItem.revision))
        )
        return@withContext when (val res = remoteItemDataSource.untrash(userId, shareId, body)) {
            LoadingResult.Loading -> LoadingResult.Loading
            is LoadingResult.Error -> {
                localItemDataSource.upsertItem(originalItem)
                LoadingResult.Error(res.exception)
            }
            is LoadingResult.Success -> LoadingResult.Success(Unit)
        }
    }

    override suspend fun clearTrash(userId: UserId): LoadingResult<Unit> =
        withContext(Dispatchers.IO) {
            val trashedItems = localItemDataSource.getTrashedItems(userId)
            val trashedPerShare = trashedItems.groupBy { it.shareId }
            trashedPerShare
                .map { entry ->
                    async {
                        val shareId = ShareId(entry.key)
                        val shareItems = entry.value
                        shareItems.chunked(MAX_TRASH_ITEMS_PER_REQUEST).forEach { items ->
                            val body =
                                TrashItemsRequest(
                                    items.map {
                                        TrashItemRevision(
                                            it.id,
                                            it.revision
                                        )
                                    }
                                )
                            remoteItemDataSource.delete(userId, shareId, body)
                            database.inTransaction {
                                items.forEach { item ->
                                    localItemDataSource.delete(
                                        shareId,
                                        ItemId(item.id)
                                    )
                                }
                            }
                        }
                    }
                }
                .awaitAll()
            LoadingResult.Success(Unit)
        }

    override suspend fun restoreItems(userId: UserId): LoadingResult<Unit> =
        withContext(Dispatchers.IO) {
            val trashedItems = localItemDataSource.getTrashedItems(userId)
            val trashedPerShare = trashedItems.groupBy { it.shareId }
            trashedPerShare
                .map { entry ->
                    async {
                        val shareId = ShareId(entry.key)
                        val shareItems = entry.value
                        shareItems.chunked(MAX_TRASH_ITEMS_PER_REQUEST).forEach { items ->
                            val body =
                                TrashItemsRequest(
                                    items.map {
                                        TrashItemRevision(
                                            it.id,
                                            it.revision
                                        )
                                    }
                                )
                            remoteItemDataSource.untrash(userId, shareId, body)
                            database.inTransaction {
                                items.forEach { item ->
                                    localItemDataSource.setItemState(
                                        shareId,
                                        ItemId(item.id),
                                        ItemState.Active
                                    )
                                }
                            }
                        }
                    }
                }
                .awaitAll()
            LoadingResult.Success(Unit)
        }

    override suspend fun deleteItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): LoadingResult<Unit> = withContext(Dispatchers.IO) {
        val item = requireNotNull(localItemDataSource.getById(shareId, itemId))
        if (item.state != ItemState.Trashed.value) return@withContext LoadingResult.Success(Unit)

        val body =
            TrashItemsRequest(listOf(TrashItemRevision(itemId = item.id, revision = item.revision)))
        return@withContext when (val result = remoteItemDataSource.delete(userId, shareId, body)) {
            is LoadingResult.Error -> LoadingResult.Error(result.exception)
            LoadingResult.Loading -> LoadingResult.Loading
            is LoadingResult.Success -> {
                localItemDataSource.delete(shareId, itemId)
                LoadingResult.Success(Unit)
            }
        }
    }

    @Suppress("ReturnCount")
    override suspend fun addPackageAndUrlToItem(
        shareId: ShareId,
        itemId: ItemId,
        packageInfo: Option<PackageInfo>,
        url: Option<String>
    ): LoadingResult<Item> = withContext(Dispatchers.IO) {
        val itemEntity = requireNotNull(localItemDataSource.getById(shareId, itemId))

        val (item, itemProto) = encryptionContextProvider.withEncryptionContext {
            val item = entityToDomain(this@withEncryptionContext, itemEntity)
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
            return@withContext LoadingResult.Success(item)
        }

        val userId = accountManager.getPrimaryUserId().first()
            ?: throw CryptoException("UserId cannot be null")
        val share = shareRepository.getById(userId, shareId)
        return@withContext LoadingResult.Success(performUpdate(userId, share, item, updatedContents))
    }

    override suspend fun refreshItems(userId: UserId, share: Share): List<Item> =
        withContext(Dispatchers.IO) {
            val address = requireNotNull(userAddressRepository.getAddresses(userId).primary())
            val items = remoteItemDataSource.getItems(address.userId, share.id)
            decryptItems(address, share, items)
        }

    override suspend fun refreshItems(userId: UserId, shareId: ShareId): List<Item> =
        withContext(Dispatchers.IO) {
            val share = shareRepository.getById(userId, shareId)
            refreshItems(userId, share)
        }

    override suspend fun applyEvents(
        userId: UserId,
        addressId: AddressId,
        shareId: ShareId,
        events: PendingEventList
    ) {
        withContext(Dispatchers.IO) {
            PassLogger.i(
                TAG,
                "Applying events: [updates=${events.updatedItems.size}] [deletes=${events.deletedItemIds.size}]"
            )

            val userAddress = requireNotNull(userAddressRepository.getAddress(userId, addressId))
            val share = shareRepository.getById(userId, shareId)
            val shareKeys = shareKeyRepository.getShareKeys(userId, addressId, shareId).first()

            val updateAsEntities = events.updatedItems.map {
                itemResponseToEntity(
                    userAddress,
                    it.toItemRevision(),
                    share,
                    shareKeys
                )
            }

            database.inTransaction {
                localItemDataSource.upsertItems(updateAsEntities)
                events.deletedItemIds.forEach { itemId ->
                    localItemDataSource.delete(shareId, ItemId(itemId))
                }
            }
            PassLogger.i(TAG, "Finishing applying events")
        }
    }

    override fun observeItemCountSummary(
        userId: UserId,
        shareIds: List<ShareId>
    ): Flow<ItemCountSummary> =
        localItemDataSource.observeItemCountSummary(userId, shareIds)
            .flowOn(Dispatchers.IO)

    override suspend fun updateItemLastUsed(shareId: ShareId, itemId: ItemId) {
        withContext(Dispatchers.IO) {
            val userId = accountManager.getPrimaryUserId().first()
                ?: throw CryptoException("UserId cannot be null")

            PassLogger.i(TAG, "Updating last used time [shareId=$shareId][itemId=$itemId]")

            val now = TimeUtil.getNowUtc()
            localItemDataSource.updateLastUsedTime(shareId, itemId, now)
            remoteItemDataSource.updateLastUsedTime(userId, shareId, itemId, now)

            PassLogger.i(TAG, "Updated last used time [shareId=$shareId][itemId=$itemId]")
        }
    }

    override fun observeItemCount(shareIds: List<ShareId>): Flow<Map<ShareId, ShareItemCount>> =
        localItemDataSource.observeItemCount(shareIds)

    override suspend fun migrateItem(
        userId: UserId,
        source: Share,
        destination: Share,
        itemId: ItemId
    ): Item {
        val item = requireNotNull(localItemDataSource.getById(source.id, itemId))
        val destinationKey = shareKeyRepository.getLatestKeyForShare(destination.id).first()

        val body = migrateItem.migrate(destinationKey, item.encryptedContent, item.contentFormatVersion)
        val request = MigrateItemRequest(
            shareId = destination.id.id,
            item = body.toRequest()
        )

        val res = remoteItemDataSource.migrateItem(userId, source.id, ItemId(item.id), request)

        val userAddress = requireNotNull(userAddressRepository.getAddresses(userId).primary())
        val resAsEntity =
            itemResponseToEntity(userAddress, res, destination, listOf(destinationKey))
        database.inTransaction {
            localItemDataSource.upsertItem(resAsEntity)
            localItemDataSource.delete(source.id, ItemId(item.id))
        }

        return encryptionContextProvider.withEncryptionContext {
            entityToDomain(this@withEncryptionContext, resAsEntity)
        }
    }

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
    ): Item {
        return withUserAddress(userId) { userAddress ->
            val (shareKey, itemKey) = itemKeyRepository
                .getLatestItemKey(userId, userAddress.addressId, share.id, item.id)
                .first()
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
            encryptionContextProvider.withEncryptionContext {
                entityToDomain(this@withEncryptionContext, entity)
            }
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
            val encryptionContext = this@withEncryptionContextSuspendable
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

    private fun decryptItem(
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
        return entityToDomain(encryptionContext, entity) to entity
    }

    private fun itemResponseToEntity(
        userAddress: UserAddress,
        itemRevision: ItemRevision,
        share: Share,
        shareKeys: List<ShareKey>
    ): ItemEntity {
        val output = openItem.open(itemRevision.toCrypto(), share, shareKeys)
        return ItemEntity(
            id = itemRevision.itemId,
            userId = userAddress.userId.id,
            addressId = userAddress.addressId.id,
            shareId = share.id.id,
            revision = itemRevision.revision,
            contentFormatVersion = itemRevision.contentFormatVersion,
            content = itemRevision.content,
            state = itemRevision.state,
            itemType = output.item.itemType.toWeightedInt(),
            createTime = itemRevision.createTime,
            modifyTime = itemRevision.modifyTime,
            lastUsedTime = itemRevision.lastUseTime,
            encryptedContent = output.item.content,
            encryptedTitle = output.item.title,
            encryptedNote = output.item.note,
            aliasEmail = itemRevision.aliasEmail,
            keyRotation = itemRevision.keyRotation,
            key = itemRevision.itemKey,
            encryptedKey = output.itemKey
        )
    }

    private fun entityToDomain(
        encryptionContext: EncryptionContext,
        entity: ItemEntity
    ): Item {
        val decrypted = encryptionContext.decrypt(entity.encryptedContent)
        val parsed = ItemV1.Item.parseFrom(decrypted)

        return Item(
            id = ItemId(entity.id),
            itemUuid = parsed.metadata.itemUuid,
            revision = entity.revision,
            shareId = ShareId(entity.shareId),
            itemType = ItemType.fromParsed(encryptionContext, parsed, entity.aliasEmail),
            title = entity.encryptedTitle,
            note = entity.encryptedNote,
            content = entity.encryptedContent,
            state = entity.state,
            packageInfoSet = entity.allowedApps(encryptionContext),
            modificationTime = Instant.fromEpochSeconds(entity.modifyTime),
            createTime = Instant.fromEpochSeconds(entity.createTime),
            lastAutofillTime = entity.lastUsedTime.toOption().map(Instant::fromEpochSeconds)
        )
    }

    companion object {
        const val MAX_TRASH_ITEMS_PER_REQUEST = 50
        const val TAG = "ItemRepositoryImpl"
    }
}
