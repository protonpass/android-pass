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

package proton.android.pass.data.impl.account

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import proton.android.pass.test.domain.UserTestFactory

class AccountMappersTest {

    @Test
    fun `user without subscription or flags is free`() {
        assertThat(UserTestFactory.createCore().toDomain().isFree).isTrue()
    }

    @Test
    fun `user with subscription is not free`() {
        assertThat(UserTestFactory.createCore(subscribed = 1).toDomain().isFree).isFalse()
    }

    @Test
    fun `user with pass lifetime flag is not free`() {
        val user = UserTestFactory.createCore(flags = mapOf("pass-lifetime" to true))

        assertThat(user.toDomain().isFree).isFalse()
    }

    @Test
    fun `user with pass from simplelogin flag is not free`() {
        val user = UserTestFactory.createCore(flags = mapOf("pass-from-sl" to true))

        assertThat(user.toDomain().isFree).isFalse()
    }

    @Test
    fun `user with flags set to false is free`() {
        val user = UserTestFactory.createCore(flags = mapOf("pass-lifetime" to false, "pass-from-sl" to false))

        assertThat(user.toDomain().isFree).isTrue()
    }
}
