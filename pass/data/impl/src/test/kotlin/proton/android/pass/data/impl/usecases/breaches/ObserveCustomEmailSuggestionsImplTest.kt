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
import me.proton.core.user.domain.entity.AddressId
import org.junit.Before
import org.junit.Test
import proton.android.pass.data.api.usecases.breach.CustomEmailSuggestion
import proton.android.pass.data.fakes.usecases.TestObserveCurrentUser
import proton.android.pass.data.fakes.usecases.TestObserveItems
import proton.android.pass.data.fakes.usecases.breach.FakeObserveBreachProtonEmails
import proton.android.pass.data.impl.usecases.breach.ObserveCustomEmailSuggestionsImpl
import proton.android.pass.domain.breach.BreachProtonEmail
import proton.android.pass.test.domain.TestItem
import proton.android.pass.test.domain.TestUser

internal class ObserveCustomEmailSuggestionsImplTest {

    private lateinit var instance: ObserveCustomEmailSuggestionsImpl

    private lateinit var observeCurrentUser: TestObserveCurrentUser
    private lateinit var observeItems: TestObserveItems
    private lateinit var observeBreachProtonEmails: FakeObserveBreachProtonEmails

    @Before
    fun setup() {
        observeItems = TestObserveItems()
        observeCurrentUser = TestObserveCurrentUser()
        observeBreachProtonEmails = FakeObserveBreachProtonEmails()
        instance = ObserveCustomEmailSuggestionsImpl(
            observeCurrentUser = observeCurrentUser,
            observeItems = observeItems,
            observeBreachProtonEmails = observeBreachProtonEmails
        )
        observeCurrentUser.sendUser(TestUser.create(userId = UserId("test-user-id")))
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
            TestItem.createLogin(email = email1),
            TestItem.createLogin(email = email1),
            TestItem.createLogin(email = email1),
            TestItem.createLogin(email = email2),
            TestItem.createLogin(email = email2),
            TestItem.createLogin(email = email3)
        )
        val aliases = listOf(
            TestItem.createAlias(alias = email3),
            TestItem.createAlias(alias = "this@isnot.alogin")
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
            TestItem.createLogin(email = second),
            TestItem.createLogin(email = first),
            TestItem.createLogin(email = first),
            TestItem.createLogin(email = second)
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
            TestItem.createLogin(email = email),
            TestItem.createLogin(email = email),
            TestItem.createLogin(email = USER_PROTON_ADDRESS)
        )
        observeItems.emitValue(logins)
        observeBreachProtonEmails.emit(
            listOf(
                BreachProtonEmail(
                    addressId = AddressId("testaddress"),
                    email = USER_PROTON_ADDRESS,
                    breachCounter = 0,
                    flags = 0,
                    lastBreachTime = null
                )
            )
        )
        val res = instance().first()

        val expected = listOf(CustomEmailSuggestion(email, 2))
        assertThat(res).isEqualTo(expected)
    }

    private companion object {
        private const val USER_PROTON_ADDRESS = "some@address.test"
    }

}
