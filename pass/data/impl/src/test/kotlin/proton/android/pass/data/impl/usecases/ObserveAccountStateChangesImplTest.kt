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

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.core.account.domain.entity.AccountState
import org.junit.Test
import proton.android.pass.account.fakes.FakeAccountManager
import proton.android.pass.domain.UserId
import proton.android.pass.test.domain.AccountTestFactory
import proton.android.pass.domain.account.AccountState as PassAccountState

class ObserveAccountStateChangesImplTest {

    private val accountManager = FakeAccountManager()
    private val useCase = ObserveAccountStateChangesImpl(accountManager)

    @Test
    fun `maps state changes to Pass accounts`() = runTest {
        val userId = UserId("user")

        useCase().test {
            accountManager.emitAccountStateChange(FakeAccountManager.createAccount(userId))

            assertThat(awaitItem()).isEqualTo(AccountTestFactory.create(userId))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `maps a step-needed state change`() = runTest {
        val userId = UserId("user")
        val account = FakeAccountManager.createAccount(userId).copy(state = AccountState.CreateAddressNeeded)

        useCase().test {
            accountManager.emitAccountStateChange(account)

            assertThat(awaitItem()).isEqualTo(
                AccountTestFactory.create(userId, state = PassAccountState.Other, isStepNeeded = true)
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `does not replay past state changes`() = runTest {
        accountManager.emitAccountStateChange(FakeAccountManager.createAccount(UserId("past")))

        useCase().test {
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
