/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.sharing.accept

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.common.api.some
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.fakes.FakeSavedStateHandleProvider
import proton.android.pass.data.fakes.usecases.FakeGetItemById
import proton.android.pass.data.fakes.usecases.FakeAcceptInvite
import proton.android.pass.data.fakes.usecases.FakeRejectInvite
import proton.android.pass.data.fakes.usecases.invites.FakeObserveInvite
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.features.sharing.R
import proton.android.pass.features.sharing.SharingNavigation
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.test.CallChecker
import proton.android.pass.test.HiltComponentActivity
import proton.android.pass.test.domain.ItemTestFactory
import proton.android.pass.test.domain.PendingInviteTestFactory
import javax.inject.Inject

@HiltAndroidTest
class AcceptBottomSheetTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()

    @Inject
    lateinit var observeInvite: FakeObserveInvite

    @Inject
    lateinit var acceptInvite: FakeAcceptInvite

    @Inject
    lateinit var rejectInvite: FakeRejectInvite

    @Inject
    lateinit var getItemById: FakeGetItemById

    @Inject
    lateinit var savedStateHandle: FakeSavedStateHandleProvider

    @Before
    fun setup() {
        hiltRule.inject()

        savedStateHandle.get().apply {
            set(CommonNavArgId.InviteToken.key, INVITE_TOKEN)
        }
    }

    @Test
    fun displaysVaultInvite() {
        observeInvite.emit(vaultInvite.some())

        composeTestRule.apply {
            val expectedTitle = activity.getString(R.string.sharing_vault_invitation_title)
            val expectedSubtitle = activity.getString(
                R.string.sharing_vault_invitation_subtitle,
                vaultInvite.inviterEmail
            )
            val expectedVaultName = vaultInvite.name
            val expectedAcceptButtonText = activity.getString(R.string.sharing_vault_invitation_accept)
            val expectedRejectButtonText = activity.getString(R.string.sharing_reject_invitation)

            setContent {
                PassTheme {
                    AcceptInviteBottomSheet(
                        onNavigateEvent = {}
                    )
                }
            }

            onNodeWithText(expectedTitle).assertExists()
            onNodeWithText(expectedSubtitle).assertExists()
            onNodeWithText(expectedVaultName).assertExists()
            onNodeWithText(expectedAcceptButtonText).assertExists()
            onNodeWithText(expectedRejectButtonText).assertExists()
        }
    }

    @Test
    fun acceptsVaultInvite() {
        observeInvite.emit(vaultInvite.some())

        composeTestRule.apply {
            val checker = CallChecker<Unit>()
            val acceptButtonText = activity.getString(R.string.sharing_vault_invitation_accept)
            val expectedNavigation = SharingNavigation.ManageSharedVault(
                sharedVaultId = ShareId(FakeAcceptInvite.DEFAULT_SHARE_ID)
            )

            setContent {
                PassTheme {
                    AcceptInviteBottomSheet(
                        onNavigateEvent = { sharingNavigation ->
                            if (sharingNavigation == expectedNavigation) {
                                checker.call()
                            }
                        }
                    )
                }
            }

            onNodeWithText(acceptButtonText).performClick()
            waitUntil { checker.isCalled }
        }
    }

    @Test
    fun rejectsVaultInvite() {
        observeInvite.emit(vaultInvite.some())

        composeTestRule.apply {
            val checker = CallChecker<Unit>()
            val rejectButtonText = activity.getString(R.string.sharing_reject_invitation)
            val expectedNavigation = SharingNavigation.BackToHome

            setContent {
                PassTheme {
                    AcceptInviteBottomSheet(
                        onNavigateEvent = { sharingNavigation ->
                            if (sharingNavigation == expectedNavigation) {
                                checker.call()
                            }
                        }
                    )
                }
            }

            onNodeWithText(rejectButtonText).performClick()
            waitUntil { checker.isCalled }
        }
    }

    @Test
    fun displaysItemInvite() {
        observeInvite.emit(itemInvite.some())

        composeTestRule.apply {
            val expectedTitle = activity.getString(R.string.sharing_item_invitation_title)
            val expectedSubtitle = activity.getString(
                R.string.sharing_item_invitation_subtitle,
                itemInvite.inviterEmail
            )
            val expectedAcceptButtonText = activity.getString(R.string.sharing_item_invitation_accept)
            val expectedRejectButtonText = activity.getString(R.string.sharing_reject_invitation)

            setContent {
                PassTheme {
                    AcceptInviteBottomSheet(
                        onNavigateEvent = {}
                    )
                }
            }

            onNodeWithText(expectedTitle).assertExists()
            onNodeWithText(expectedSubtitle).assertExists()
            onNodeWithText(expectedAcceptButtonText).assertExists()
            onNodeWithText(expectedRejectButtonText).assertExists()
        }
    }

    @Test
    fun acceptsItemInvite() {
        val item = ItemTestFactory.create(
            shareId = ShareId(FakeAcceptInvite.DEFAULT_SHARE_ID),
            itemId = ItemId(FakeAcceptInvite.DEFAULT_ITEM_ID),
            itemType = ItemType.Note(text = "Test note", customFields = emptyList())
        )
        observeInvite.emit(itemInvite.some())
        getItemById.emit(Result.success(item))

        composeTestRule.apply {
            val checker = CallChecker<Unit>()
            val acceptButtonText = activity.getString(R.string.sharing_item_invitation_accept)
            val expectedNavigation = SharingNavigation.SharedItemDetails(
                shareId = ShareId(FakeAcceptInvite.DEFAULT_SHARE_ID),
                itemId = ItemId(FakeAcceptInvite.DEFAULT_ITEM_ID),
                itemCategory = ItemCategory.Note
            )

            setContent {
                PassTheme {
                    AcceptInviteBottomSheet(
                        onNavigateEvent = { sharingNavigation ->
                            if (sharingNavigation == expectedNavigation) {
                                checker.call()
                            }
                        }
                    )
                }
            }

            onNodeWithText(acceptButtonText).performClick()
            waitUntil { checker.isCalled }
        }
    }

    @Test
    fun rejectsItemInvite() {
        observeInvite.emit(itemInvite.some())

        composeTestRule.apply {
            val checker = CallChecker<Unit>()
            val rejectButtonText = activity.getString(R.string.sharing_reject_invitation)
            val expectedNavigation = SharingNavigation.BackToHome

            setContent {
                PassTheme {
                    AcceptInviteBottomSheet(
                        onNavigateEvent = { sharingNavigation ->
                            if (sharingNavigation == expectedNavigation) {
                                checker.call()
                            }
                        }
                    )
                }
            }

            onNodeWithText(rejectButtonText).performClick()
            waitUntil { checker.isCalled }
        }
    }

    private companion object {

        private const val INVITE_TOKEN = "AcceptBottomSheetTest.INVITE_TOKEN"

        private const val INVITE_NAME = "AcceptBottomSheetTest.INVITE_NAME"

        private val vaultInvite = PendingInviteTestFactory.Vault.create(
            token = INVITE_TOKEN,
            name = INVITE_NAME
        )

        private val itemInvite = PendingInviteTestFactory.Item.create(
            inviteToken = INVITE_TOKEN,
            inviterEmail = INVITE_NAME
        )
    }

}
