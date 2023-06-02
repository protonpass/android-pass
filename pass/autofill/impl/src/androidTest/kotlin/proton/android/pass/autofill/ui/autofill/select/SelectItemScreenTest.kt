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
import proton.android.featuresearchoptions.impl.SearchOptionsModule
import proton.android.pass.autofill.entities.AutofillAppState
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
import proton.android.pass.test.CallChecker
import proton.android.pass.test.HiltComponentActivity
import proton.android.pass.test.waitUntilExists
import proton.pass.domain.HiddenState
import proton.pass.domain.Item
import proton.pass.domain.ItemContents
import proton.pass.domain.ItemId
import proton.pass.domain.Plan
import proton.pass.domain.PlanLimit
import proton.pass.domain.PlanType
import proton.pass.domain.ShareId
import proton.pass.domain.Vault
import javax.inject.Inject

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
            planType = PlanType.Free
        )

        upgradeTest()
    }

    @Test
    fun showsUpgradeScreenWhenYesSuggestionsAndNoOtherItems() {
        performSetup(
            vaults = 2,
            suggestions = 2,
            otherItems = 0,
            planType = PlanType.Free
        )

        upgradeTest()
    }

    @Test
    fun showsUpgradeScreenWhenNoSuggestionsAndYesOtherItems() {
        performSetup(
            vaults = 2,
            suggestions = 0,
            otherItems = 2,
            planType = PlanType.Free
        )

        upgradeTest()
    }

    @Test
    fun showsUpgradeScreenWhenYesSuggestionsAndYesOtherItems() {
        performSetup(
            vaults = 2,
            suggestions = 2,
            otherItems = 2,
            planType = PlanType.Free
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

            val text = activity.getString(R.string.autofill_only_searching_in_primary_vault)
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
                isPrimary = it == 0
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
                plan = plan,
                totalVaults = 1,
                totalAlias = 1,
                totalTotp = 1
            )
        )

        return SetupData(vaultList, suggestionsList, otherItemsList)
    }

    private fun fakeAutofillState() = AutofillAppState(
        androidAutofillIds = emptyList(),
        fieldTypes = emptyList(),
        packageInfoUi = null,
        webDomain = None,
        title = ""
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
    }

    companion object {
        private const val SUGGESTION_TITLE_PREFIX = "suggestion-"
        private const val OTHER_ITEM_TITLE_PREFIX = "other-"
        private const val SUGGESTION_USERNAME_PREFIX = "username-"
    }
}
