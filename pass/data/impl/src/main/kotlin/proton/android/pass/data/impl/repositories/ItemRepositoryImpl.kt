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
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.extension.publicKeyRing
import me.proton.core.key.domain.repository.PublicAddressRepository
import me.proton.core.key.domain.repository.Source
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.extension.primary
import me.proton.core.user.domain.repository.UserAddressRepository
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Result
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.asResult
import proton.android.pass.common.api.flatMap
import proton.android.pass.common.api.map
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
import proton.android.pass.data.api.repositories.KeyPacketRepository
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.repositories.VaultKeyRepository
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
import proton.pass.domain.KeyPacket
import proton.pass.domain.Share
import proton.pass.domain.ShareId
import proton.pass.domain.ShareSelection
import proton.pass.domain.entity.NewAlias
import proton.pass.domain.entity.PackageName
import proton.pass.domain.key.ItemKey
import proton.pass.domain.key.VaultKey
import proton_pass_item_v1.ItemV1
import javax.inject.Inject

@Suppress("LongParameterList", "TooManyFunctions", "LargeClass")
class ItemRepositoryImpl @Inject constructor(
    private val database: PassDatabase,
    private val cryptoContext: CryptoContext,
    private val accountManager: AccountManager,
    override val userAddressRepository: UserAddressRepository,
    private val keyRepository: PublicAddressRepository,
    private val vaultKeyRepository: VaultKeyRepository,
    private val shareRepository: ShareRepository,
    private val createItem: CreateItem,
    private val updateItem: UpdateItem,
    private val localItemDataSource: LocalItemDataSource,
    private val remoteItemDataSource: RemoteItemDataSource,
    private val keyPacketRepository: KeyPacketRepository,
    private val openItem: OpenItem,
    private val encryptionContextProvider: EncryptionContextProvider
) : BaseRepository(userAddressRepository), ItemRepository {

    @Suppress("TooGenericExceptionCaught")
    override suspend fun createItem(
        userId: UserId,
        share: Share,
        contents: ItemContents
    ): Result<Item> = withContext(Dispatchers.IO) {
        withUserAddress(userId) { userAddress ->
            val result =
                vaultKeyRepository.getLatestVaultItemKey(userAddress, share.id, share.signingKey)
            when (result) {
                is Result.Error -> return@withUserAddress Result.Error(result.exception)
                Result.Loading -> return@withUserAddress Result.Loading
                is Result.Success -> Unit
            }
            val (vaultKey, itemKey) = result.data

            val body = try {
                createItem.create(vaultKey, itemKey, userAddress, contents)
            } catch (e: RuntimeException) {
                PassLogger.w(TAG, e, "Error creating item")
                return@withUserAddress Result.Error(e)
            }

            remoteItemDataSource.createItem(userId, share.id, body.toRequest())
                .map { itemResponse ->
                    val userPublicKeys = userAddress.publicKeyRing(cryptoContext).keys
                    val entity = itemResponseToEntity(
                        userAddress,
                        itemResponse,
                        share,
                        userPublicKeys,
                        listOf(vaultKey),
                        listOf(itemKey)
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
    ): Result<Item> = withContext(Dispatchers.IO) {
        withUserAddress(userId) { userAddress ->
            val result =
                vaultKeyRepository.getLatestVaultItemKey(userAddress, share.id, share.signingKey)
            when (result) {
                is Result.Error -> return@withUserAddress Result.Error(result.exception)
                Result.Loading -> return@withUserAddress Result.Loading
                is Result.Success -> Unit
            }
            val (vaultKey, itemKey) = result.data
            val itemContents = ItemContents.Alias(title = newAlias.title, note = newAlias.note)
            val body = createItem.create(vaultKey, itemKey, userAddress, itemContents)

            val mailboxIds = newAlias.mailboxes.map { it.id }
            val requestBody = CreateAliasRequest(
                prefix = newAlias.prefix,
                signedSuffix = newAlias.suffix.signedSuffix,
                mailboxes = mailboxIds,
                item = body.toRequest()
            )

            remoteItemDataSource.createAlias(userId, share.id, requestBody)
                .map { itemResponse ->
                    val userPublicKeys = userAddress.publicKeyRing(cryptoContext).keys
                    val entity = itemResponseToEntity(
                        userAddress,
                        itemResponse,
                        share,
                        userPublicKeys,
                        listOf(vaultKey),
                        listOf(itemKey)
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
    ): Result<Item> = withContext(Dispatchers.IO) {
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
    ): Flow<Result<List<Item>>> =
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
            .asResult()
            .flowOn(Dispatchers.IO)

    override suspend fun getById(userId: UserId, shareId: ShareId, itemId: ItemId): Result<Item> =
        withContext(Dispatchers.IO) {
            val item = localItemDataSource.getById(shareId, itemId)
            requireNotNull(item)
            encryptionContextProvider.withEncryptionContext {
                Result.Success(entityToDomain(this@withEncryptionContext, item))
            }
        }

    override suspend fun trashItem(userId: UserId, shareId: ShareId, itemId: ItemId): Result<Unit> =
        withContext(Dispatchers.IO) {
            val item = requireNotNull(localItemDataSource.getById(shareId, itemId))
            if (item.state == ItemState.Trashed.value) {
                return@withContext Result.Error(CannotRemoveNotTrashedItemError())
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
    ): Result<Unit> = withContext(Dispatchers.IO) {
        // Optimistically update the local database
        val originalItem: ItemEntity = database.inTransaction {
            val item = requireNotNull(localItemDataSource.getById(shareId, itemId))
            if (item.state == ItemState.Active.value) return@inTransaction null
            val updatedItem = item.copy(
                state = ItemState.Active.value
            )
            localItemDataSource.upsertItem(updatedItem)
            item
        }
            ?: return@withContext Result.Error(IllegalStateException("UnTrash item could not be updated locally"))

        // Perform the network request
        val body = TrashItemsRequest(
            listOf(TrashItemRevision(originalItem.id, originalItem.revision))
        )
        return@withContext when (val res = remoteItemDataSource.untrash(userId, shareId, body)) {
            Result.Loading -> Result.Loading
            is Result.Error -> {
                localItemDataSource.upsertItem(originalItem)
                Result.Error(res.exception)
            }
            is Result.Success -> Result.Success(Unit)
        }
    }

    override suspend fun clearTrash(userId: UserId): Result<Unit> = withContext(Dispatchers.IO) {
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
        Result.Success(Unit)
    }

    override suspend fun restoreItems(userId: UserId): Result<Unit> = withContext(Dispatchers.IO) {
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
        Result.Success(Unit)
    }

    override suspend fun deleteItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val item = requireNotNull(localItemDataSource.getById(shareId, itemId))
        if (item.state != ItemState.Trashed.value) return@withContext Result.Success(Unit)

        val body =
            TrashItemsRequest(listOf(TrashItemRevision(itemId = item.id, revision = item.revision)))
        return@withContext when (val result = remoteItemDataSource.delete(userId, shareId, body)) {
            is Result.Error -> Result.Error(result.exception)
            Result.Loading -> Result.Loading
            is Result.Success -> {
                localItemDataSource.delete(shareId, itemId)
                Result.Success(Unit)
            }
        }
    }

    @Suppress("ReturnCount")
    override suspend fun addPackageAndUrlToItem(
        shareId: ShareId,
        itemId: ItemId,
        packageName: Option<PackageName>,
        url: Option<String>
    ): Result<Item> = withContext(Dispatchers.IO) {
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
            return@withContext Result.Success(item)
        }

        val userId = accountManager.getPrimaryUserId().first()
            ?: throw CryptoException("UserId cannot be null")
        val shareResult = shareRepository.getById(userId, shareId)
        when (shareResult) {
            is Result.Error -> return@withContext Result.Error(shareResult.exception)
            Result.Loading -> return@withContext Result.Loading
            is Result.Success -> Unit
        }
        val share = shareResult.data ?: throw CryptoException("Share cannot be null")

        return@withContext performUpdate(userId, share, item, updatedContents)
    }

    override suspend fun refreshItems(userId: UserId, share: Share): Result<List<Item>> =
        withContext(Dispatchers.IO) {
            val address = requireNotNull(userAddressRepository.getAddresses(userId).primary())
            fetchItemsForShare(address, share)
        }

    override suspend fun refreshItems(userId: UserId, shareId: ShareId): Result<List<Item>> =
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

            val userEmails = events.updatedItems
                .map { it.signatureEmail }
                .distinct()
            val userKeys = getUserKeys(userId, userEmails)

            val keys = events.updatedItems
                .map { it.rotationId }
                .distinct()
                .associateWith { rotationId -> getVaultKeyItemKey(userAddress, share, rotationId) }

            val updateAsEntities = events.updatedItems.map {
                val verifyKeys = requireNotNull(userKeys[it.signatureEmail])
                val (vaultKey, itemKey) = requireNotNull(keys[it.rotationId])
                itemResponseToEntity(
                    userAddress,
                    it.toItemRevision(),
                    share,
                    verifyKeys,
                    listOf(vaultKey),
                    listOf(itemKey)
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
        shareId: ShareId
    ): Flow<ItemCountSummary> =
        localItemDataSource.observeItemCountSummary(userId, shareId)
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
            is Result.Success -> share.data
            is Result.Error -> {
                val error =
                    share.exception ?: IllegalStateException("Got error in shareRepository.getById")
                throw error
            }
            Result.Loading -> throw IllegalStateException("shareRepository.getById cannot return Loading")
        } ?: throw IllegalStateException("Could not find share [shareId=${shareId.id}]")

    private suspend fun getUserKeys(userId: UserId, emails: List<String>) = emails
        .associateWith {
            val publicAddress =
                keyRepository.getPublicAddress(
                    userId,
                    it,
                    source = Source.LocalIfAvailable
                )
            publicAddress.keys.publicKeyRing().keys
        }

    @Suppress("ThrowsCount")
    private suspend fun getVaultKeyItemKey(
        userAddress: UserAddress,
        share: Share,
        rotationId: String
    ): Pair<VaultKey, ItemKey> {
        val vaultKeyResult =
            vaultKeyRepository.getVaultKeyById(userAddress, share.id, share.signingKey, rotationId)
        val vaultKey = when (vaultKeyResult) {
            is Result.Success -> vaultKeyResult.data
            is Result.Error -> {
                val error = vaultKeyResult.exception
                    ?: IllegalStateException("Got error in vaultKeyRepository.getVaultKeyById")
                throw error
            }
            Result.Loading -> throw IllegalStateException("vaultKeyRepository.getVaultKeyById cannot return Loading")
        }

        val itemKeyResult =
            vaultKeyRepository.getItemKeyById(userAddress, share.id, share.signingKey, rotationId)
        val itemKey = when (itemKeyResult) {
            is Result.Success -> itemKeyResult.data
            is Result.Error -> {
                val error = itemKeyResult.exception
                    ?: IllegalStateException("Got error in vaultKeyRepository.getItemKeyById")
                throw error
            }
            Result.Loading -> throw IllegalStateException("vaultKeyRepository.getItemKeyById cannot return Loading")
        }

        return Pair(vaultKey, itemKey)
    }

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
    ): Result<Item> {
        val keyPacketResult: Result<KeyPacket> =
            keyPacketRepository.getLatestKeyPacketForItem(userId, share.id, item.id)
        when (keyPacketResult) {
            is Result.Error -> return Result.Error(keyPacketResult.exception)
            Result.Loading -> return Result.Loading
            is Result.Success -> Unit
        }
        return withUserAddress(userId) { userAddress ->
            val vaultKeyResult: Result<VaultKey> = vaultKeyRepository.getVaultKeyById(
                userAddress,
                share.id,
                share.signingKey,
                keyPacketResult.data.rotationId
            )
            when (vaultKeyResult) {
                is Result.Error -> return@withUserAddress Result.Error(vaultKeyResult.exception)
                Result.Loading -> return@withUserAddress Result.Loading
                is Result.Success -> Unit
            }
            val itemKeyResult: Result<ItemKey> = vaultKeyRepository.getItemKeyById(
                userAddress,
                share.id,
                share.signingKey,
                keyPacketResult.data.rotationId
            )
            when (itemKeyResult) {
                is Result.Error -> return@withUserAddress Result.Error(itemKeyResult.exception)
                Result.Loading -> return@withUserAddress Result.Loading
                is Result.Success -> Unit
            }
            val body = updateItem.createRequest(
                vaultKeyResult.data,
                itemKeyResult.data,
                keyPacketResult.data,
                userAddress,
                itemContents,
                item.revision
            )
            remoteItemDataSource.updateItem(userId, share.id, item.id, body.toRequest())
                .map { itemResponse ->
                    val userPublicKeys = userAddress.publicKeyRing(cryptoContext).keys
                    val entity = itemResponseToEntity(
                        userAddress,
                        itemResponse,
                        share,
                        userPublicKeys,
                        listOf(vaultKeyResult.data),
                        listOf(itemKeyResult.data)
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
    ): Result<Unit> {
        val shareIds: List<Share> = when (shareSelection) {
            is ShareSelection.AllShares -> {
                when (val result = shareRepository.observeAllShares(userAddress.userId).first()) {
                    is Result.Error -> return Result.Error(result.exception)
                    Result.Loading -> return Result.Loading
                    is Result.Success -> result.data
                }
            }
            is ShareSelection.Share -> {
                when (
                    val result: Result<Share?> =
                        shareRepository.getById(userAddress.userId, shareSelection.shareId)
                ) {
                    is Result.Error -> return Result.Error(result.exception)
                    Result.Loading -> return Result.Loading
                    is Result.Success -> listOf(requireNotNull(result.data))
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
        return Result.Success(Unit)
    }

    private suspend fun fetchItemsForShare(
        userAddress: UserAddress,
        share: Share
    ): Result<List<Item>> =
        remoteItemDataSource.getItems(userAddress.userId, share.id)
            .flatMap { items ->
                decryptItems(userAddress, share, items)
            }

    private suspend fun decryptItems(
        userAddress: UserAddress,
        share: Share,
        items: List<ItemRevision>
    ): Result<List<Item>> {
        val userEmails = items
            .map { it.signatureEmail }
            .distinct()
        val userKeys = getUserKeys(userAddress.userId, userEmails)

        val (vaultKeys, itemKeys) = when (
            val res =
                getVaultKeysItemKeys(userAddress, share, items)
        ) {
            Result.Loading -> return Result.Loading
            is Result.Error -> return res
            is Result.Success -> res.data
        }

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
                            vaultKeys = vaultKeys,
                            itemKeys = itemKeys,
                            userKeys = userKeys
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

    @Suppress("ReturnCount")
    private suspend fun getVaultKeysItemKeys(
        userAddress: UserAddress,
        share: Share,
        items: List<ItemRevision>
    ): Result<Pair<Map<String, VaultKey>, Map<String, ItemKey>>> {
        val rotations = items.map { it.rotationId }.distinct()
        val vaultKeys = rotations.associateWith { rotation ->
            val vaultKeyResult: Result<VaultKey> = vaultKeyRepository.getVaultKeyById(
                userAddress,
                share.id,
                share.signingKey,
                rotation
            )
            when (vaultKeyResult) {
                is Result.Error -> return Result.Error(vaultKeyResult.exception)
                Result.Loading -> return Result.Loading
                is Result.Success -> vaultKeyResult.data
            }
        }
        val itemKeys = rotations.associateWith { rotation ->
            val itemKeyResult: Result<ItemKey> = vaultKeyRepository.getItemKeyById(
                userAddress,
                share.id,
                share.signingKey,
                rotation
            )
            when (itemKeyResult) {
                is Result.Error -> return Result.Error(itemKeyResult.exception)
                Result.Loading -> return Result.Loading
                is Result.Success -> itemKeyResult.data
            }
        }

        return Result.Success(vaultKeys to itemKeys)
    }

    private fun decryptItem(
        encryptionContext: EncryptionContext,
        userAddress: UserAddress,
        share: Share,
        item: ItemRevision,
        vaultKeys: Map<String, VaultKey>,
        itemKeys: Map<String, ItemKey>,
        userKeys: Map<String, List<PublicKey>>
    ): Result<Pair<Item, ItemEntity>> {
        val verifyKeys = requireNotNull(userKeys[item.signatureEmail])
        val vaultKey = requireNotNull(vaultKeys[item.rotationId])
        val itemKey = requireNotNull(itemKeys[item.rotationId])

        val entity = itemResponseToEntity(
            userAddress = userAddress,
            itemRevision = item,
            share = share,
            verifyKeys = verifyKeys,
            vaultKeys = listOf(vaultKey),
            itemKeys = listOf(itemKey)
        )
        return Result.Success(entityToDomain(encryptionContext, entity) to entity)
    }

    private fun itemResponseToEntity(
        userAddress: UserAddress,
        itemRevision: ItemRevision,
        share: Share,
        verifyKeys: List<PublicKey>,
        vaultKeys: List<VaultKey>,
        itemKeys: List<ItemKey>
    ): ItemEntity {
        val item = openItem.open(itemRevision.toCrypto(), share, verifyKeys, vaultKeys, itemKeys)
        return ItemEntity(
            id = itemRevision.itemId,
            userId = userAddress.userId.id,
            addressId = userAddress.addressId.id,
            shareId = share.id.id,
            revision = itemRevision.revision,
            contentFormatVersion = itemRevision.contentFormatVersion,
            rotationId = itemRevision.rotationId,
            content = itemRevision.content,
            userSignature = itemRevision.userSignature,
            itemKeySignature = itemRevision.itemKeySignature,
            state = itemRevision.state,
            itemType = item.itemType.toWeightedInt(),
            signatureEmail = itemRevision.signatureEmail,
            createTime = itemRevision.createTime,
            modifyTime = itemRevision.modifyTime,
            lastUsedTime = itemRevision.lastUseTime,
            encryptedContent = item.content,
            encryptedTitle = item.title,
            encryptedNote = item.note,
            aliasEmail = itemRevision.aliasEmail
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
            modificationTime = Instant.fromEpochSeconds(entity.modifyTime)
        )

    companion object {
        const val MAX_TRASH_ITEMS_PER_REQUEST = 50
        const val TAG = "ItemRepositoryImpl"
    }
}
