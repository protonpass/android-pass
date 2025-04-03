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

package proton.android.pass.data.impl.usecases

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import proton.android.pass.account.fakes.TestAccountManager
import proton.android.pass.account.fakes.TestKeyStoreCrypto
import proton.android.pass.common.api.Option
import proton.android.pass.data.api.usecases.GetSuggestedAutofillItems
import proton.android.pass.data.api.usecases.ItemData
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.SuggestedAutofillItemsResult
import proton.android.pass.data.api.usecases.Suggestion
import proton.android.pass.data.fakes.repositories.FakeAssetLinkRepository
import proton.android.pass.data.fakes.usecases.TestGetUserPlan
import proton.android.pass.data.fakes.usecases.TestObserveItems
import proton.android.pass.data.fakes.usecases.shares.FakeObserveAutofillShares
import proton.android.pass.data.impl.autofill.SuggestionItemFilterer
import proton.android.pass.data.impl.autofill.SuggestionSorter
import proton.android.pass.domain.Item
import proton.android.pass.domain.Plan
import proton.android.pass.domain.PlanLimit
import proton.android.pass.domain.PlanType
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole
import proton.android.pass.domain.ShareSelection
import proton.android.pass.domain.VaultId
import proton.android.pass.preferences.LastItemAutofillPreference
import proton.android.pass.preferences.TestInternalSettingsRepository
import proton.android.pass.preferences.TestPreferenceRepository
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.TestItem
import proton.android.pass.test.domain.TestShare

private typealias Filter = (Item) -> Boolean

internal class FakeSuggestionItemFilterer : SuggestionItemFilterer {
    private var filter: Filter = {
        throw IllegalStateException("Filter has not been initialized")
    }

    fun setFilter(fn: Filter) {
        this.filter = fn
    }

    override fun filter(items: List<Item>, suggestion: Suggestion): List<Item> = items.filter(filter::invoke)
}

class FakeSuggestionSorter : SuggestionSorter {
    override fun sort(
        items: List<ItemData.SuggestedItem>,
        lastItemAutofill: Option<LastItemAutofillPreference>
    ): List<ItemData.SuggestedItem> = items
}

