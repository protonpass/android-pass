package proton.android.pass.featurehome.impl

import androidx.annotation.StringRes
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.components.SingletonComponent
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.autofill.api.AutofillStatus
import proton.android.pass.autofill.api.AutofillSupportedStatus
import proton.android.pass.autofill.fakes.TestAutofillManager
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonuimodels.api.ItemTypeUiState
import proton.android.pass.data.api.SearchEntry
import proton.android.pass.data.api.repositories.ItemSyncStatus
import proton.android.pass.data.fakes.usecases.TestItemSyncStatusRepository
import proton.android.pass.data.fakes.usecases.TestObserveItems
import proton.android.pass.data.fakes.usecases.TestObserveSearchEntry
import proton.android.pass.data.fakes.usecases.TestObserveVaults
import proton.android.pass.featurehome.impl.HomeContentTestTag.DrawerIconTestTag
import proton.android.pass.preferences.HasCompletedOnBoarding
import proton.android.pass.preferences.HasDismissedAutofillBanner
import proton.android.pass.preferences.TestPreferenceRepository
import proton.android.pass.test.CallChecker
import proton.android.pass.test.HiltComponentActivity
import proton.android.pass.test.waitUntilExists
import proton.pass.domain.Item
import proton.pass.domain.ItemId
import proton.pass.domain.ShareColor
import proton.pass.domain.ShareIcon
import proton.pass.domain.ShareId
import proton.pass.domain.Vault
import javax.inject.Inject

