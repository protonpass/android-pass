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

package proton.android.pass.ui.launcher

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import proton.android.pass.domain.UserId
import proton.android.pass.domain.account.Account
import proton.android.pass.domain.account.AccountState as PassAccountState
import proton.android.pass.test.domain.AccountTestFactory

class LauncherAccountStateTest {

    @Test
    fun `no accounts needs an account`() {
        assertThat(emptyList<Account>().toLauncherAccountState())
            .isEqualTo(AccountState.AccountNeeded)
    }

    @Test
    fun `all disabled accounts need an account`() {
        val accounts = listOf(account("a", PassAccountState.Disabled), account("b", PassAccountState.Disabled))

        assertThat(accounts.toLauncherAccountState()).isEqualTo(AccountState.AccountNeeded)
    }

    @Test
    fun `any ready account means a primary exists`() {
        val accounts = listOf(account("a", PassAccountState.Disabled), account("b", PassAccountState.Ready))

        assertThat(accounts.toLauncherAccountState()).isEqualTo(AccountState.PrimaryExist)
    }

    @Test
    fun `ready wins over step needed`() {
        val accounts = listOf(
            account("a", PassAccountState.Other, isStepNeeded = true),
            account("b", PassAccountState.Ready)
        )

        assertThat(accounts.toLauncherAccountState()).isEqualTo(AccountState.PrimaryExist)
    }

    @Test
    fun `step needed without a ready account`() {
        val accounts = listOf(account("a", PassAccountState.Other, isStepNeeded = true))

        assertThat(accounts.toLauncherAccountState()).isEqualTo(AccountState.StepNeeded)
    }

    @Test
    fun `other accounts without a step are processing`() {
        val accounts = listOf(account("a", PassAccountState.Other), account("b", PassAccountState.Disabled))

        assertThat(accounts.toLauncherAccountState()).isEqualTo(AccountState.Processing)
    }

    private fun account(
        id: String,
        state: PassAccountState,
        isStepNeeded: Boolean = false
    ) = AccountTestFactory.create(UserId(id), state = state, isStepNeeded = isStepNeeded)
}