@RunWith(JUnit4::class)
class GetSuggestedAutofillItemsImplTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var accountManager: TestAccountManager
    private lateinit var observeItems: TestObserveItems
    private lateinit var getUserPlan: TestGetUserPlan
    private lateinit var filter: FakeSuggestionItemFilterer
    private lateinit var getSuggestedAutofillItems: GetSuggestedAutofillItems
    private lateinit var observeAutofillShares: FakeObserveAutofillShares
    private lateinit var internalSettingsRepository: TestInternalSettingsRepository
    private lateinit var assetLinkRepository: FakeAssetLinkRepository
    private lateinit var userPreferencesRepository: UserPreferencesRepository

    @Before
    fun setUp() {
        accountManager = TestAccountManager()
        observeItems = TestObserveItems()
        getUserPlan = TestGetUserPlan()
        filter = FakeSuggestionItemFilterer()
        observeAutofillShares = FakeObserveAutofillShares()
        internalSettingsRepository = TestInternalSettingsRepository()
        assetLinkRepository = FakeAssetLinkRepository()
        userPreferencesRepository = TestPreferenceRepository()
        getSuggestedAutofillItems = GetSuggestedAutofillItemsImpl(
            accountManager = accountManager,
            observeAutofillShares = observeAutofillShares,
            observeItems = observeItems,
            suggestionItemFilter = filter,
            suggestionSorter = FakeSuggestionSorter(),
            internalSettingsRepository = internalSettingsRepository,
            getUserPlan = getUserPlan,
            assetLinkRepository = assetLinkRepository,
            userPreferencesRepository = userPreferencesRepository
        )
    }

    @Test
    fun `filter is invoked`() = runTest {
        val fixedTitle = "item1"
        val shareId = ShareId("test-share-id")
        val userId = UserId("test-user-id")
        val item1 = TestItem.create(shareId = shareId, title = fixedTitle)
        val item2 = TestItem.create()
        val vaultShare = TestShare.Vault.create(userId = userId.id, id = shareId.id)
        accountManager.setAccounts(listOf(TestAccountManager.DEFAULT_ACCOUNT.copy(userId = userId)))
        observeAutofillShares.setValue(listOf(vaultShare), userId = userId)
        observeItems.emitValue(listOf(item1, item2))
        filter.setFilter { TestKeyStoreCrypto.decrypt(it.title) == fixedTitle }

        val expected: List<ItemData.SuggestedItem> = listOf(item1).map {
            ItemData.SuggestedItem(it, DEFAULT_SUGGESTION)
        }

        getSuggestedAutofillItems(itemTypeFilter = ItemTypeFilter.Logins, DEFAULT_SUGGESTION).test {
            assertEquals(SuggestedAutofillItemsResult.Items(expected), awaitItem())
        }
    }

    @Test
    fun `error is propagated`() = runTest {
        val message = "test exception"
        observeAutofillShares.setValue(emptyList())
        filter.setFilter { true }
        observeItems.sendException(Exception(message))

        getSuggestedAutofillItems(itemTypeFilter = ItemTypeFilter.Logins, DEFAULT_SUGGESTION).test {
            val e = awaitError()
            assertTrue(e is Exception)
            assertEquals(e.message, message)
        }
    }

    @Test
    fun `not supported item type throws exception`() = runTest {
        filter.setFilter { true }

        getSuggestedAutofillItems(itemTypeFilter = ItemTypeFilter.Notes, DEFAULT_SUGGESTION).test {
            val e = awaitError()
            assertTrue(e is Exception)
        }
    }

    @Test
    fun `only suggestions from the usable vaults`() = runTest {
        // GIVEN
        val firstShareId = ShareId("123")
        val vaultShares = listOf(
            TestShare.Vault.create(
                id = firstShareId.id,
                shareRole = ShareRole.Admin,
                name = "default"
            ),
            TestShare.Vault.create(
                id = ShareId("789").id,
                shareRole = ShareRole.Read,
                name = "another"
            )
        )
        val userId = UserId("test-user-id")
        accountManager.setAccounts(listOf(TestAccountManager.DEFAULT_ACCOUNT.copy(userId = userId)))
        observeAutofillShares.setValue(vaultShares, userId)

        filter.setFilter { true }

        val items = listOf(TestItem.create(shareId = firstShareId))
        observeItems.emitValue(items)
        val expected: List<ItemData.SuggestedItem> =
            items.map { ItemData.SuggestedItem(it, DEFAULT_SUGGESTION) }

        // WHEN
        getSuggestedAutofillItems(itemTypeFilter = ItemTypeFilter.Logins, DEFAULT_SUGGESTION).test {

            // THEN
            assertThat(awaitItem()).isEqualTo(SuggestedAutofillItemsResult.Items(expected))
        }
    }

    @Test
    fun `when plan is free and credit cards, then show upgrade`() = runTest {
        val shareId = ShareId("test-share-id")
        val userId = UserId("test-user-id")
        val vaultShare = TestShare.Vault.create(userId = userId.id, id = shareId.id)
        accountManager.setAccounts(listOf(TestAccountManager.DEFAULT_ACCOUNT.copy(userId = userId)))
        getUserPlan.setResult(Result.success(buildPlan(PlanType.Free("", ""))), userId)
        observeItems.emitValue(listOf(TestObserveItems.createCreditCard(shareId = shareId)))
        observeAutofillShares.setValue(listOf(vaultShare), userId)

        filter.setFilter { true }

        getSuggestedAutofillItems(
            itemTypeFilter = ItemTypeFilter.CreditCards,
            DEFAULT_SUGGESTION
        ).test {
            assertThat(awaitItem()).isInstanceOf(SuggestedAutofillItemsResult.ShowUpgrade::class.java)
        }
    }

    @Test
    fun `when plan is paid and no credit cards, show empty list`() = runTest {
        getUserPlan.setResult(Result.success(buildPlan(PlanType.Paid.Plus("", ""))))
        observeItems.emitValue(emptyList())
        filter.setFilter { true }

        val result = getSuggestedAutofillItems(
            itemTypeFilter = ItemTypeFilter.CreditCards,
            DEFAULT_SUGGESTION
        ).first()
        assertThat(result).isInstanceOf(SuggestedAutofillItemsResult.Items::class.java)

        val items = (result as SuggestedAutofillItemsResult.Items).suggestedItems
        assertThat(items).isEmpty()
    }

    @Test
    fun `should filter CC from free plan`() = runTest {
        val freeUserId = UserId("free-user-id")
        val paidUserId = UserId("paid-user-id")
        accountManager.setAccounts(
            listOf(
                TestAccountManager.DEFAULT_ACCOUNT.copy(
                    userId = freeUserId
                ),
                TestAccountManager.DEFAULT_ACCOUNT.copy(
                    userId = paidUserId
                )
            )
        )
        getUserPlan.setResult(
            userId = freeUserId,
            value = Result.success(buildPlan(PlanType.Free("", "")))
        )
        getUserPlan.setResult(
            userId = paidUserId,
            value = Result.success(buildPlan(PlanType.Paid.Plus("", "")))
        )
        val freeShareId = ShareId("free-share-id")
        val paidShareId = ShareId("paid-share-id")
        val freeCCItem = TestObserveItems.createCreditCard(shareId = freeShareId)
        val paidCCItem = TestObserveItems.createCreditCard(shareId = paidShareId)
        observeItems.emit(
            TestObserveItems.Params(
                userId = freeUserId,
                filter = ItemTypeFilter.CreditCards,
                selection = ShareSelection.Shares(listOf(freeShareId))
            ),
            listOf(freeCCItem)
        )
        observeItems.emit(
            TestObserveItems.Params(
                userId = paidUserId,
                filter = ItemTypeFilter.CreditCards,
                selection = ShareSelection.Shares(listOf(paidShareId))
            ),
            listOf(paidCCItem)
        )

        observeAutofillShares.setValues(
            values = mapOf(
                freeUserId to listOf(
                    TestShare.Vault.create(
                        userId = freeUserId.id,
                        id = freeShareId.id,
                        vaultId = VaultId("free-vault-id").id
                    )
                ),
                paidUserId to listOf(
                    TestShare.Vault.create(
                        userId = paidUserId.id,
                        id = paidShareId.id,
                        vaultId = VaultId("paid-vault-id").id
                    )
                )
            )
        )

        filter.setFilter { true }

        getSuggestedAutofillItems(
            itemTypeFilter = ItemTypeFilter.CreditCards,
            DEFAULT_SUGGESTION
        ).test {
            val result = awaitItem()
            assertThat(result).isInstanceOf(SuggestedAutofillItemsResult.Items::class.java)

            val items = (result as SuggestedAutofillItemsResult.Items).suggestedItems
            assertThat(items).hasSize(1)
            assertThat(items.first().item.shareId).isEqualTo(paidShareId)
        }
    }

    private fun buildPlan(planType: PlanType) = Plan(
        planType = planType,
        hideUpgrade = false,
        vaultLimit = PlanLimit.Unlimited,
        aliasLimit = PlanLimit.Unlimited,
        totpLimit = PlanLimit.Unlimited,
        updatedAt = 0
    )

    companion object {
        private val DEFAULT_SUGGESTION = Suggestion.PackageName("com.example")
    }
}

