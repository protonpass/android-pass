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
import me.proton.core.util.android.sentry.TimberLogger
import me.proton.core.util.kotlin.CoreLogger
import proton.android.pass.account.fakes.TestAccountManager
import proton.android.pass.data.api.usecases.ItemData
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.SuggestedAutofillItemsResult
import proton.android.pass.data.api.usecases.Suggestion
import proton.android.pass.data.fakes.usecases.TestGetSuggestedAutofillItems
import proton.android.pass.data.fakes.usecases.TestObserveItems
import proton.android.pass.domain.ItemId
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class E2EApp : Application() {

    @Inject
    lateinit var accountManager: TestAccountManager

    @Inject
    lateinit var autofillItems: TestGetSuggestedAutofillItems

    @Inject
    lateinit var featureFlagsPreferencesRepository: FeatureFlagsPreferencesRepository

    override fun onCreate() {
        super.onCreate()
        setupItems()
        setupAccount()
        setupLogger()
        featureFlagsPreferencesRepository.set(FeatureFlag.IDENTITY_V1, true)
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
        ).map { ItemData.SuggestedItem(it, Suggestion.PackageName("")) }
        autofillItems.sendValue(
            itemTypeFilter = ItemTypeFilter.Logins,
            value = Result.success(SuggestedAutofillItemsResult.Items(logins))
        )

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
        ).map { ItemData.SuggestedItem(it, Suggestion.PackageName("")) }
        autofillItems.sendValue(
            itemTypeFilter = ItemTypeFilter.CreditCards,
            value = Result.success(SuggestedAutofillItemsResult.Items(creditCards))
        )

        val identities = listOf(
            TestObserveItems.createIdentity(
                itemId = ItemId("identity1"),
                fullName = "Peter Parker"
            ),
            TestObserveItems.createIdentity(
                itemId = ItemId("identity2"),
                fullName = "Tony Stark"
            )
        ).map { ItemData.SuggestedItem(it, Suggestion.PackageName("")) }

        autofillItems.sendValue(
            itemTypeFilter = ItemTypeFilter.Identity,
            value = Result.success(SuggestedAutofillItemsResult.Items(identities))
        )
    }

    private fun setupLogger() {
        Timber.plant(Timber.DebugTree())
        CoreLogger.set(TimberLogger)
    }

    private fun setupAccount() {
        accountManager.sendPrimaryUserId(UserId("user1"))
    }
}
