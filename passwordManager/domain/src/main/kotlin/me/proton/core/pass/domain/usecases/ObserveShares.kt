package me.proton.core.pass.domain.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.domain.entity.UserId
import me.proton.core.pass.domain.Share
import me.proton.core.pass.domain.entity.NewVault
import me.proton.core.pass.domain.repositories.ShareRepository
import javax.inject.Inject

class ObserveShares @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val sharesRepository: ShareRepository
) {
    operator fun invoke(userId: UserId): Flow<List<Share>> =
        sharesRepository.observeShares(userId).mapLatest { shares ->
            if (shares.isEmpty()) {
                sharesRepository.createVault(userId, NewVault(
                    name = "Personal".encrypt(cryptoContext.keyStoreCrypto),
                    description = "Personal vault".encrypt(cryptoContext.keyStoreCrypto)
                ))
            }
            shares
        }
}
