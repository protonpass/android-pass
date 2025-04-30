package proton.android.pass.account.fakes.crypto

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.repository.PassphraseRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Suppress("NotImplementedDeclaration")
class FakePassphraseRepository @Inject constructor() : PassphraseRepository {
    override suspend fun setPassphrase(userId: UserId, passphrase: EncryptedByteArray) {
        TODO("Not yet implemented")
    }

    override suspend fun getPassphrase(userId: UserId): EncryptedByteArray? {
        TODO("Not yet implemented")
    }

    override suspend fun clearPassphrase(userId: UserId) {
        TODO("Not yet implemented")
    }

    override fun addOnPassphraseChangedListener(listener: PassphraseRepository.OnPassphraseChangedListener) {
        TODO("Not yet implemented")
    }
}
