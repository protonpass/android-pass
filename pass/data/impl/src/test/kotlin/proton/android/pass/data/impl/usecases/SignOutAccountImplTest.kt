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
package proton.android.pass.data.impl.usecases

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Test
import proton.android.pass.account.fakes.FakeAccountManager
import proton.android.pass.data.fakes.usecases.FakeDisableAccount
import proton.android.pass.data.fakes.usecases.FakeObserveReadyAccounts

class SignOutAccountImplTest {

    private val signedOut = UserId("signed-out")
    private val other = UserId("other")

    private val disableAccount = FakeDisableAccount()
    private val observeReadyAccounts = FakeObserveReadyAccounts()
    private val instance = SignOutAccountImpl(disableAccount, observeReadyAccounts)

    @Test
    fun `disables the account`() = runTest {
        instance(signedOut)

        assertThat(disableAccount.disabledUserIds).containsExactly(signedOut)
    }

    @Test
    fun `returns true when another ready account remains`() = runTest {
        observeReadyAccounts.emitAccounts(
            listOf(FakeAccountManager.createAccount(signedOut), FakeAccountManager.createAccount(other))
        )

        assertThat(instance(signedOut)).isTrue()
    }

    @Test
    fun `returns false when the signed out account was the only ready one`() = runTest {
        observeReadyAccounts.emitAccounts(listOf(FakeAccountManager.createAccount(signedOut)))

        assertThat(instance(signedOut)).isFalse()
    }
}