@HiltAndroidTest
class HomeScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()

    @Inject
    lateinit var itemSyncStatusRepository: TestItemSyncStatusRepository

    @Inject
    lateinit var observeItems: TestObserveItems

    @Inject
    lateinit var observeVaults: TestObserveVaults

    @Inject
    lateinit var observeSearchEntry: TestObserveSearchEntry

    @Inject
    lateinit var autofillManager: TestAutofillManager

    @Inject
    lateinit var preferencesRepository: TestPreferenceRepository

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Test
    fun canNavigateToItemDetail() {
        val shareId = ShareId("shareId")
        val loginItemId = ItemId("login")
        val loginItemTitle = "login item"
        val items = listOf(
            TestObserveItems.createLogin(shareId, loginItemId, loginItemTitle),
            TestObserveItems.createAlias(shareId, ItemId("alias"), "alias-item"),
            TestObserveItems.createNote(shareId, ItemId("note"), "note-item"),
        )
        setupWithItems(items)

        val checker = CallChecker<Pair<ShareId, ItemId>>()
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    HomeScreen(
                        homeScreenNavigation = HomeScreenNavigation(
                            toEditLogin = { _, _ -> },
                            toEditNote = { _, _ -> },
                            toEditAlias = { _, _ -> },
                            toItemDetail = { shareId, itemId ->
                                checker.call(shareId to itemId)
                            },
                            toAuth = {},
                            toProfile = {},
                            toOnBoarding = {},
                        ),
                        onAddItemClick = { _, _ -> },
                        onCreateVaultClick = {},
                        onEditVaultClick = {},
                        onDeleteVaultClick = {},
                    )
                }
            }

            waitUntilExists(hasText(loginItemTitle))
            onNode(hasText(loginItemTitle)).performClick()
            composeTestRule.waitUntil { checker.isCalled }
        }

        val loginItem = items.first()
        assertEquals(loginItem.shareId to items[0].id, checker.memory)
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Test
    fun canNavigateToCreateVault() {
        val checker = CallChecker<Unit>()
        composeTestRule.setContent {
            PassTheme(isDark = true) {
                HomeScreen(
                    homeScreenNavigation = HomeScreenNavigation(
                        toEditLogin = { _, _ -> },
                        toEditNote = { _, _ -> },
                        toEditAlias = { _, _ -> },
                        toItemDetail = { _, _ -> },
                        toAuth = {},
                        toProfile = {},
                        toOnBoarding = {},
                    ),
                    onAddItemClick = { _, _ -> },
                    onCreateVaultClick = { checker.call() },
                    onEditVaultClick = {},
                    onDeleteVaultClick = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(DrawerIconTestTag).performClick()
        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.vault_drawer_create_vault))
            .performClick()
        composeTestRule.waitUntil { checker.isCalled }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Test
    fun canNavigateToCreateItem() {
        setupWithItems()

        val checker = CallChecker<ItemTypeUiState>()
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    HomeScreen(
                        homeScreenNavigation = HomeScreenNavigation(
                            toEditLogin = { _, _ -> },
                            toEditNote = { _, _ -> },
                            toEditAlias = { _, _ -> },
                            toItemDetail = { _, _ -> },
                            toAuth = {},
                            toProfile = {},
                            toOnBoarding = {},
                        ),
                        onAddItemClick = { _, itemType ->
                            checker.call(itemType)
                        },
                        onCreateVaultClick = { },
                        onEditVaultClick = {},
                        onDeleteVaultClick = {},
                    )
                }
            }

            val contentDescription = activity.getString(
                proton.android.pass.composecomponents.impl.R.string.bottom_bar_add_item_icon_content_description
            )
            onNodeWithContentDescription(contentDescription).performClick()

            composeTestRule.waitUntil { checker.isCalled }
        }

        assertEquals(ItemTypeUiState.Unknown, checker.memory)
    }

    @Test
    fun canNavigateToCreateLogin() {
        testEmptyScreenCreateItem(R.string.home_empty_vault_create_login, ItemTypeUiState.Login)
    }

    @Test
    fun canNavigateToCreateAlias() {
        testEmptyScreenCreateItem(R.string.home_empty_vault_create_alias, ItemTypeUiState.Alias)
    }

    @Test
    fun canNavigateToCreateNote() {
        testEmptyScreenCreateItem(R.string.home_empty_vault_create_note, ItemTypeUiState.Note)
    }

    @OptIn(ExperimentalMaterialApi::class)
    private fun testEmptyScreenCreateItem(
        @StringRes text: Int,
        itemTypeUiState: ItemTypeUiState
    ) {
        setupWithItems(emptyList())

        val checker = CallChecker<ItemTypeUiState>()
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    HomeScreen(
                        homeScreenNavigation = HomeScreenNavigation(
                            toEditLogin = { _, _ -> },
                            toEditNote = { _, _ -> },
                            toEditAlias = { _, _ -> },
                            toItemDetail = { _, _ -> },
                            toAuth = {},
                            toProfile = {},
                            toOnBoarding = {},
                        ),
                        onAddItemClick = { _, itemType ->
                            checker.call(itemType)
                        },
                        onCreateVaultClick = { },
                        onEditVaultClick = {},
                        onDeleteVaultClick = {},
                    )
                }
            }

            val targetText = activity.getString(text)
            waitUntilExists(hasText(targetText))

            onNode(hasText(targetText)).performClick()
            waitUntil { checker.isCalled }
        }

        assertEquals(itemTypeUiState, checker.memory)
    }

    private fun setupWithItems(items: List<Item> = TestObserveItems.defaultValues.asList()) {
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.EnabledByOurService))
        runBlocking {
            preferencesRepository.setHasCompletedOnBoarding(HasCompletedOnBoarding.Completed)
            preferencesRepository.setHasDismissedAutofillBanner(HasDismissedAutofillBanner.Dismissed)
        }

        val vaults = items
            .map { it.shareId }
            .distinct()
            .map { shareId ->
                Vault(
                    shareId = shareId,
                    name = "Vault ${shareId.id}",
                    color = ShareColor.Color1,
                    icon = ShareIcon.Icon1,
                    isPrimary = false
                )
            }

        val searchEntries = items.map {
            SearchEntry(
                itemId = it.id,
                shareId = it.shareId,
                userId = UserId("userid"),
                createTime = Clock.System.now().toJavaInstant().epochSecond
            )
        }

        itemSyncStatusRepository.emit(ItemSyncStatus.Synced(hasItems = items.isNotEmpty()))
        observeVaults.sendResult(Result.success(vaults))
        observeItems.emitValue(items)
        observeSearchEntry.emit(searchEntries)

    }

    @Module
    @InstallIn(SingletonComponent::class)
    object TestModule {
        @Provides
        fun provideClock(): Clock = Clock.System
    }

}
