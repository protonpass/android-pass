/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.account.fakes

import kotlinx.coroutines.flow.Flow
import me.proton.core.auth.fido.domain.entity.SecondFactorProof
import me.proton.core.challenge.domain.entity.ChallengeFrameDetails
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.srp.Auth
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.CreateUserType
import me.proton.core.user.domain.entity.Domain
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.repository.PassphraseRepository
import me.proton.core.user.domain.repository.UserRepository
import javax.inject.Inject

@Suppress("TooManyFunctions")
class FakeUserRepository @Inject constructor() : UserRepository {
    override fun addOnPassphraseChangedListener(listener: PassphraseRepository.OnPassphraseChangedListener) {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun addUser(user: User) {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun checkUsernameAvailable(sessionUserId: UserId?, username: String) {
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
        frames: List<ChallengeFrameDetails>,
        sessionUserId: SessionUserId?
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
        frames: List<ChallengeFrameDetails>,
        sessionUserId: SessionUserId?
    ): User {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun getPassphrase(userId: UserId): EncryptedByteArray? {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun getUser(sessionUserId: SessionUserId, refresh: Boolean): User {
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
        secondFactorProof: SecondFactorProof?
    ): Boolean {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun updateUser(user: User) {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun updateUserUsedSpace(userId: UserId, usedSpace: Long) {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun updateUserUsedBaseSpace(userId: UserId, usedBaseSpace: Long) {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun updateUserUsedDriveSpace(userId: UserId, usedDriveSpace: Long) {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun checkExternalEmailAvailable(sessionUserId: SessionUserId?, email: String) {
        throw IllegalStateException("Not implemented")
    }
}
