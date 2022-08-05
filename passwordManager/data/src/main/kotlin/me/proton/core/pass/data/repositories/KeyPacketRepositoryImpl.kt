package me.proton.core.pass.data.repositories

import javax.inject.Inject
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.pgp.PGPHeader
import me.proton.core.domain.entity.UserId
import me.proton.core.pass.data.remote.RemoteKeyPacketDataSource
import me.proton.core.pass.data.responses.KeyPacketInfo
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.KeyPacket
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.domain.key.publicKey
import me.proton.core.pass.domain.repositories.KeyPacketRepository
import me.proton.core.pass.domain.repositories.ShareRepository
import me.proton.core.pass.domain.repositories.VaultKeyRepository
import me.proton.core.user.domain.extension.primary
import me.proton.core.user.domain.repository.UserAddressRepository

class KeyPacketRepositoryImpl @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val userAddressRepository: UserAddressRepository,
    private val shareRepository: ShareRepository,
    private val keyRepository: VaultKeyRepository,
    private val remoteKeyPacketDataSource: RemoteKeyPacketDataSource,
) : KeyPacketRepository {

    override suspend fun getLatestKeyPacketForItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): KeyPacket {
        val data = remoteKeyPacketDataSource.getLatestKeyPacketForItem(userId, shareId, itemId)
        return responseToDomain(userId, shareId, data)
    }

    private suspend fun responseToDomain(
        userId: UserId,
        shareId: ShareId,
        data: KeyPacketInfo
    ): KeyPacket {
        val share = shareRepository.getById(userId, shareId)
        requireNotNull(share)
        val userAddress = requireNotNull(userAddressRepository.getAddresses(userId).primary())

        val decodedKeyPacket = cryptoContext.pgpCrypto.getBase64Decoded(data.keyPacket)
        val decodedKeyPacketSignature =
            cryptoContext.pgpCrypto.getBase64Decoded(data.keyPacketSignature)
        val armoredKeyPacketSignature =
            cryptoContext.pgpCrypto.getArmored(decodedKeyPacketSignature, PGPHeader.Signature)

        val itemKey =
            keyRepository.getItemKeyById(userAddress, share.id, share.signingKey, data.rotationId)
        val itemPublicKey = itemKey.publicKey(cryptoContext)
        val validated = cryptoContext.pgpCrypto.verifyData(
            decodedKeyPacket,
            armoredKeyPacketSignature,
            itemPublicKey.key
        )
        require(validated) { "KeyPacketSignature did not match [shareId=${shareId.id}] [rotationId=${data.rotationId}]" }

        return KeyPacket(
            rotationId = data.rotationId,
            keyPacket = decodedKeyPacket,
        )
    }
}
