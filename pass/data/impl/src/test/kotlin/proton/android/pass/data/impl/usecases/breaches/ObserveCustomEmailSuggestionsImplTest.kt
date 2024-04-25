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

package proton.android.pass.data.impl.usecases.breaches

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Test
import proton.android.pass.account.fakes.TestAccountManager
import proton.android.pass.account.fakes.TestUserAddressRepository
import proton.android.pass.data.api.usecases.breach.CustomEmailSuggestion
import proton.android.pass.data.fakes.usecases.TestObserveItems
import proton.android.pass.data.impl.usecases.breach.ObserveCustomEmailSuggestionsImpl

class ObserveCustomEmailSuggestionsImplTest {

    private lateinit var instance: ObserveCustomEmailSuggestionsImpl

    private lateinit var observeItems: TestObserveItems

    @Before
    fun setup() {
        observeItems = TestObserveItems()
        instance = ObserveCustomEmailSuggestionsImpl(
            accountManager = TestAccountManager().apply {
                sendPrimaryUserId(UserId("test-user-id"))
            },
            observeItems = observeItems,
            addressRepository = TestUserAddressRepository().apply {
                val address = generateAddress(
                    displayName = "testaddress",
                    email = USER_PROTON_ADDRESS
                )
                setAddresses(listOf(address))
            }
        )
    }

    @Test
    fun `can handle empty lists`() = runTest {
        observeItems.emitValue(emptyList())

        val res = instance().first()
        assertThat(res).isEmpty()
    }

    @Test
    fun `can group items together`() = runTest {
        val email1 = "some@email.lol"
        val email2 = "another@email.lol"
        val email3 = "yetanother@email.lol"

        val logins = listOf(
            TestObserveItems.createLogin(username = email1),
            TestObserveItems.createLogin(username = email1),
            TestObserveItems.createLogin(username = email1),
            TestObserveItems.createLogin(username = email2),
            TestObserveItems.createLogin(username = email2),
            TestObserveItems.createLogin(username = email3)
        )
        val aliases = listOf(
            TestObserveItems.createAlias(alias = email3),
            TestObserveItems.createAlias(alias = "this@isnot.alogin")
        )
        val items = logins + aliases
        observeItems.emitValue(items)

        val res = instance().first()

        val expected = listOf(
            CustomEmailSuggestion(email1, 3),
            CustomEmailSuggestion(email2, 2)
        )
        assertThat(res).isEqualTo(expected)
    }

    @Test
    fun `sorting is respected`() = runTest {
        val first = "first@email.test"
        val second = "second@email.test"

        val logins = listOf(
            TestObserveItems.createLogin(username = second),
            TestObserveItems.createLogin(username = first),
            TestObserveItems.createLogin(username = first),
            TestObserveItems.createLogin(username = second)
        )
        observeItems.emitValue(logins)

        val res = instance().first()

        val expected = listOf(
            CustomEmailSuggestion(first, 2),
            CustomEmailSuggestion(second, 2)
        )
        assertThat(res).isEqualTo(expected)
    }

    @Test
    fun `excludes proton addresses`() = runTest {
        val email = "first@email.test"

        val logins = listOf(
            TestObserveItems.createLogin(username = email),
            TestObserveItems.createLogin(username = email),
            TestObserveItems.createLogin(username = USER_PROTON_ADDRESS)
        )
        observeItems.emitValue(logins)

        val res = instance().first()

        val expected = listOf(CustomEmailSuggestion(email, 2))
        assertThat(res).isEqualTo(expected)
    }

    companion object {
        private const val USER_PROTON_ADDRESS = "some@address.test"
    }
}
