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

package proton.android.pass.autofill.ui.autofill.select

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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.autofill.AppIcon
import proton.android.pass.autofill.entities.AssistField
import proton.android.pass.autofill.entities.AssistInfo
import proton.android.pass.autofill.entities.AutofillAppState
import proton.android.pass.autofill.entities.AutofillData
import proton.android.pass.autofill.heuristics.NodeCluster
import proton.android.pass.autofill.service.R
import proton.android.pass.autofill.ui.autofill.navigation.SelectItemNavigation
import proton.android.pass.common.api.None
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.crypto.fakes.context.TestEncryptionContext
import proton.android.pass.data.api.usecases.UpgradeInfo
import proton.android.pass.data.fakes.usecases.TestGetSuggestedLoginItems
import proton.android.pass.data.fakes.usecases.TestGetUserPlan
import proton.android.pass.data.fakes.usecases.TestObserveActiveItems
import proton.android.pass.data.fakes.usecases.TestObserveItems
import proton.android.pass.data.fakes.usecases.TestObserveUpgradeInfo
import proton.android.pass.data.fakes.usecases.TestObserveVaults
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.Plan
import proton.android.pass.domain.PlanLimit
import proton.android.pass.domain.PlanType
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import proton.android.pass.domain.entity.AppName
import proton.android.pass.domain.entity.PackageInfo
import proton.android.pass.domain.entity.PackageName
import proton.android.pass.featuresearchoptions.impl.SearchOptionsModule
import proton.android.pass.test.CallChecker
import proton.android.pass.test.HiltComponentActivity
import proton.android.pass.test.TestConstants
import proton.android.pass.test.waitUntilExists
import javax.inject.Inject
import me.proton.core.presentation.R as CoreR

