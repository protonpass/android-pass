package me.proton.android.pass.data.impl.repositories

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import me.proton.android.pass.data.api.ItemCountSummary
import me.proton.android.pass.data.api.PendingEventList
import me.proton.android.pass.data.api.errors.CannotRemoveNotTrashedItemError
import me.proton.android.pass.data.api.repositories.ItemRepository
import me.proton.android.pass.data.api.repositories.KeyPacketRepository
import me.proton.android.pass.data.api.repositories.ShareRepository
import me.proton.android.pass.data.api.repositories.VaultKeyRepository
import me.proton.android.pass.data.impl.crypto.CreateItem
import me.proton.android.pass.data.impl.crypto.CryptoException
import me.proton.android.pass.data.impl.crypto.OpenItem
import me.proton.android.pass.data.impl.crypto.UpdateItem
import me.proton.android.pass.data.impl.db.PassDatabase
import me.proton.android.pass.data.impl.db.entities.ItemEntity
import me.proton.android.pass.data.impl.extensions.allowedApps
import me.proton.android.pass.data.impl.extensions.hasPackageName
import me.proton.android.pass.data.impl.extensions.hasWebsite
import me.proton.android.pass.data.impl.extensions.itemType
import me.proton.android.pass.data.impl.extensions.serializeToProto
import me.proton.android.pass.data.impl.extensions.toItemRevision
import me.proton.android.pass.data.impl.extensions.with
import me.proton.android.pass.data.impl.extensions.withUrl
import me.proton.android.pass.data.impl.local.LocalItemDataSource
import me.proton.android.pass.data.impl.remote.RemoteItemDataSource
import me.proton.android.pass.data.impl.requests.CreateAliasRequest
import me.proton.android.pass.data.impl.requests.TrashItemRevision
import me.proton.android.pass.data.impl.requests.TrashItemsRequest
import me.proton.android.pass.data.impl.responses.ItemRevision
import me.proton.android.pass.log.PassLogger
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.extension.publicKeyRing
import me.proton.core.key.domain.repository.PublicAddressRepository
import me.proton.core.key.domain.repository.Source
import me.proton.core.network.domain.ApiException
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.extension.primary
import me.proton.core.user.domain.repository.UserAddressRepository
import me.proton.pass.common.api.None
import me.proton.pass.common.api.Option
import me.proton.pass.common.api.Result
import me.proton.pass.common.api.Some
import me.proton.pass.common.api.asResult
import me.proton.pass.common.api.map
import me.proton.pass.domain.Item
import me.proton.pass.domain.ItemContents
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ItemState
import me.proton.pass.domain.ItemType
import me.proton.pass.domain.KeyPacket
import me.proton.pass.domain.Share
import me.proton.pass.domain.ShareId
import me.proton.pass.domain.ShareSelection
import me.proton.pass.domain.entity.NewAlias
import me.proton.pass.domain.entity.PackageName
import me.proton.pass.domain.key.ItemKey
import me.proton.pass.domain.key.VaultKey
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
    private val openItem: OpenItem
) : BaseRepository(userAddressRepository), ItemRepository {

    override suspend fun createItem(
        userId: UserId,
        share: Share,
        contents: ItemContents,
        packageName: PackageName?
    ): Result<Item> = withUserAddress(userId) { userAddress ->
        val result =
            vaultKeyRepository.getLatestVaultItemKey(userAddress, share.id, share.signingKey)
        when (result) {
            is Result.Error -> return@withUserAddress Result.Error(result.exception)
            Result.Loading -> return@withUserAddress Result.Loading
            is Result.Success -> Unit
        }
        val (vaultKey, itemKey) = result.data
        val body = createItem.create(vaultKey, itemKey, userAddress, contents, packageName)
        remoteItemDataSource.createItem(userId, share.id, body)
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
                entityToDomain(entity)
            }
    }

    override suspend fun createAlias(
        userId: UserId,
        share: Share,
        newAlias: NewAlias
    ): Result<Item> = withUserAddress(userId) { userAddress ->
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
            item = body
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
                entityToDomain(entity)
            }
    }

    override suspend fun updateItem(
        userId: UserId,
        share: Share,
        item: Item,
        contents: ItemContents
    ): Result<Item> =
        performUpdate(
            userId,
            share,
            item,
            contents.serializeToProto()
        )

    override fun observeItems(
        userId: UserId,
        shareSelection: ShareSelection,
        itemState: ItemState
    ): Flow<Result<List<Item>>> =
        when (shareSelection) {
            is ShareSelection.Share -> localItemDataSource.observeItemsForShare(
                userId,
                shareSelection.shareId,
                itemState
            )
            is ShareSelection.AllShares -> localItemDataSource.observeItems(userId, itemState)
        }
            .map { items ->
                // Detect if we have received the update from a logout
                val isAccountStillAvailable = accountManager.getAccount(userId).first() != null
                if (!isAccountStillAvailable) return@map emptyList()

                // The update does not come from a logout
                val userAddress =
                    requireNotNull(userAddressRepository.getAddresses(userId).primary())
                refreshItemsIfNeeded(userAddress, shareSelection)
                items.map { entityToDomain(it) }
            }
            .asResult()

    override suspend fun getById(userId: UserId, shareId: ShareId, itemId: ItemId): Result<Item> {
        val item = localItemDataSource.getById(shareId, itemId)
        requireNotNull(item)
        return Result.Success(entityToDomain(item))
    }

    override suspend fun trashItem(userId: UserId, shareId: ShareId, itemId: ItemId): Result<Unit> {
        val item = requireNotNull(localItemDataSource.getById(shareId, itemId))
        if (item.state == ItemState.Trashed.value) return Result.Error(
            CannotRemoveNotTrashedItemError()
        )

        val body =
            TrashItemsRequest(listOf(TrashItemRevision(itemId = item.id, revision = item.revision)))

        return remoteItemDataSource.sendToTrash(userId, shareId, body)
            .map { response ->
                database.inTransaction {
                    response.items.find { it.itemId == item.id }?.let {
                        val updatedItem = item.copy(
                            revision = it.revision,
                            state = ItemState.Trashed.value
                        )
                        localItemDataSource.upsertItem(updatedItem)
                    }
                }
            }
    }

    @Suppress("SwallowedException")
    override suspend fun untrashItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Result<Unit> {
        // Optimistically update the local database
        val originalItem: ItemEntity = database.inTransaction {
            val item = requireNotNull(localItemDataSource.getById(shareId, itemId))
            if (item.state == ItemState.Active.value) return@inTransaction null
            val updatedItem = item.copy(
                state = ItemState.Active.value
            )
            localItemDataSource.upsertItem(updatedItem)
            item
        } ?: return Result.Error(IllegalStateException("UnTrash item could not be updated locally"))

        // Perform the network request
        return try {
            val body = TrashItemsRequest(
                listOf(TrashItemRevision(originalItem.id, originalItem.revision))
            )
            remoteItemDataSource.untrash(userId, shareId, body)
            Result.Success(Unit)
        } catch (e: ApiException) {
            // In case of an exception, restore the old version
            localItemDataSource.upsertItem(originalItem)
            Result.Error(e)
        }
    }

    override suspend fun clearTrash(userId: UserId): Result<Unit> {
        val trashedItems = localItemDataSource.getTrashedItems(userId)
        val trashedPerShare = trashedItems.groupBy { it.shareId }
        coroutineScope {
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
        }
        return Result.Success(Unit)
    }

    override suspend fun deleteItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Result<Unit> {
        val item = requireNotNull(localItemDataSource.getById(shareId, itemId))
        if (item.state != ItemState.Trashed.value) return Result.Success(Unit)

        val body =
            TrashItemsRequest(listOf(TrashItemRevision(itemId = item.id, revision = item.revision)))
        return remoteItemDataSource.delete(userId, shareId, body)
            .map {
                localItemDataSource.delete(shareId, itemId)
                Result.Success(Unit)
            }
    }

    @Suppress("ReturnCount")
    override suspend fun addPackageAndUrlToItem(
        shareId: ShareId,
        itemId: ItemId,
        packageName: Option<PackageName>,
        url: Option<String>
    ): Result<Item> {
        val itemEntity = requireNotNull(localItemDataSource.getById(shareId, itemId))
        val item = entityToDomain(itemEntity)

        val itemContents = item.content.decrypt(cryptoContext.keyStoreCrypto)
        val itemProto = ItemV1.Item.parseFrom(itemContents.array)

        val (needsToUpdate, updatedContents) = updateItemContents(
            item,
            itemProto,
            packageName,
            url
        )

        if (!needsToUpdate) {
            PassLogger.i(TAG, "Did not need to perform any update")
            return Result.Success(item)
        }

        val userId = accountManager.getPrimaryUserId().first()
            ?: throw CryptoException("UserId cannot be null")
        val shareResult = shareRepository.getById(userId, shareId)
        when (shareResult) {
            is Result.Error -> return Result.Error(shareResult.exception)
            Result.Loading -> return Result.Loading
            is Result.Success -> Unit
        }
        val share = shareResult.data ?: throw CryptoException("Share cannot be null")

        return performUpdate(userId, share, item, updatedContents)
    }

    override suspend fun refreshItems(userId: UserId, share: Share): Result<List<Item>> {
        val address = requireNotNull(userAddressRepository.getAddresses(userId).primary())
        return fetchItemsForShare(address, share)
    }

    override suspend fun refreshItems(userId: UserId, shareId: ShareId): Result<List<Item>> {
        val share = getShare(userId, shareId)
        return refreshItems(userId, share)
    }

    override suspend fun applyEvents(
        userId: UserId,
        addressId: AddressId,
        shareId: ShareId,
        events: PendingEventList
    ) {
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

    override fun observeItemCountSummary(
        userId: UserId,
        shareId: ShareId
    ): Flow<ItemCountSummary> = localItemDataSource.observeItemCountSummary(userId, shareId)


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
            remoteItemDataSource.updateItem(userId, share.id, item.id, body)
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
                    entityToDomain(entity)
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
                when (val result = shareRepository.observeShares(userAddress.userId).first()) {
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
            .map { items ->
                val userKeys = items
                    .map { it.signatureEmail }
                    .distinct()
                    .associateWith {
                        val publicAddress =
                            keyRepository.getPublicAddress(
                                userAddress.userId,
                                it,
                                source = Source.LocalIfAvailable
                            )
                        publicAddress.keys.publicKeyRing().keys
                    }

                items.map { item ->
                    val verifyKeys = requireNotNull(userKeys[item.signatureEmail])
                    val vaultKeyResult: Result<VaultKey> = vaultKeyRepository
                        .getVaultKeyById(
                            userAddress,
                            share.id,
                            share.signingKey,
                            item.rotationId
                        )
                    when (vaultKeyResult) {
                        is Result.Error -> return Result.Error(vaultKeyResult.exception)
                        Result.Loading -> return Result.Loading
                        is Result.Success -> Unit
                    }
                    val itemKeyResult: Result<ItemKey> = vaultKeyRepository
                        .getItemKeyById(
                            userAddress,
                            share.id,
                            share.signingKey,
                            item.rotationId
                        )
                    when (itemKeyResult) {
                        is Result.Error -> return Result.Error(itemKeyResult.exception)
                        Result.Loading -> return Result.Loading
                        is Result.Success -> Unit
                    }
                    val entity =
                        itemResponseToEntity(
                            userAddress,
                            item,
                            share,
                            verifyKeys,
                            listOf(vaultKeyResult.data),
                            listOf(itemKeyResult.data)
                        )
                    localItemDataSource.upsertItem(entity)
                    entityToDomain(entity)
                }
            }

    private fun itemResponseToEntity(
        userAddress: UserAddress,
        itemRevision: ItemRevision,
        share: Share,
        verifyKeys: List<PublicKey>,
        vaultKeys: List<VaultKey>,
        itemKeys: List<ItemKey>
    ): ItemEntity {
        val item = openItem.open(itemRevision, share, verifyKeys, vaultKeys, itemKeys)
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
            encryptedContent = item.content,
            encryptedTitle = item.title,
            encryptedNote = item.note,
            aliasEmail = itemRevision.aliasEmail
        )
    }

    private fun entityToDomain(entity: ItemEntity): Item =
        Item(
            id = ItemId(entity.id),
            revision = entity.revision,
            shareId = ShareId(entity.shareId),
            itemType = entity.itemType(cryptoContext.keyStoreCrypto),
            title = entity.encryptedTitle,
            note = entity.encryptedNote,
            content = entity.encryptedContent,
            allowedPackageNames = entity.allowedApps(cryptoContext.keyStoreCrypto)
        )

    companion object {
        const val MAX_TRASH_ITEMS_PER_REQUEST = 50
        const val TAG = "ItemRepositoryImpl"
    }
}
