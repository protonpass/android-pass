package proton.android.pass.test

import kotlinx.coroutines.flow.Flow
import me.proton.core.challenge.domain.entity.ChallengeFrameDetails
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.srp.Auth
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.CreateUserType
import me.proton.core.user.domain.entity.Domain
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.repository.PassphraseRepository
import me.proton.core.user.domain.repository.UserRepository

class TestUserRepository : UserRepository {
    override fun addOnPassphraseChangedListener(listener: PassphraseRepository.OnPassphraseChangedListener) {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun addUser(user: User) {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun checkExternalEmailAvailable(email: String) {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun checkUsernameAvailable(username: String) {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun clearPassphrase(userId: UserId) {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun createExternalEmailUser(
        email: String,
        password: EncryptedString,
        referrer: String?,
        type: CreateUserType,
        auth: Auth,
        frames: List<ChallengeFrameDetails>
    ): User {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun createUser(
        username: String,
        domain: Domain?,
        password: EncryptedString,
        recoveryEmail: String?,
        recoveryPhone: String?,
        referrer: String?,
        type: CreateUserType,
        auth: Auth,
        frames: List<ChallengeFrameDetails>
    ): User {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun getPassphrase(userId: UserId): EncryptedByteArray? {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun getUser(sessionUserId: SessionUserId, refresh: Boolean): User {
        throw IllegalStateException("Not implemented")
    }

    override fun getUserFlow(
        sessionUserId: SessionUserId,
        refresh: Boolean
    ): Flow<DataResult<User>> {
        throw IllegalStateException("Not implemented")
    }

    override fun observeUser(sessionUserId: SessionUserId, refresh: Boolean): Flow<User?> {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun removeLockedAndPasswordScopes(sessionUserId: SessionUserId): Boolean {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun setPassphrase(userId: UserId, passphrase: EncryptedByteArray) {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun unlockUserForLockedScope(
        sessionUserId: SessionUserId,
        srpProofs: SrpProofs,
        srpSession: String
    ): Boolean {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun unlockUserForPasswordScope(
        sessionUserId: SessionUserId,
        srpProofs: SrpProofs,
        srpSession: String,
        twoFactorCode: String?
    ): Boolean {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun updateUser(user: User) {
        throw IllegalStateException("Not implemented")
    }
}
