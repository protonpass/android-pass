package me.proton.core.pass.data.repositories

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.extension.publicKeyRing
import me.proton.core.key.domain.repository.PublicAddressRepository
import me.proton.core.key.domain.repository.Source
import me.proton.core.network.domain.ApiException
import me.proton.core.pass.data.crypto.CreateItem
import me.proton.core.pass.data.crypto.OpenItem
import me.proton.core.pass.data.crypto.UpdateItem
import me.proton.core.pass.data.db.PassDatabase
import me.proton.core.pass.data.db.entities.ItemEntity
import me.proton.core.pass.data.extensions.itemType
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
import me.proton.core.pass.domain.Share
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.domain.ShareSelection
import me.proton.core.pass.domain.entity.NewAlias
import me.proton.core.pass.domain.key.ItemKey
import me.proton.core.pass.domain.key.VaultKey
import me.proton.core.pass.domain.repositories.ItemRepository
import me.proton.core.pass.domain.repositories.KeyPacketRepository
import me.proton.core.pass.domain.repositories.ShareRepository
import me.proton.core.pass.domain.repositories.VaultKeyRepository
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.extension.primary
import me.proton.core.user.domain.repository.UserAddressRepository
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
    ): Item {
        return withUserAddress(userId) { userAddress ->
            val (vaultKey, itemKey) =
                vaultKeyRepository.getLatestVaultItemKey(userAddress, share.id, share.signingKey)
            val body = createItem.createItem(vaultKey, itemKey, userAddress, contents)
            val itemResponse = remoteItemDataSource.createItem(userId, share.id, body)

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

    override suspend fun createAlias(userId: UserId, share: Share, newAlias: NewAlias): Item {
        return withUserAddress(userId) { userAddress ->
            val (vaultKey, itemKey) =
                vaultKeyRepository.getLatestVaultItemKey(userAddress, share.id, share.signingKey)
            val itemContents = ItemContents.Alias(title = newAlias.title, note = newAlias.note)
            val body = createItem.createItem(vaultKey, itemKey, userAddress, itemContents)
            val requestBody = CreateAliasRequest(
                prefix = newAlias.prefix,
                signedSuffix = newAlias.suffix.signedSuffix,
                mailboxes = listOf(newAlias.mailbox.id),
                item = body
            )

            val itemResponse = remoteItemDataSource.createAlias(userId, share.id, requestBody)
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
    ): Item {
        val keyPacket = keyPacketRepository.getLatestKeyPacketForItem(userId, share.id, item.id)

        return withUserAddress(userId) { userAddress ->
            val vaultKey =
                vaultKeyRepository.getVaultKeyById(
                    userAddress,
                    share.id,
                    share.signingKey,
                    keyPacket.rotationId
                )
            val itemKey =
                vaultKeyRepository.getItemKeyById(
                    userAddress,
                    share.id,
                    share.signingKey,
                    keyPacket.rotationId
                )

            val body = updateItem.updateItem(
                vaultKey,
                itemKey,
                keyPacket,
                userAddress,
                contents,
                item.revision
            )
            val itemResponse = remoteItemDataSource.updateItem(userId, share.id, item.id, body)

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

    override fun observeItems(
        userId: UserId,
        shareSelection: ShareSelection,
        itemState: ItemState
    ): Flow<List<Item>> {
        return when (shareSelection) {
            is ShareSelection.Share -> localItemDataSource.observeItemsForShare(
                userId,
                shareSelection.shareId,
                itemState
            )
            is ShareSelection.AllShares -> localItemDataSource.observeItems(userId, itemState)
        }.map { items ->
            // Detect if we have received the update from a logout
            val isAccountStillAvailable = accountManager.getAccount(userId).first() != null
            if (!isAccountStillAvailable) return@map emptyList()

            // The update does not come from a logout
            val userAddress = requireNotNull(userAddressRepository.getAddresses(userId).primary())
            refreshItemsIfNeeded(userAddress, shareSelection)
            items.map { entityToDomain(it) }
        }
    }

    override suspend fun getById(userId: UserId, shareId: ShareId, itemId: ItemId): Item {
        val item = localItemDataSource.getById(shareId, itemId)
        requireNotNull(item)
        return entityToDomain(item)
    }

    override suspend fun trashItem(userId: UserId, shareId: ShareId, itemId: ItemId) {
        val item = requireNotNull(localItemDataSource.getById(shareId, itemId))
        if (item.state == ItemState.Trashed.value) return

        val body =
            TrashItemsRequest(listOf(TrashItemRevision(itemId = item.id, revision = item.revision)))
        val response = remoteItemDataSource.sendToTrash(userId, shareId, body)

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

    @Suppress("SwallowedException")
    override suspend fun untrashItem(userId: UserId, shareId: ShareId, itemId: ItemId) {
        // Optimistically update the local database
        val originalItem = database.inTransaction {
            val item = requireNotNull(localItemDataSource.getById(shareId, itemId))
            if (item.state == ItemState.Active.value) return@inTransaction null
            val updatedItem = item.copy(
                state = ItemState.Active.value
            )
            localItemDataSource.upsertItem(updatedItem)
            item
        } ?: return

        // Perform the network request
        try {
            val body = TrashItemsRequest(
                listOf(
                    TrashItemRevision(
                        itemId = originalItem.id,
                        revision = originalItem.revision
                    )
                )
            )
            remoteItemDataSource.untrash(userId, shareId, body)
        } catch (e: ApiException) {
            // In case of an exception, restore the old version
            localItemDataSource.upsertItem(originalItem)
        }
    }

    override suspend fun clearTrash(userId: UserId) {
        val trashedItems = localItemDataSource.getTrashedItems(userId)
        val trashedPerShare = trashedItems.groupBy { it.shareId }
        coroutineScope {
            trashedPerShare.map { entry ->
                async {
                    val shareId = ShareId(entry.key)
                    val shareItems = entry.value
                    shareItems.chunked(MAX_TRASH_ITEMS_PER_REQUEST).forEach { items ->
                        val body =
                            TrashItemsRequest(items.map { TrashItemRevision(it.id, it.revision) })
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
            }.awaitAll()
        }
    }

    override suspend fun deleteItem(userId: UserId, shareId: ShareId, itemId: ItemId) {
        val item = requireNotNull(localItemDataSource.getById(shareId, itemId))
        if (item.state != ItemState.Trashed.value) return

        val body =
            TrashItemsRequest(listOf(TrashItemRevision(itemId = item.id, revision = item.revision)))
        remoteItemDataSource.delete(userId, shareId, body)
        localItemDataSource.delete(shareId, itemId)
    }

    private suspend fun refreshItemsIfNeeded(
        userAddress: UserAddress,
        shareSelection: ShareSelection
    ) {
        val shareIds = when (shareSelection) {
            is ShareSelection.AllShares -> shareRepository.observeShares(userAddress.userId).first()
            is ShareSelection.Share -> {
                val share = requireNotNull(
                    shareRepository.getById(
                        userAddress.userId,
                        shareSelection.shareId
                    )
                )
                listOf(share)
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
    }

    private suspend fun fetchItemsForShare(userAddress: UserAddress, share: Share): List<Item> {
        val items = remoteItemDataSource.getItems(userAddress.userId, share.id)
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

        return items.map { item ->
            val verifyKeys = requireNotNull(userKeys[item.signatureEmail])
            val vaultKey =
                vaultKeyRepository.getVaultKeyById(
                    userAddress,
                    share.id,
                    share.signingKey,
                    item.rotationId
                )
            val itemKey =
                vaultKeyRepository.getItemKeyById(
                    userAddress,
                    share.id,
                    share.signingKey,
                    item.rotationId
                )

            val entity =
                itemResponseToEntity(
                    userAddress,
                    item,
                    share,
                    verifyKeys,
                    listOf(vaultKey),
                    listOf(itemKey)
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
