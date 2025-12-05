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
import kotlinx.datetime.Clock
import me.proton.core.domain.entity.UserId
import me.proton.core.util.android.sentry.TimberLogger
import me.proton.core.util.kotlin.CoreLogger
import proton.android.pass.account.fakes.FakeUserManager
import proton.android.pass.account.fakes.FakeAccountManager
import proton.android.pass.data.api.usecases.ItemData
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.SuggestedAutofillItemsResult
import proton.android.pass.data.api.usecases.Suggestion
import proton.android.pass.data.fakes.usecases.FakeGetItemById
import proton.android.pass.data.fakes.usecases.FakeGetSuggestedAutofillItems
import proton.android.pass.data.fakes.usecases.FakeGetUserPlan
import proton.android.pass.data.fakes.usecases.FakeObserveItems
import proton.android.pass.data.fakes.usecases.shares.FakeObserveAutofillShares
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.Plan
import proton.android.pass.domain.PlanLimit
import proton.android.pass.domain.PlanType
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareSelection
import proton.android.pass.test.domain.TestItem
import proton.android.pass.test.domain.TestShare
import proton.android.pass.test.domain.TestUser
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class E2EApp : Application() {

    @Inject
    lateinit var accountManager: FakeAccountManager

    @Inject
    lateinit var userManager: FakeUserManager

    @Inject
    lateinit var autofillItems: FakeGetSuggestedAutofillItems

    @Inject
    lateinit var observeItems: FakeObserveItems

    @Inject
    lateinit var observeAutofillShares: FakeObserveAutofillShares

    @Inject
    lateinit var getItemById: FakeGetItemById

    @Inject
    lateinit var getUserPlan: FakeGetUserPlan

    override fun onCreate() {
        super.onCreate()
        setupAccount()
        setupVault()
        setupItems()
        setupLogger()
    }

    private fun setupVault() {
        val vaultShare = TestShare.Vault.create(
            id = VAULT_SHARE_ID.id,
            userId = PRIMARY_USER_ID.id
        )

        observeAutofillShares.setValue(listOf(vaultShare), userId = PRIMARY_USER_ID)
    }

    private fun setupItems() {
        setupCreditCards()
        setupIdentities()
        setupLogins()
    }

    private fun setupLogins() {
        val logins = listOf(
            TestItem.createLogin(
                shareId = VAULT_SHARE_ID,
                itemId = ItemId("item1"),
                title = "Item1",
                username = "user1",
                password = "pass1"
            ),
            TestItem.createLogin(
                shareId = VAULT_SHARE_ID,
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
        emitItems(logins, ItemTypeFilter.Logins)
    }

    private fun setupCreditCards() {
        val creditCards = listOf(
            TestItem.createCreditCard(
                shareId = VAULT_SHARE_ID,
                itemId = ItemId("ccItem1"),
                holder = "FirstName LastName",
                number = "4766000011112222",
                verificationNumber = "123",
                expirationDate = "2025-12",
                title = "First CreditCard"
            ),
            TestItem.createCreditCard(
                shareId = VAULT_SHARE_ID,
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
        emitItems(creditCards, ItemTypeFilter.CreditCards)
    }

    private fun setupIdentities() {
        val identities = listOf(
            TestItem.createIdentity(
                shareId = VAULT_SHARE_ID,
                itemId = ItemId("identity1"),
                fullName = "Peter Parker"
            ),
            TestItem.createIdentity(
                shareId = VAULT_SHARE_ID,
                itemId = ItemId("identity2"),
                fullName = "Tony Stark"
            )
        ).map { ItemData.SuggestedItem(it, Suggestion.PackageName("")) }

        autofillItems.sendValue(
            itemTypeFilter = ItemTypeFilter.Identity,
            value = Result.success(SuggestedAutofillItemsResult.Items(identities))
        )
        emitItems(identities, ItemTypeFilter.Identity)
    }

    private fun emitItems(items: List<ItemData.SuggestedItem>, filter: ItemTypeFilter) {
        observeItems.emit(
            params = FakeObserveItems.Params(
                userId = PRIMARY_USER_ID,
                filter = filter,
                selection = ShareSelection.Shares(listOf(VAULT_SHARE_ID)),
                itemState = ItemState.Active
            ),
            value = items.map { it.item }
        )
        items.forEach { item ->
            getItemById.emit(
                shareId = item.item.shareId,
                itemId = item.item.id,
                value = Result.success(item.item)
            )
        }
    }

    private fun setupLogger() {
        Timber.plant(Timber.DebugTree())
        CoreLogger.set(TimberLogger)
    }

    private fun setupAccount() {
        accountManager.sendPrimaryUserId(PRIMARY_USER_ID)
        accountManager.setAccounts(listOf(FakeAccountManager.createAccount(PRIMARY_USER_ID)))
        userManager.setUser(TestUser.create(userId = PRIMARY_USER_ID))

        val plan = Plan(
            planType = PlanType.Paid.Plus(name = "plus", displayName = "plus"),
            hideUpgrade = true,
            vaultLimit = PlanLimit.Unlimited,
            aliasLimit = PlanLimit.Unlimited,
            totpLimit = PlanLimit.Unlimited,
            updatedAt = Clock.System.now().epochSeconds
        )
        getUserPlan.setResult(value = Result.success(plan), userId = PRIMARY_USER_ID)

    }

    private companion object {
        private val PRIMARY_USER_ID = UserId("E2EApp-UserID")
        private val VAULT_SHARE_ID = ShareId("E2EApp-ShareID")
    }
}
