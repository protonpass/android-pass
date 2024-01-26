/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.autofill.e2e

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import me.proton.core.domain.entity.UserId
import proton.android.pass.account.fakes.TestAccountManager
import proton.android.pass.data.api.usecases.SuggestedCreditCardItemsResult
import proton.android.pass.data.fakes.usecases.TestGetSuggestedCreditCardItems
import proton.android.pass.data.fakes.usecases.TestGetSuggestedLoginItems
import proton.android.pass.data.fakes.usecases.TestObserveItems
import proton.android.pass.domain.ItemId
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class E2EApp : Application() {

    @Inject
    lateinit var accountManager: TestAccountManager

    @Inject
    lateinit var loginItems: TestGetSuggestedLoginItems

    @Inject
    lateinit var creditCardItems: TestGetSuggestedCreditCardItems

    override fun onCreate() {
        super.onCreate()
        setupItems()
        setupAccount()
        setupLogger()
    }

    private fun setupItems() {
        val logins = listOf(
            TestObserveItems.createLogin(
                itemId = ItemId("item1"),
                title = "Item1",
                username = "user1",
                password = "pass1"
            ),
            TestObserveItems.createLogin(
                itemId = ItemId("item2"),
                title = "Item2",
                username = "user2",
                password = "pass2"
            )
        )
        loginItems.sendValue(Result.success(logins))

        val creditCards = listOf(
            TestObserveItems.createCreditCard(
                itemId = ItemId("ccItem1"),
                holder = "FirstName LastName",
                number = "4766000011112222",
                verificationNumber = "123",
                expirationDate = "2025-12",
                title = "First CreditCard"
            ),
            TestObserveItems.createCreditCard(
                itemId = ItemId("ccItem2"),
                holder = "SecondName SecondLast",
                number = "1234123412341234",
                verificationNumber = "987",
                expirationDate = "2028-03",
                title = "Second card"
            )
        )
        creditCardItems.sendValue(Result.success(SuggestedCreditCardItemsResult.Items(creditCards)))
    }

    private fun setupLogger() {
        Timber.plant(Timber.DebugTree())
    }

    private fun setupAccount() {
        accountManager.sendPrimaryUserId(UserId("user1"))
    }
}
