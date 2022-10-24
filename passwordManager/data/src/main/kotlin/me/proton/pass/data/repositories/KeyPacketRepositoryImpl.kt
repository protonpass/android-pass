package me.proton.pass.data.repositories

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.pgp.PGPHeader
import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.common.api.map
import me.proton.pass.data.remote.RemoteKeyPacketDataSource
import me.proton.pass.data.responses.KeyPacketInfo
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.KeyPacket
import me.proton.pass.domain.Share
import me.proton.pass.domain.ShareId
import me.proton.pass.domain.key.publicKey
import me.proton.pass.domain.repositories.KeyPacketRepository
import me.proton.pass.domain.repositories.ShareRepository
import me.proton.pass.domain.repositories.VaultKeyRepository
import me.proton.core.user.domain.extension.primary
import me.proton.core.user.domain.repository.UserAddressRepository
import javax.inject.Inject

class KeyPacketRepositoryImpl @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val userAddressRepository: UserAddressRepository,
    private val shareRepository: ShareRepository,
    private val keyRepository: VaultKeyRepository,
    private val remoteKeyPacketDataSource: RemoteKeyPacketDataSource
) : KeyPacketRepository {

    override suspend fun getLatestKeyPacketForItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Result<KeyPacket> =
        when (
            val result =
                remoteKeyPacketDataSource.getLatestKeyPacketForItem(userId, shareId, itemId)
        ) {
            is Result.Error -> Result.Error(result.exception)
            Result.Loading -> Result.Loading
            is Result.Success -> responseToDomain(userId, shareId, result.data)
        }

    private suspend fun responseToDomain(
        userId: UserId,
        shareId: ShareId,
        data: KeyPacketInfo
    ): Result<KeyPacket> {
        val shareResult = shareRepository.getById(userId, shareId)
        when (shareResult) {
            is Result.Error -> return Result.Error(shareResult.exception)
            Result.Loading -> return Result.Loading
            is Result.Success -> Unit
        }
        val share: Share? = shareResult.data
        requireNotNull(share)
        val userAddress = requireNotNull(userAddressRepository.getAddresses(userId).primary())

        val decodedKeyPacket = cryptoContext.pgpCrypto.getBase64Decoded(data.keyPacket)
        val decodedKeyPacketSignature =
            cryptoContext.pgpCrypto.getBase64Decoded(data.keyPacketSignature)
        val armoredKeyPacketSignature =
            cryptoContext.pgpCrypto.getArmored(decodedKeyPacketSignature, PGPHeader.Signature)

        return keyRepository
            .getItemKeyById(
                userAddress,
                share.id,
                share.signingKey,
                data.rotationId
            )
            .map { itemKey ->
                val itemPublicKey = itemKey.publicKey(cryptoContext)
                val validated = cryptoContext.pgpCrypto.verifyData(
                    decodedKeyPacket,
                    armoredKeyPacketSignature,
                    itemPublicKey.key
                )
                require(validated) {
                    "KeyPacketSignature did not match [shareId=${shareId.id}] [rotationId=${data.rotationId}]"
                }

                KeyPacket(
                    rotationId = data.rotationId,
                    keyPacket = decodedKeyPacket
                )
            }
    }
}
