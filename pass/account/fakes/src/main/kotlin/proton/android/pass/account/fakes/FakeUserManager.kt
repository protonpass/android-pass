/*
 * Copyright (c) 2024 Proton AG
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
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.crypto.common.srp.Auth
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.entity.UserKey
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeUserManager @Inject constructor() : UserManager {
    override suspend fun addUser(user: User, addresses: List<UserAddress>) {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun changePassword(
        userId: UserId,
        newPassword: EncryptedString,
        secondFactorCode: String,
        proofs: SrpProofs,
        srpSession: String,
        auth: Auth?
    ): Boolean {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun getAddresses(
        sessionUserId: SessionUserId,
        refresh: Boolean
    ): List<UserAddress> {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun getUser(sessionUserId: SessionUserId, refresh: Boolean): User {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun lock(userId: UserId) {
        throw IllegalStateException("Not implemented")
    }

    override fun observeAddresses(
        sessionUserId: SessionUserId,
        refresh: Boolean
    ): Flow<List<UserAddress>> {
        throw IllegalStateException("Not implemented")
    }

    override fun observeUser(sessionUserId: SessionUserId, refresh: Boolean): Flow<User?> {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun reactivateKey(userKey: UserKey): User {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun resetPassword(
        sessionUserId: SessionUserId,
        newPassword: EncryptedString,
        auth: Auth?
    ): Boolean {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun setupPrimaryKeys(
        sessionUserId: SessionUserId,
        username: String,
        domain: String,
        auth: Auth,
        password: ByteArray
    ): User {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun unlockWithPassphrase(
        userId: UserId,
        passphrase: EncryptedByteArray
    ): UserManager.UnlockResult {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun unlockWithPassword(
        userId: UserId,
        password: PlainByteArray,
        refreshKeySalts: Boolean
    ): UserManager.UnlockResult {
        throw IllegalStateException("Not implemented")
    }
}
