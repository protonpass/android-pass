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
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.flatMap
import proton.android.pass.common.api.map
import proton.android.pass.common.api.toOption
import proton.android.pass.common.api.transpose
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.error.CryptoException
import proton.android.pass.crypto.api.usecases.CreateItem
import proton.android.pass.crypto.api.usecases.OpenItem
import proton.android.pass.crypto.api.usecases.UpdateItem
import proton.android.pass.data.api.ItemCountSummary
import proton.android.pass.data.api.PendingEventList
import proton.android.pass.data.api.errors.CannotRemoveNotTrashedItemError
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.db.entities.ItemEntity
import proton.android.pass.data.impl.extensions.allowedApps
import proton.android.pass.data.impl.extensions.hasPackageName
import proton.android.pass.data.impl.extensions.hasWebsite
import proton.android.pass.data.impl.extensions.itemType
import proton.android.pass.data.impl.extensions.serializeToProto
import proton.android.pass.data.impl.extensions.toCrypto
import proton.android.pass.data.impl.extensions.toItemRevision
import proton.android.pass.data.impl.extensions.toRequest
import proton.android.pass.data.impl.extensions.with
import proton.android.pass.data.impl.extensions.withUrl
import proton.android.pass.data.impl.local.LocalItemDataSource
import proton.android.pass.data.impl.remote.RemoteItemDataSource
import proton.android.pass.data.impl.requests.CreateAliasRequest
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
import proton.pass.domain.entity.PackageName
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
    private val itemKeyRepository: ItemKeyRepository,
    private val encryptionContextProvider: EncryptionContextProvider
) : BaseRepository(userAddressRepository), ItemRepository {

    @Suppress("TooGenericExceptionCaught")
    override suspend fun createItem(
        userId: UserId,
        share: Share,
        contents: ItemContents
    ): LoadingResult<Item> = withContext(Dispatchers.IO) {
        withUserAddress(userId) { userAddress ->
            val shareKey = shareKeyRepository.getLatestKeyForShare(share.id).first()

            val body = try {
                createItem.create(shareKey, contents)
            } catch (e: RuntimeException) {
                PassLogger.w(TAG, e, "Error creating item")
                return@withUserAddress LoadingResult.Error(e)
            }

            remoteItemDataSource.createItem(userId, share.id, body.request.toRequest())
                .map { itemResponse ->
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
    }

    override suspend fun createAlias(
        userId: UserId,
        share: Share,
        newAlias: NewAlias
    ): LoadingResult<Item> = withContext(Dispatchers.IO) {
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

            remoteItemDataSource.createAlias(userId, share.id, requestBody)
                .map { itemResponse ->
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
    }

    override suspend fun updateItem(
        userId: UserId,
        share: Share,
        item: Item,
        contents: ItemContents
    ): LoadingResult<Item> = withContext(Dispatchers.IO) {
        performUpdate(
            userId,
            share,
            item,
            contents.serializeToProto()
        )
    }

    override fun observeItems(
        userId: UserId,
        shareSelection: ShareSelection,
        itemState: ItemState,
        itemTypeFilter: ItemTypeFilter
    ): Flow<LoadingResult<List<Item>>> =
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

                // The update does not come from a logout
                val userAddress =
                    requireNotNull(userAddressRepository.getAddresses(userId).primary())
                refreshItemsIfNeeded(userAddress, shareSelection)
                encryptionContextProvider.withEncryptionContext {
                    items.map { entityToDomain(this@withEncryptionContext, it) }
                }
            }
            .asLoadingResult()
            .flowOn(Dispatchers.IO)

    override suspend fun getById(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): LoadingResult<Item> =
        withContext(Dispatchers.IO) {
            val item = localItemDataSource.getById(shareId, itemId)
            requireNotNull(item)
            encryptionContextProvider.withEncryptionContext {
                LoadingResult.Success(entityToDomain(this@withEncryptionContext, item))
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
        packageName: Option<PackageName>,
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
            packageName,
            url
        )

        if (!needsToUpdate) {
            PassLogger.i(TAG, "Did not need to perform any update")
            return@withContext LoadingResult.Success(item)
        }

        val userId = accountManager.getPrimaryUserId().first()
            ?: throw CryptoException("UserId cannot be null")
        val shareResult = shareRepository.getById(userId, shareId)
        when (shareResult) {
            is LoadingResult.Error -> return@withContext LoadingResult.Error(shareResult.exception)
            LoadingResult.Loading -> return@withContext LoadingResult.Loading
            is LoadingResult.Success -> Unit
        }
        val share = shareResult.data ?: throw CryptoException("Share cannot be null")

        return@withContext performUpdate(userId, share, item, updatedContents)
    }

    override suspend fun refreshItems(userId: UserId, share: Share): LoadingResult<List<Item>> =
        withContext(Dispatchers.IO) {
            val address = requireNotNull(userAddressRepository.getAddresses(userId).primary())
            fetchItemsForShare(address, share)
        }

    override suspend fun refreshItems(userId: UserId, shareId: ShareId): LoadingResult<List<Item>> =
        withContext(Dispatchers.IO) {
            val share = getShare(userId, shareId)
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
            val share = getShare(userId, shareId)
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

    private suspend fun getShare(userId: UserId, shareId: ShareId): Share =
        when (val share = shareRepository.getById(userId, shareId)) {
            is LoadingResult.Success -> share.data
            is LoadingResult.Error -> throw share.exception
            LoadingResult.Loading -> throw IllegalStateException("shareRepository.getById cannot return Loading")
        } ?: throw IllegalStateException("Could not find share [shareId=${shareId.id}]")

    private fun updateItemContents(
        item: Item,
        itemProto: ItemV1.Item,
        packageName: Option<PackageName>,
        url: Option<String>
    ): Pair<Boolean, ItemV1.Item> {
        var needsToUpdate = false

        val itemContentsWithPackageName = when (packageName) {
            None -> itemProto
            is Some -> {
                if (itemProto.hasPackageName(packageName.value)) {
                    PassLogger.i(
                        TAG,
                        "Item already has this package name " +
                            "[shareId=${item.shareId}] [itemId=${item.id}] [packageName=$packageName]"
                    )
                    itemProto
                } else {
                    needsToUpdate = true
                    itemProto.with(packageName.value)
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
    ): LoadingResult<Item> {
        return withUserAddress(userId) { userAddress ->
            val (shareKey, itemKey) = itemKeyRepository
                .getLatestItemKey(userId, userAddress.addressId, share.id, item.id)
                .first()
            val body = updateItem.createRequest(
                itemKey,
                itemContents,
                item.revision
            )
            remoteItemDataSource.updateItem(userId, share.id, item.id, body.toRequest())
                .map { itemResponse ->
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
    }

    @Suppress("ReturnCount")
    private suspend fun refreshItemsIfNeeded(
        userAddress: UserAddress,
        shareSelection: ShareSelection
    ): LoadingResult<Unit> {
        val shareIds: List<Share> = when (shareSelection) {
            is ShareSelection.AllShares -> {
                when (val result = shareRepository.observeAllShares(userAddress.userId).first()) {
                    is LoadingResult.Error -> return LoadingResult.Error(result.exception)
                    LoadingResult.Loading -> return LoadingResult.Loading
                    is LoadingResult.Success -> result.data
                }
            }
            is ShareSelection.Share -> {
                when (
                    val result: LoadingResult<Share?> =
                        shareRepository.getById(userAddress.userId, shareSelection.shareId)
                ) {
                    is LoadingResult.Error -> return LoadingResult.Error(result.exception)
                    LoadingResult.Loading -> return LoadingResult.Loading
                    is LoadingResult.Success -> listOf(requireNotNull(result.data))
                }
            }
        }

        database.inTransaction {
            shareIds.forEach {
                val hasItems = localItemDataSource.hasItemsForShare(userAddress.userId, it.id)
                if (!hasItems) {
                    fetchItemsForShare(userAddress, it)
                }
            }
        }
        return LoadingResult.Success(Unit)
    }

    private suspend fun fetchItemsForShare(
        userAddress: UserAddress,
        share: Share
    ): LoadingResult<List<Item>> =
        remoteItemDataSource.getItems(userAddress.userId, share.id)
            .flatMap { items ->
                decryptItems(userAddress, share, items)
            }

    private suspend fun decryptItems(
        userAddress: UserAddress,
        share: Share,
        items: List<ItemRevision>
    ): LoadingResult<List<Item>> {
        val shareKeys = shareKeyRepository.getShareKeys(userAddress.userId, userAddress.addressId, share.id).first()
        return encryptionContextProvider.withEncryptionContextSuspendable {
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
                }.awaitAll().transpose()
            }
        }.map { itemsEntities ->
            val entities = itemsEntities.map { it.second }
            localItemDataSource.upsertItems(entities)

            itemsEntities.map { it.first }
        }
    }

    private fun decryptItem(
        encryptionContext: EncryptionContext,
        userAddress: UserAddress,
        share: Share,
        item: ItemRevision,
        shareKeys: List<ShareKey>
    ): LoadingResult<Pair<Item, ItemEntity>> {
        val entity = itemResponseToEntity(
            userAddress = userAddress,
            itemRevision = item,
            share = share,
            shareKeys = shareKeys
        )
        return LoadingResult.Success(entityToDomain(encryptionContext, entity) to entity)
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
    ): Item =
        Item(
            id = ItemId(entity.id),
            revision = entity.revision,
            shareId = ShareId(entity.shareId),
            itemType = entity.itemType(encryptionContext),
            title = entity.encryptedTitle,
            note = entity.encryptedNote,
            content = entity.encryptedContent,
            allowedPackageNames = entity.allowedApps(encryptionContext),
            modificationTime = Instant.fromEpochSeconds(entity.modifyTime),
            revisionCount = entity.revision,
            createTime = Instant.fromEpochSeconds(entity.createTime),
            lastAutofillTime = entity.lastUsedTime.toOption().map(Instant::fromEpochSeconds)
        )


    companion object {
        const val MAX_TRASH_ITEMS_PER_REQUEST = 50
        const val TAG = "ItemRepositoryImpl"
    }
}
