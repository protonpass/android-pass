/*
 * Copyright (c) 2026 Proton AG
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


package proton.android.pass.crypto.impl

import com.google.common.truth.Truth.assertThat
import me.proton.core.key.domain.entity.key.KeyId
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.entity.UserKey
import org.junit.Test
import proton.android.pass.crypto.fakes.context.FakeCryptoContext
import proton.android.pass.domain.UserId
import proton.android.pass.test.domain.UserTestFactory
import kotlin.test.assertFailsWith

class CoreUserKeysTest {

    @Test
    fun `hasActiveKey is true for an active key with the same id`() {
        val instance = instance(userKey(keyId = ACTIVE_KEY_ID, active = true))

        assertThat(instance.hasActiveKey(ACTIVE_KEY_ID)).isTrue()
    }

    @Test
    fun `hasActiveKey is false for an inactive key`() {
        val instance = instance(userKey(keyId = INACTIVE_KEY_ID, active = false))

        assertThat(instance.hasActiveKey(INACTIVE_KEY_ID)).isFalse()
    }

    @Test
    fun `hasActiveKey is false for an unknown key id`() {
        val instance = instance(userKey(keyId = ACTIVE_KEY_ID, active = true))

        assertThat(instance.hasActiveKey("other-key-id")).isFalse()
    }

    @Test
    fun `hasActiveKey is false when the activity of the key is unknown`() {
        val instance = instance(userKey(keyId = ACTIVE_KEY_ID, active = null))

        assertThat(instance.hasActiveKey(ACTIVE_KEY_ID)).isFalse()
    }

    @Test
    fun `hasActiveKey is false when the user has no keys`() {
        assertThat(instance().hasActiveKey(ACTIVE_KEY_ID)).isFalse()
    }

    @Test
    fun `decrypt rethrows the failure of the crypto context`() {
        val instance = instance(userKey(keyId = ACTIVE_KEY_ID, active = true))

        assertFailsWith<IllegalStateException> { instance.decrypt("token", "label") }
    }

    @Test
    fun `encryptAndSign rethrows the failure of the crypto context`() {
        val instance = instance(userKey(keyId = ACTIVE_KEY_ID, active = true))

        assertFailsWith<IllegalStateException> { instance.encryptAndSign(byteArrayOf(1), "label") }
    }

    @Test
    fun `primaryKeyId returns the id of the primary key`() {
        val instance = instance(
            userKey(keyId = SECONDARY_KEY_ID, active = true, isPrimary = false),
            userKey(keyId = ACTIVE_KEY_ID, active = true, isPrimary = true)
        )

        assertThat(instance.primaryKeyId()).isEqualTo(ACTIVE_KEY_ID)
    }

    @Test
    fun `primaryKeyId is null when no key is primary`() {
        val instance = instance(userKey(keyId = ACTIVE_KEY_ID, active = true, isPrimary = false))

        assertThat(instance.primaryKeyId()).isNull()
    }

    @Test
    fun `primaryKeyId is null when the user has no keys`() {
        assertThat(instance().primaryKeyId()).isNull()
    }

    private fun instance(vararg keys: UserKey) = CoreUserKeys(
        user = user(keys.toList()),
        cryptoContext = FakeCryptoContext()
    )

    private fun user(keys: List<UserKey>): User = UserTestFactory.createCore(userId = USER_ID).copy(keys = keys)

    private fun userKey(
        keyId: String,
        active: Boolean?,
        isPrimary: Boolean = true
    ) = UserKey(
        userId = USER_ID,
        version = 1,
        activation = null,
        active = active,
        keyId = KeyId(keyId),
        privateKey = PrivateKey(
            key = "armored-key",
            isPrimary = isPrimary,
            isActive = active == true,
            canEncrypt = true,
            canVerify = true,
            passphrase = null
        )
    )

    private companion object {
        val USER_ID = UserId("user-id")
        const val ACTIVE_KEY_ID = "active-key-id"
        const val SECONDARY_KEY_ID = "secondary-key-id"
        const val INACTIVE_KEY_ID = "inactive-key-id"
    }
}
