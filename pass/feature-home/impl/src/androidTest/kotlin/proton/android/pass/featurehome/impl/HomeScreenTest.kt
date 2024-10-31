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

package proton.android.pass.featurehome.impl

import androidx.annotation.StringRes
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.google.common.truth.Truth.assertThat
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.components.SingletonComponent
import junit.framework.TestCase.assertEquals
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
import proton.android.pass.data.fakes.usecases.FakeObserveEncryptedItems
import proton.android.pass.data.fakes.usecases.TestItemSyncStatusRepository
import proton.android.pass.data.fakes.usecases.TestObserveSearchEntry
import proton.android.pass.data.fakes.usecases.TestObserveVaults
import proton.android.pass.data.fakes.usecases.TestObserveVaultsWithItemCount
import proton.android.pass.data.fakes.usecases.TestTrashItems
import proton.android.pass.domain.ItemEncrypted
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.ShareIcon
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import proton.android.pass.domain.VaultId
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.featurehome.impl.HomeContentTestTag.DRAWER_ICON_TEST_TAG
import proton.android.pass.preferences.HasCompletedOnBoarding
import proton.android.pass.preferences.HasDismissedAutofillBanner
import proton.android.pass.preferences.TestPreferenceRepository
import proton.android.pass.preferences.UseFaviconsPreference
import proton.android.pass.test.CallChecker
import proton.android.pass.test.HiltComponentActivity
import proton.android.pass.test.waitUntilExists
import java.util.Date
import javax.inject.Inject
import proton.android.pass.composecomponents.impl.R as CompR
import proton.android.pass.features.trash.R as TrashR

@HiltAndroidTest
class HomeScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()

    @Inject
    lateinit var itemSyncStatusRepository: TestItemSyncStatusRepository

    @Inject
    lateinit var observeEncryptedItems: FakeObserveEncryptedItems

    @Inject
    lateinit var observeVaults: TestObserveVaults

    @Inject
    lateinit var observeVaultsWithItemCount: TestObserveVaultsWithItemCount

    @Inject
    lateinit var observeSearchEntry: TestObserveSearchEntry

    @Inject
    lateinit var autofillManager: TestAutofillManager

    @Inject
    lateinit var preferencesRepository: TestPreferenceRepository

    @Inject
    lateinit var trashItem: TestTrashItems

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun canNavigateToItemDetail() {
        val shareId = ShareId("shareId")
        val loginItemId = ItemId("login")
        val loginItemTitle = "login item"
        val items = listOf(
            FakeObserveEncryptedItems.createLogin(shareId, loginItemId, loginItemTitle),
            FakeObserveEncryptedItems.createAlias(shareId, ItemId("alias"), "alias-item"),
            FakeObserveEncryptedItems.createNote(shareId, ItemId("note"), "note-item"),
        )
        setupWithItems(items)

        val checker = CallChecker<Pair<ShareId, ItemId>>()
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    HomeScreen(
                        onNavigateEvent = {
                            if (it is HomeNavigation.ItemDetail) {
                                checker.call(it.shareId to it.itemId)
                            }
                        }
                    )
                }
            }

            waitUntilExists(hasText(loginItemTitle))
            onNode(hasText(loginItemTitle)).performClick()
            waitUntil { checker.isCalled }
        }

        val loginItem = items.first()
        assertEquals(loginItem.shareId to items[0].id, checker.memory)
    }

    @Test
    fun canNavigateToCreateVault() {
        val vault = VaultWithItemCount(
            vault = Vault(
                userId = UserId(""),
                shareId = ShareId("ShareId-canNavigateToCreateVault"),
                vaultId = VaultId("vaultId-canNavigateToCreateVault"),
                name = "Vault canNavigateToCreateVault",
                color = ShareColor.Color1,
                icon = ShareIcon.Icon1,
                createTime = Date()
            ),
            activeItemCount = 0,
            trashedItemCount = 0
        )
        observeVaultsWithItemCount.sendResult(Result.success(listOf(vault)))

        val checker = CallChecker<Unit>()
        composeTestRule.setContent {
            PassTheme(isDark = true) {
                HomeScreen(
                    onNavigateEvent = {
                        if (it is HomeNavigation.CreateVault) {
                            checker.call()
                        }
                    }
                )
            }
        }

        composeTestRule.onNodeWithTag(DRAWER_ICON_TEST_TAG).performClick()
        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.vault_drawer_create_vault))
            .performClick()
        composeTestRule.waitUntil { checker.isCalled }
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

    @Test
    fun showsConfirmationDialogBeforeTrashingAlias() {
        val title = "Some alias"
        val aliasItem = FakeObserveEncryptedItems.createAlias(title = title)
        setupWithItems(listOf(aliasItem))

        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    HomeScreen(
                        onNavigateEvent = {}
                    )
                }
            }

            waitUntilExists(hasText(title))

            val menuContentDescription =
                activity.getString(CompR.string.action_content_description_menu)
            onNodeWithContentDescription(menuContentDescription).performClick()

            val trashItemText = activity.getString(CompR.string.bottomsheet_move_to_trash)
            waitUntilExists(hasText(trashItemText))
            onNodeWithText(trashItemText).performClick()

            val confirmDialogText =
                activity.getString(TrashR.string.alias_dialog_move_to_trash_confirm)
            waitUntilExists(hasText(confirmDialogText))
            onNodeWithText(confirmDialogText).performClick()

            waitUntil { trashItem.getMemory().isNotEmpty() }

            onNodeWithText(confirmDialogText).assertDoesNotExist()
        }

        val memory = trashItem.getMemory()
        assertThat(memory.size).isEqualTo(1)

        val memoryItem = memory.first()
        assertThat(memoryItem.items[aliasItem.shareId]!!.first()).isEqualTo(aliasItem.id)
    }

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
                        onNavigateEvent = {
                            if (it is HomeNavigation.AddItem) {
                                checker.call(it.itemTypeUiState)
                            }
                        }
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

    private fun setupWithItems(items: List<ItemEncrypted> = FakeObserveEncryptedItems.defaultValues.asList()) {
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.EnabledByOurService))
        preferencesRepository.setHasCompletedOnBoarding(HasCompletedOnBoarding.Completed)
        preferencesRepository.setHasDismissedAutofillBanner(HasDismissedAutofillBanner.Dismissed)
        preferencesRepository.setUseFaviconsPreference(UseFaviconsPreference.Disabled)

        val vaults = items
            .map { it.shareId }
            .distinct()
            .map { shareId ->
                Vault(
                    userId = UserId(""),
                    shareId = shareId,
                    vaultId = VaultId("vaultId"),
                    name = "Vault ${shareId.id}",
                    color = ShareColor.Color1,
                    icon = ShareIcon.Icon1,
                    createTime = Date()
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

        itemSyncStatusRepository.tryEmit(ItemSyncStatus.SyncSuccess)
        observeVaults.sendResult(Result.success(vaults))
        observeEncryptedItems.emitValue(items)
        observeSearchEntry.emit(searchEntries)

    }

    @Module
    @InstallIn(SingletonComponent::class)
    object TestModule {
        @Provides
        fun provideClock(): Clock = Clock.System
    }

}
