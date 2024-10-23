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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.proton.core.auth.fido.domain.entity.SecondFactorProof
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.crypto.common.pgp.Based64Encoded
import me.proton.core.crypto.common.srp.Auth
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.Type
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.entity.UserKey
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeUserManager @Inject constructor() : UserManager {

    private val state: MutableStateFlow<User> = MutableStateFlow(DEFAULT_USER)

    override suspend fun addUser(user: User, addresses: List<UserAddress>) {
        state.emit(user)
    }

    override suspend fun getAddresses(sessionUserId: SessionUserId, refresh: Boolean): List<UserAddress> {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun getUser(sessionUserId: SessionUserId, refresh: Boolean): User = state.value

    override suspend fun lock(userId: UserId) {
        throw IllegalStateException("Not implemented")
    }

    override suspend fun changePassword(
        userId: UserId,
        newPassword: EncryptedString,
        secondFactorProof: SecondFactorProof?,
        proofs: SrpProofs?,
        srpSession: String?,
        auth: Auth?,
        encryptedSecret: Based64Encoded?
    ): Boolean {
        throw IllegalStateException("Not implemented")
    }

    override fun observeAddresses(sessionUserId: SessionUserId, refresh: Boolean): Flow<List<UserAddress>> {
        throw IllegalStateException("Not implemented")
    }

    override fun observeUser(sessionUserId: SessionUserId, refresh: Boolean): Flow<User?> = state

    fun setUser(user: User) {
        state.update { user }
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
        password: ByteArray,
        organizationPublicKey: Armored?,
        deviceSecret: EncryptedString?
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

    companion object {
        const val USER_ID = "FakeUserManager-DefaultUserId"
        const val EMAIL = "DefaultEmail"
        val DEFAULT_USER = User(
            userId = UserId(USER_ID),
            email = EMAIL,
            name = "name",
            displayName = null,
            currency = "",
            type = Type.Proton,
            credit = 0,
            createdAtUtc = 0,
            usedSpace = 0,
            maxSpace = 0,
            maxUpload = 0,
            role = null,
            private = false,
            services = 0,
            subscribed = 0,
            delinquent = null,
            keys = listOf(),
            flags = emptyMap(),
            recovery = null
        )
    }
}
