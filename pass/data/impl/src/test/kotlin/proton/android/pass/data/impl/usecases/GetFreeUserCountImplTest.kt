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
import org.junit.Before
import org.junit.Test
import proton.android.pass.account.fakes.FakeUserManager
import proton.android.pass.data.fakes.usecases.FakeObserveReadyAccounts
import proton.android.pass.domain.UserId
import proton.android.pass.domain.account.Account
import proton.android.pass.domain.account.AccountState
import proton.android.pass.test.domain.UserTestFactory

class GetFreeUserCountImplTest {

    private lateinit var observeReadyAccounts: FakeObserveReadyAccounts
    private lateinit var userManager: FakeUserManager
    private lateinit var instance: GetFreeUserCountImpl
    private val accounts = mutableListOf<Account>()

    @Before
    fun setup() {
        observeReadyAccounts = FakeObserveReadyAccounts()
        userManager = FakeUserManager()
        instance = GetFreeUserCountImpl(observeReadyAccounts, userManager)
    }

    @Test
    fun `returns zero when there are no ready accounts`() = runTest {
        observeReadyAccounts.emitAccounts(emptyList())

        assertThat(instance()).isEqualTo(0)
    }

    @Test
    fun `counts only the free users`() = runTest {
        addAccount("free", subscribed = 0)
        addAccount("paid", subscribed = 1)
        addAccount("lifetime", flags = mapOf("pass-lifetime" to true))
        addAccount("fromSl", flags = mapOf("pass-from-sl" to true))
        addAccount("free2", subscribed = 0)

        assertThat(instance()).isEqualTo(2)
    }

    @Test
    fun `counts every user when all are free`() = runTest {
        addAccount("a")
        addAccount("b")
        addAccount("c")

        assertThat(instance()).isEqualTo(3)
    }

    private fun addAccount(
        id: String,
        subscribed: Int = 0,
        flags: Map<String, Boolean> = emptyMap()
    ) {
        val userId = UserId(id)
        userManager.setUser(userId, UserTestFactory.createCore(userId = userId, subscribed = subscribed, flags = flags))
        accounts.add(Account(userId = userId, username = id, state = AccountState.Ready, isStepNeeded = false))
        observeReadyAccounts.emitAccounts(accounts.toList())
    }
}