@HiltAndroidTest
@UninstallModules(SearchOptionsModule::class)
class SelectItemScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()

    @Inject
    lateinit var getSuggestedLoginItems: TestGetSuggestedLoginItems

    @Inject
    lateinit var observeVaults: TestObserveVaults

    @Inject
    lateinit var getUserPlan: TestGetUserPlan

    @Inject
    lateinit var observeUpgradeInfo: TestObserveUpgradeInfo

    @Inject
    lateinit var observeActiveItems: TestObserveActiveItems

    @Before
    fun setup() {
        hiltRule.inject()
    }

    // Click on item
    @Test
    fun canSelectSuggestionWithNoOtherItems() {
        performSetup(
            vaults = 1,
            suggestions = 3,
            otherItems = 0,
            planType = PlanType.Paid("", "")
        )
        clickOnItemTest("${SUGGESTION_TITLE_PREFIX}0")
    }

    @Test
    fun canSelectSuggestionWithOtherItems() {
        performSetup(
            vaults = 1,
            suggestions = 3,
            otherItems = 2,
            planType = PlanType.Paid("", "")
        )
        clickOnItemTest("${SUGGESTION_TITLE_PREFIX}0")
    }

    @Test
    fun canSelectOtherItemWithNoSuggestions() {
        performSetup(
            vaults = 1,
            suggestions = 0,
            otherItems = 2,
            planType = PlanType.Paid("", "")
        )

        clickOnItemTest("${OTHER_ITEM_TITLE_PREFIX}0")
    }

    @Test
    fun canSelectOtherItemWithSuggestions() {
        performSetup(
            vaults = 1,
            suggestions = 3,
            otherItems = 2,
            planType = PlanType.Paid("", "")
        )

        clickOnItemTest("${OTHER_ITEM_TITLE_PREFIX}0")
    }

    private fun clickOnItemTest(text: String) {
        val checker = CallChecker<Unit>()
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    SelectItemScreen(
                        autofillAppState = fakeAutofillState(),
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

            waitUntilExists(hasText(text))
            onNode(hasText(text)).performClick()
            waitUntil { checker.isCalled }
        }
    }

    // UPGRADE

    @Test
    fun showsUpgradeScreenWhenNoSuggestionsAndNoOtherItems() {
        performSetup(
            vaults = 2,
            suggestions = 0,
            otherItems = 0,
            planType = TestConstants.FreePlanType
        )

        upgradeTest()
    }

    @Test
    fun showsUpgradeScreenWhenYesSuggestionsAndNoOtherItems() {
        performSetup(
            vaults = 2,
            suggestions = 2,
            otherItems = 0,
            planType = TestConstants.FreePlanType
        )

        upgradeTest()
    }

    @Test
    fun showsUpgradeScreenWhenNoSuggestionsAndYesOtherItems() {
        performSetup(
            vaults = 2,
            suggestions = 0,
            otherItems = 2,
            planType = TestConstants.FreePlanType
        )

        upgradeTest()
    }

    @Test
    fun showsUpgradeScreenWhenYesSuggestionsAndYesOtherItems() {
        performSetup(
            vaults = 2,
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
                        autofillAppState = fakeAutofillState(),
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

            val text = activity.getString(R.string.autofill_only_searching_in_oldest_vaults)
            waitUntilExists(hasText(text, substring = true))
            onNode(hasText(text, substring = true)).performClick()
            waitUntil { checker.isCalled }
        }
    }

    // SEARCH

    @Test
    fun canSearchForSuggestion() {
        performSetup(
            vaults = 1,
            suggestions = 3,
            otherItems = 0,
            planType = PlanType.Paid("", "")
        )

        searchTest(query = SUGGESTION_TITLE_PREFIX, itemText = "${SUGGESTION_TITLE_PREFIX}0")
    }

    @Test
    fun canSearchForItem() {
        performSetup(
            vaults = 1,
            suggestions = 1,
            otherItems = 2,
            planType = PlanType.Paid("", "")
        )

        searchTest(query = OTHER_ITEM_TITLE_PREFIX, itemText = "${OTHER_ITEM_TITLE_PREFIX}0")
    }

    private fun searchTest(query: String, itemText: String) {
        val checker = CallChecker<Unit>()
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    SelectItemScreen(
                        autofillAppState = fakeAutofillState(),
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
        vaults: Int,
        suggestions: Int,
        otherItems: Int,
        planType: PlanType
    ): SetupData {
        val vaultList = (0 until vaults).map {
            val shareId = ShareId("shareid-test-$it")
            Vault(
                shareId = shareId,
                name = "testVault-$it",
            )
        }
        observeVaults.sendResult(Result.success(vaultList))

        val shareId = vaultList.first().shareId
        val suggestionsList = (0 until suggestions).map {
            TestObserveItems.createItem(
                shareId = shareId,
                itemId = ItemId("itemid-suggestion-$it"),
                itemContents = ItemContents.Login(
                    title = "${SUGGESTION_TITLE_PREFIX}$it",
                    note = "",
                    username = "${SUGGESTION_USERNAME_PREFIX}$it",
                    password = HiddenState.Concealed(TestEncryptionContext.encrypt("")),
                    urls = emptyList(),
                    packageInfoSet = emptySet(),
                    primaryTotp = HiddenState.Revealed(TestEncryptionContext.encrypt(""), ""),
                    customFields = emptyList()
                )
            )
        }
        getSuggestedLoginItems.sendValue(Result.success(suggestionsList))

        val otherItemsList = (0 until otherItems).map {
            TestObserveItems.createItem(
                shareId = shareId,
                itemId = ItemId("itemid-other-$it"),
                itemContents = ItemContents.Login(
                    title = "${OTHER_ITEM_TITLE_PREFIX}$it",
                    note = "",
                    username = "${SUGGESTION_USERNAME_PREFIX}$it",
                    password = HiddenState.Concealed(TestEncryptionContext.encrypt("")),
                    urls = emptyList(),
                    packageInfoSet = emptySet(),
                    primaryTotp = HiddenState.Revealed(TestEncryptionContext.encrypt(""), ""),
                    customFields = emptyList()
                )
            )
        }
        val otherItemsPlusSuggestionsList = otherItemsList + suggestionsList
        observeActiveItems.sendItemList(otherItemsPlusSuggestionsList)

        val plan = Plan(
            planType = planType,
            hideUpgrade = false,
            vaultLimit = PlanLimit.Limited(1),
            aliasLimit = PlanLimit.Limited(1),
            totpLimit = PlanLimit.Limited(1),
            updatedAt = Clock.System.now().epochSeconds
        )
        getUserPlan.setResult(Result.success(plan))

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

        return SetupData(vaultList, suggestionsList, otherItemsList)
    }

    private fun fakeAutofillState() = AutofillAppState(
        autofillData = AutofillData(
            assistInfo = AssistInfo(
                cluster = NodeCluster.Login.UsernameAndPassword(
                    username = AssistField(
                        id = newAutofillFieldId(),
                        type = null,
                        value = null,
                        text = null,
                        isFocused = false,
                        nodePath = listOf()
                    ),
                    password = AssistField(
                        id = newAutofillFieldId(),
                        type = null,
                        value = null,
                        text = null,
                        isFocused = false,
                        nodePath = listOf()
                    )
                ),
                url = None
            ),
            packageInfo = PackageInfo(
                packageName = PackageName("some.app"),
                appName = AppName("Some app")
            ),
            isDangerousAutofill = false
        )
    )

    data class SetupData(
        val vaults: List<Vault>,
        val suggestions: List<Item>,
        val otherItems: List<Item>
    )

    @Module
    @InstallIn(SingletonComponent::class)
    object TestModule {
        @Provides
        fun provideClock(): Clock = Clock.System

        @Provides
        @AppIcon
        fun provideIcon(): Int = CoreR.drawable.ic_proton_shield_half_filled
    }

    companion object {
        private const val SUGGESTION_TITLE_PREFIX = "suggestion-"
        private const val OTHER_ITEM_TITLE_PREFIX = "other-"
        private const val SUGGESTION_USERNAME_PREFIX = "username-"
    }
}
