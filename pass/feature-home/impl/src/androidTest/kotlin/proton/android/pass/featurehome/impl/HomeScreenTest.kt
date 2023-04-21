package proton.android.pass.featurehome.impl

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.components.SingletonComponent
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
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.data.api.SearchEntry
import proton.android.pass.data.api.repositories.ItemSyncStatus
import proton.android.pass.data.fakes.usecases.TestItemSyncStatusRepository
import proton.android.pass.data.fakes.usecases.TestObserveItems
import proton.android.pass.data.fakes.usecases.TestObserveSearchEntry
import proton.android.pass.data.fakes.usecases.TestObserveVaults
import proton.android.pass.preferences.HasCompletedOnBoarding
import proton.android.pass.preferences.HasDismissedAutofillBanner
import proton.android.pass.preferences.TestPreferenceRepository
import proton.android.pass.test.CallChecker
import proton.android.pass.test.HiltComponentActivity
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
        composeTestRule.setContent {
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
                    onAddItemClick = {_, _ -> },
                    onCreateVaultClick = {},
                    onEditVaultClick = {},
                    onDeleteVaultClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText(loginItemTitle).performClick()
        composeTestRule.waitUntil { checker.isCalled }
        assert(checker.memory == items[0].shareId to items[0].id)
    }

    private fun setupWithItems(items: List<Item>) {
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

        itemSyncStatusRepository.emit(ItemSyncStatus.Synced(hasItems = true))
        observeVaults.sendResult(LoadingResult.Success(vaults))
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
