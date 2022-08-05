package me.proton.core.pass.data.repositories

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.extension.publicKeyRing
import me.proton.core.key.domain.repository.PublicAddressRepository
import me.proton.core.key.domain.repository.Source
import me.proton.core.pass.data.crypto.CreateItem
import me.proton.core.pass.data.crypto.OpenItem
import me.proton.core.pass.data.crypto.UpdateItem
import me.proton.core.pass.data.db.PassDatabase
import me.proton.core.pass.data.db.entities.ItemEntity
import me.proton.core.pass.data.extensions.itemType
import me.proton.core.pass.data.local.LocalItemDataSource
import me.proton.core.pass.data.remote.RemoteItemDataSource
import me.proton.core.pass.data.responses.ItemRevision
import me.proton.core.pass.domain.*
import me.proton.core.pass.domain.key.ItemKey
import me.proton.core.pass.domain.key.VaultKey
import me.proton.core.pass.domain.repositories.ItemRepository
import me.proton.core.pass.domain.repositories.KeyPacketRepository
import me.proton.core.pass.domain.repositories.ShareRepository
import me.proton.core.pass.domain.repositories.VaultKeyRepository
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.extension.primary
import me.proton.core.user.domain.repository.UserAddressRepository

class ItemRepositoryImpl @Inject constructor(
    private val database: PassDatabase,
    private val cryptoContext: CryptoContext,
    private val userAddressRepository: UserAddressRepository,
    private val keyRepository: PublicAddressRepository,
    private val vaultKeyRepository: VaultKeyRepository,
    private val shareRepository: ShareRepository,
    private val createItem: CreateItem,
    private val updateItem: UpdateItem,
    private val localItemDataSource: LocalItemDataSource,
    private val remoteItemDataSource: RemoteItemDataSource,
    private val keyPacketRepository: KeyPacketRepository,
    private val openItem: OpenItem,
) : ItemRepository {
    override suspend fun createItem(
        userId: UserId,
        share: Share,
        contents: ItemContents
    ): Item {
        val userAddress = requireNotNull(userAddressRepository.getAddresses(userId).primary())
        val (vaultKey, itemKey) = vaultKeyRepository.getLatestVaultItemKey(userAddress, share.id, share.signingKey)
        val body = createItem.createItem(vaultKey, itemKey, userAddress, contents)
        val itemResponse = remoteItemDataSource.createItem(userId, share.id, body)

        val userPublicKeys = userAddress.publicKeyRing(cryptoContext).keys
        val entity = itemResponseToEntity(userAddress, itemResponse, share, userPublicKeys, listOf(vaultKey), listOf(itemKey))
        localItemDataSource.upsertItem(entity)
        return entityToDomain(entity)
    }

    override suspend fun updateItem(
        userId: UserId,
        share: Share,
        item: Item,
        contents: ItemContents
    ): Item {
        val keyPacket = keyPacketRepository.getLatestKeyPacketForItem(userId, share.id, item.id)

        val userAddress = requireNotNull(userAddressRepository.getAddresses(userId).primary())
        val vaultKey = vaultKeyRepository.getVaultKeyById(userAddress, share.id, share.signingKey, keyPacket.rotationId)
        val itemKey = vaultKeyRepository.getItemKeyById(userAddress, share.id, share.signingKey, keyPacket.rotationId)

        val body = updateItem.updateItem(vaultKey, itemKey, keyPacket, userAddress, contents, item.revision)
        val itemResponse = remoteItemDataSource.updateItem(userId, share.id, item.id, body)

        val userPublicKeys = userAddress.publicKeyRing(cryptoContext).keys
        val entity = itemResponseToEntity(userAddress, itemResponse, share, userPublicKeys, listOf(vaultKey), listOf(itemKey))
        localItemDataSource.upsertItem(entity)
        return entityToDomain(entity)
    }

    override fun observeItems(userId: UserId, shareSelection: ShareSelection): Flow<List<Item>> {
        return when (shareSelection) {
            is ShareSelection.Share -> localItemDataSource.observeItemsForShare(userId, shareSelection.shareId)
            is ShareSelection.AllShares -> localItemDataSource.observeItems(userId)
        }.map { items ->
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

    override suspend fun deleteItem(userId: UserId, shareId: ShareId, itemId: ItemId) {
        localItemDataSource.delete(shareId, itemId)
        remoteItemDataSource.delete(userId, shareId, itemId)
    }

    private suspend fun refreshItemsIfNeeded(
        userAddress: UserAddress,
        shareSelection: ShareSelection
    ) {
        val shareIds = when (shareSelection) {
            is ShareSelection.AllShares -> shareRepository.observeShares(userAddress.userId).first()
            is ShareSelection.Share -> {
                val share = requireNotNull(shareRepository.getById(userAddress.userId, shareSelection.shareId))
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
                val publicAddress = keyRepository.getPublicAddress(userAddress.userId, it, source = Source.LocalIfAvailable)
                publicAddress.keys.publicKeyRing().keys
            }

        return items.map { item ->
            val verifyKeys = requireNotNull(userKeys[item.signatureEmail])
            val vaultKey = vaultKeyRepository.getVaultKeyById(userAddress, share.id, share.signingKey, item.rotationId)
            val itemKey = vaultKeyRepository.getItemKeyById(userAddress, share.id, share.signingKey, item.rotationId)

            val entity = itemResponseToEntity(userAddress, item, share, verifyKeys, listOf(vaultKey), listOf(itemKey))
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
            encryptedNote = item.note
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
}
