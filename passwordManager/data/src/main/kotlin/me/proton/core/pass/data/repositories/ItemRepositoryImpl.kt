package me.proton.core.pass.data.repositories

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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
import me.proton.core.pass.common.api.Result
import me.proton.core.pass.common.api.asResult
import me.proton.core.pass.common.api.map
import me.proton.core.pass.data.crypto.CreateItem
import me.proton.core.pass.data.crypto.CryptoException
import me.proton.core.pass.data.crypto.OpenItem
import me.proton.core.pass.data.crypto.UpdateItem
import me.proton.core.pass.data.db.PassDatabase
import me.proton.core.pass.data.db.entities.ItemEntity
import me.proton.core.pass.data.extensions.hasPackageName
import me.proton.core.pass.data.extensions.itemType
import me.proton.core.pass.data.extensions.serializeToProto
import me.proton.core.pass.data.extensions.with
import me.proton.core.pass.data.local.LocalItemDataSource
import me.proton.core.pass.data.remote.RemoteItemDataSource
import me.proton.core.pass.data.requests.CreateAliasRequest
import me.proton.core.pass.data.requests.TrashItemRevision
import me.proton.core.pass.data.requests.TrashItemsRequest
import me.proton.core.pass.data.responses.ItemRevision
import me.proton.core.pass.domain.Item
import me.proton.core.pass.domain.ItemContents
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ItemState
import me.proton.core.pass.domain.KeyPacket
import me.proton.core.pass.domain.Share
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.domain.ShareSelection
import me.proton.core.pass.domain.entity.NewAlias
import me.proton.core.pass.domain.entity.PackageName
import me.proton.core.pass.domain.key.ItemKey
import me.proton.core.pass.domain.key.VaultKey
import me.proton.core.pass.domain.repositories.ItemRepository
import me.proton.core.pass.domain.repositories.KeyPacketRepository
import me.proton.core.pass.domain.repositories.ShareRepository
import me.proton.core.pass.domain.repositories.VaultKeyRepository
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.extension.primary
import me.proton.core.user.domain.repository.UserAddressRepository
import proton_pass_item_v1.ItemV1
import javax.inject.Inject

@Suppress("LongParameterList", "TooManyFunctions")
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
        contents: ItemContents
    ): Result<Item> = withUserAddress(userId) { userAddress ->
        val result =
            vaultKeyRepository.getLatestVaultItemKey(userAddress, share.id, share.signingKey)
        when (result) {
            is Result.Error -> return@withUserAddress Result.Error(result.exception)
            Result.Loading -> return@withUserAddress Result.Loading
            is Result.Success -> Unit
        }
        val (vaultKey, itemKey) = result.data
        val body = createItem.createItem(vaultKey, itemKey, userAddress, contents)
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
        val body = createItem.createItem(vaultKey, itemKey, userAddress, itemContents)

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
        if (item.state == ItemState.Trashed.value) return Result.Success(Unit)

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
    override suspend fun addPackageToItem(shareId: ShareId, itemId: ItemId, packageName: PackageName): Result<Item> {
        val itemEntity = requireNotNull(localItemDataSource.getById(shareId, itemId))
        val item = entityToDomain(itemEntity)

        val itemContents = item.content.decrypt(cryptoContext.keyStoreCrypto)
        val newItemContents = ItemV1.Item.parseFrom(itemContents.array)

        if (newItemContents.hasPackageName(packageName)) {
            PassLogger.i(
                "ItemRepositoryImpl",
                "Item already has this package name [shareId=$shareId] [itemId=$itemId] [packageName=$packageName]"
            )
            return Result.Success(item)
        }
        val updatedContents = newItemContents.with(packageName)

        val userId = accountManager.getPrimaryUserId().first() ?: throw CryptoException("UserId cannot be null")
        val shareResult = shareRepository.getById(userId, shareId)
        when (shareResult) {
            is Result.Error -> return Result.Error(shareResult.exception)
            Result.Loading -> return Result.Loading
            is Result.Success -> Unit
        }
        val share = shareResult.data ?: throw CryptoException("Share cannot be null")

        return performUpdate(userId, share, item, updatedContents)
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
        val item = openItem.openItem(itemRevision, share, verifyKeys, vaultKeys, itemKeys)
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
            itemType = entity.itemType(cryptoContext),
            title = entity.encryptedTitle,
            note = entity.encryptedNote,
            content = entity.encryptedContent
        )

    companion object {
        const val MAX_TRASH_ITEMS_PER_REQUEST = 50
    }
}
