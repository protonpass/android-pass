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

package proton.android.pass.initializer

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Test
import proton.android.pass.domain.account.AccountState
import proton.android.pass.test.domain.AccountTestFactory

class AccountReadyTransitionsTest {

    @Test
    fun `emits when an account becomes ready for the first time`() = runTest {
        val result = flowOf(account(FIRST, AccountState.Ready)).justBecameReady().toList()

        assertThat(result).hasSize(1)
    }

    @Test
    fun `does not emit for a repeated ready state`() = runTest {
        val result = flowOf(
            account(FIRST, AccountState.Ready),
            account(FIRST, AccountState.Ready)
        ).justBecameReady().toList()

        assertThat(result).hasSize(1)
    }

    @Test
    fun `emits again when an account becomes ready after being disabled`() = runTest {
        val result = flowOf(
            account(FIRST, AccountState.Ready),
            account(FIRST, AccountState.Disabled),
            account(FIRST, AccountState.Ready)
        ).justBecameReady().toList()

        assertThat(result).hasSize(2)
    }

    @Test
    fun `does not emit for states other than ready`() = runTest {
        val result = flowOf(
            account(FIRST, AccountState.Disabled),
            account(FIRST, AccountState.Removed),
            account(FIRST, AccountState.Other)
        ).justBecameReady().toList()

        assertThat(result).isEmpty()
    }

    @Test
    fun `tracks each user independently`() = runTest {
        val result = flowOf(
            account(FIRST, AccountState.Ready),
            account(SECOND, AccountState.Ready),
            account(FIRST, AccountState.Ready),
            account(SECOND, AccountState.Ready)
        ).justBecameReady().toList()

        assertThat(result).hasSize(2)
    }

    private fun account(userId: UserId, state: AccountState) = AccountTestFactory.create(
        userId = userId,
        state = state
    )

    private companion object {
        val FIRST = UserId("first-user")
        val SECOND = UserId("second-user")
    }
}
