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

package proton.android.pass.features.selectitem.ui

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import kotlinx.datetime.Clock
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.account.fakes.FakeUserManager
import proton.android.pass.account.fakes.FakeAccountManager
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.crypto.fakes.context.FakeEncryptionContext
import proton.android.pass.data.api.usecases.ItemData
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.SuggestedAutofillItemsResult
import proton.android.pass.data.api.usecases.Suggestion
import proton.android.pass.data.api.usecases.UpgradeInfo
import proton.android.pass.data.fakes.usecases.FakeGetSuggestedAutofillItems
import proton.android.pass.data.fakes.usecases.FakeGetUserPlan
import proton.android.pass.data.fakes.usecases.FakeObserveItems
import proton.android.pass.data.fakes.usecases.FakeObserveUpgradeInfo
import proton.android.pass.data.fakes.usecases.shares.FakeObserveAutofillShares
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.Plan
import proton.android.pass.domain.PlanLimit
import proton.android.pass.domain.PlanType
import proton.android.pass.domain.Share
import proton.android.pass.domain.entity.AppName
import proton.android.pass.domain.entity.PackageInfo
import proton.android.pass.domain.entity.PackageName
import proton.android.pass.features.selectitem.R
import proton.android.pass.features.selectitem.navigation.SelectItemNavigation
import proton.android.pass.features.selectitem.navigation.SelectItemState
import proton.android.pass.searchoptions.impl.SearchOptionsModule
import proton.android.pass.test.CallChecker
import proton.android.pass.test.HiltComponentActivity
import proton.android.pass.test.TestConstants
import proton.android.pass.test.domain.ItemTestFactory
import proton.android.pass.test.domain.ShareTestFactory
import proton.android.pass.test.waitUntilExists
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(SearchOptionsModule::class)
class SelectItemScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()

    @Inject
    lateinit var getSuggestedLoginItems: FakeGetSuggestedAutofillItems

    @Inject
    lateinit var observeAutofillShares: FakeObserveAutofillShares

    @Inject
    lateinit var getUserPlan: FakeGetUserPlan

    @Inject
    lateinit var observeUpgradeInfo: FakeObserveUpgradeInfo

    @Inject
    lateinit var observeItems: FakeObserveItems

    @Inject
    lateinit var accountManager: FakeAccountManager

    @Inject
    lateinit var userManager: FakeUserManager

    @Before
    fun setup() {
        hiltRule.inject()
    }

    // Click on item
    @Test
    fun canSelectSuggestionWithNoOtherItems() {
        performSetup(
            sharesCount = 1,
            suggestions = 3,
            otherItems = 0,
            planType = PlanType.Paid.Plus("", "")
        )
        clickOnItemTest("${SUGGESTION_TITLE_PREFIX}0", ExpectedItemClicked.Suggestion)
    }

    @Test
    fun canSelectSuggestionWithOtherItems() {
        performSetup(
            sharesCount = 1,
            suggestions = 3,
            otherItems = 2,
            planType = PlanType.Paid.Plus("", "")
        )
        clickOnItemTest("${SUGGESTION_TITLE_PREFIX}0", ExpectedItemClicked.Suggestion)
    }

    @Test
    fun canSelectOtherItemWithNoSuggestions() {
        performSetup(
            sharesCount = 1,
            suggestions = 0,
            otherItems = 2,
            planType = PlanType.Paid.Plus("", "")
        )

        clickOnItemTest("${OTHER_ITEM_TITLE_PREFIX}0", ExpectedItemClicked.Item)
    }

    @Test
    fun canSelectOtherItemWithSuggestions() {
        performSetup(
            sharesCount = 1,
            suggestions = 3,
            otherItems = 2,
            planType = PlanType.Paid.Plus("", "")
        )

        clickOnItemTest("${OTHER_ITEM_TITLE_PREFIX}0", ExpectedItemClicked.Item)
    }

    private enum class ExpectedItemClicked {
        Item,
        Suggestion
    }

    private fun clickOnItemTest(text: String, expected: ExpectedItemClicked) {
        val checker = CallChecker<Unit>()
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    SelectItemScreen(
                        state = fakeAutofillState(),
                        onScreenShown = {},
                        onNavigate = {
                            when (it) {
                                is SelectItemNavigation.ItemSelected -> {
                                    if (expected == ExpectedItemClicked.Item) {
                                        checker.call()
                                    }
                                }

                                is SelectItemNavigation.SuggestionSelected -> {
                                    if (expected == ExpectedItemClicked.Suggestion) {
                                        checker.call()
                                    }
                                }

                                else -> {}
                            }
                        }
                    )
                }
            }

            waitUntilExists(hasText(text))
            onNode(hasText(text)).performClick()
            waitUntil { checker.isCalled }
        }
    }

    // UPGRADE

    @Test
    fun showsUpgradeScreenWhenNoSuggestionsAndNoOtherItems() {
        performSetup(
            sharesCount = 2,
            suggestions = 0,
            otherItems = 0,
            planType = TestConstants.FreePlanType
        )

        upgradeTest()
    }

    @Test
    fun showsUpgradeScreenWhenYesSuggestionsAndNoOtherItems() {
        performSetup(
            sharesCount = 2,
            suggestions = 2,
            otherItems = 0,
            planType = TestConstants.FreePlanType
        )

        upgradeTest()
    }

    @Test
    fun showsUpgradeScreenWhenNoSuggestionsAndYesOtherItems() {
        performSetup(
            sharesCount = 2,
            suggestions = 0,
            otherItems = 2,
            planType = TestConstants.FreePlanType
        )

        upgradeTest()
    }

    @Test
    fun showsUpgradeScreenWhenYesSuggestionsAndYesOtherItems() {
        performSetup(
            sharesCount = 2,
            suggestions = 2,
            otherItems = 2,
            planType = TestConstants.FreePlanType
        )

        upgradeTest()
    }

    private fun upgradeTest() {
        val checker = CallChecker<Unit>()
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    SelectItemScreen(
                        state = fakeAutofillState(),
                        onScreenShown = {},
                        onNavigate = {
                            when (it) {
                                is SelectItemNavigation.Upgrade -> {
                                    checker.call()
                                }

                                else -> {}
                            }
                        }
                    )
                }
            }

            val text = activity.getString(R.string.select_item_only_searching_in_oldest_vaults)
            waitUntilExists(hasText(text, substring = true))
            onNode(hasText(text, substring = true)).performClick()
            waitUntil { checker.isCalled }
        }
    }

    // SEARCH

    @Test
    fun canSearchForSuggestion() {
        performSetup(
            sharesCount = 1,
            suggestions = 3,
            otherItems = 0,
            planType = PlanType.Paid.Plus("", "")
        )

        searchTest(query = SUGGESTION_TITLE_PREFIX, itemText = "${SUGGESTION_TITLE_PREFIX}0")
    }

    @Test
    fun canSearchForItem() {
        performSetup(
            sharesCount = 1,
            suggestions = 1,
            otherItems = 2,
            planType = PlanType.Paid.Plus("", "")
        )

        searchTest(query = OTHER_ITEM_TITLE_PREFIX, itemText = "${OTHER_ITEM_TITLE_PREFIX}0")
    }

    private fun searchTest(query: String, itemText: String) {
        val checker = CallChecker<Unit>()
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    SelectItemScreen(
                        state = fakeAutofillState(),
                        onScreenShown = {},
                        onNavigate = {
                            when (it) {
                                is SelectItemNavigation.ItemSelected -> {
                                    checker.call()
                                }

                                else -> {}
                            }
                        }
                    )
                }
            }

            val text = activity.getString(R.string.topbar_search_query)
            waitUntilExists(hasText(text))
            onNode(hasText(text)).performClick()
            onNode(hasText(text)).performTextInput(query)

            Espresso.closeSoftKeyboard()

            waitUntilExists(hasText(itemText))
            onNode(hasText(itemText)).performClick()

            waitUntil { checker.isCalled }
        }
    }

    private fun performSetup(
        sharesCount: Int,
        suggestions: Int,
        otherItems: Int,
        planType: PlanType
    ): SetupData {
        val userId = UserId("test-user-id")
        accountManager.setAccounts(listOf(FakeAccountManager.DEFAULT_ACCOUNT.copy(userId = userId)))
        userManager.setUser(FakeUserManager.DEFAULT_USER.copy(userId = userId))
        val vaultShares = (0 until sharesCount).map {
            ShareTestFactory.Vault.create(
                userId = userId.id,
                id = "shareid-test-$it",
                vaultId = "vaultid-test-$it",
                name = "testVault-$it"
            )
        }

        observeAutofillShares.setValue(vaultShares, userId)

        val shareId = vaultShares.first().id
        val suggestionsList = (0 until suggestions).map {
            val item = ItemTestFactory.create(
                shareId = shareId,
                itemId = ItemId("itemid-suggestion-$it"),
                itemContents = ItemContents.Login(
                    title = "$SUGGESTION_TITLE_PREFIX$it",
                    note = "",
                    itemEmail = "$SUGGESTION_EMAIL_PREFIX$it",
                    itemUsername = "",
                    password = HiddenState.Concealed(FakeEncryptionContext.encrypt("")),
                    urls = emptyList(),
                    packageInfoSet = setOf(
                        PackageInfo(PackageName(PACKAGE_NAME), AppName(""))
                    ),
                    primaryTotp = HiddenState.Revealed(FakeEncryptionContext.encrypt(""), ""),
                    customFields = emptyList(),
                    passkeys = emptyList()
                )
            )
            ItemData.SuggestedItem(item, Suggestion.Url("$it"))
        }
        getSuggestedLoginItems.sendValue(
            itemTypeFilter = ItemTypeFilter.Logins,
            value = Result.success(SuggestedAutofillItemsResult.Items(suggestionsList))
        )

        val otherItemsList = (0 until otherItems).map {
            ItemTestFactory.create(
                shareId = shareId,
                itemId = ItemId("itemid-other-$it"),
                itemContents = ItemContents.Login(
                    title = "$OTHER_ITEM_TITLE_PREFIX$it",
                    note = "",
                    itemEmail = "$SUGGESTION_EMAIL_PREFIX$it",
                    itemUsername = "",
                    password = HiddenState.Concealed(FakeEncryptionContext.encrypt("")),
                    urls = emptyList(),
                    packageInfoSet = setOf(
                        PackageInfo(PackageName(PACKAGE_NAME), AppName(""))
                    ),
                    primaryTotp = HiddenState.Revealed(FakeEncryptionContext.encrypt(""), ""),
                    customFields = emptyList(),
                    passkeys = emptyList()
                )
            )
        }
        val otherItemsPlusSuggestionsList = otherItemsList + suggestionsList.map { it.item }
        observeItems.emitValue(otherItemsPlusSuggestionsList)

        val plan = Plan(
            planType = planType,
            hideUpgrade = false,
            vaultLimit = PlanLimit.Limited(1),
            aliasLimit = PlanLimit.Limited(1),
            totpLimit = PlanLimit.Limited(1),
            updatedAt = Clock.System.now().epochSeconds
        )
        getUserPlan.setResult(Result.success(plan), userId)

        observeUpgradeInfo.setResult(
            UpgradeInfo(
                isUpgradeAvailable = true,
                isSubscriptionAvailable = true,
                plan = plan,
                totalVaults = 1,
                totalAlias = 1,
                totalTotp = 1
            )
        )

        return SetupData(vaultShares, suggestionsList, otherItemsList)
    }

    private fun fakeAutofillState() = SelectItemState.Autofill.Login(
        title = "Some title",
        suggestion = Suggestion.PackageName(PACKAGE_NAME)
    )

    data class SetupData(
        val shares: List<Share>,
        val suggestions: List<ItemData.SuggestedItem>,
        val otherItems: List<Item>
    )

    @Module
    @InstallIn(SingletonComponent::class)
    object TestModule {
        @Provides
        fun provideClock(): Clock = Clock.System
    }

    private companion object {
        private const val SUGGESTION_TITLE_PREFIX = "suggestion-"
        private const val OTHER_ITEM_TITLE_PREFIX = "other-"
        private const val SUGGESTION_EMAIL_PREFIX = "email-"

        private const val PACKAGE_NAME = "some.package.name"
    }
}
