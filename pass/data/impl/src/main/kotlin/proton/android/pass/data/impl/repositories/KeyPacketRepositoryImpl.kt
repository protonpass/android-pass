package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import proton.android.pass.data.api.repositories.KeyPacketRepository
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.repositories.VaultKeyRepository
import proton.android.pass.data.impl.remote.RemoteKeyPacketDataSource
import proton.android.pass.data.impl.responses.KeyPacketInfo
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.pgp.PGPHeader
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.extension.primary
import me.proton.core.user.domain.repository.UserAddressRepository
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.map
import proton.pass.domain.ItemId
import proton.pass.domain.KeyPacket
import proton.pass.domain.Share
import proton.pass.domain.ShareId
import proton.pass.domain.key.publicKey
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
    ): LoadingResult<KeyPacket> = withContext(Dispatchers.IO) {
        when (
            val result =
                remoteKeyPacketDataSource.getLatestKeyPacketForItem(userId, shareId, itemId)
        ) {
            is LoadingResult.Error -> LoadingResult.Error(result.exception)
            LoadingResult.Loading -> LoadingResult.Loading
            is LoadingResult.Success -> responseToDomain(userId, shareId, result.data)
        }
    }

    private suspend fun responseToDomain(
        userId: UserId,
        shareId: ShareId,
        data: KeyPacketInfo
    ): LoadingResult<KeyPacket> {
        val shareResult = shareRepository.getById(userId, shareId)
        when (shareResult) {
            is LoadingResult.Error -> return LoadingResult.Error(shareResult.exception)
            LoadingResult.Loading -> return LoadingResult.Loading
            is LoadingResult.Success -> Unit
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
