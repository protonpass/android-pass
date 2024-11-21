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
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.data.fakes.usecases.TestAcceptInvite
import proton.android.pass.data.fakes.usecases.TestObserveInvites
import proton.android.pass.data.fakes.usecases.TestRejectInvite
import proton.android.pass.features.sharing.R
import proton.android.pass.features.sharing.SharingNavigation
import proton.android.pass.test.CallChecker
import proton.android.pass.test.HiltComponentActivity
import proton.android.pass.test.domain.TestPendingInvite
import proton.android.pass.domain.InviteToken
import javax.inject.Inject

@HiltAndroidTest
class AcceptBottomSheetTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()

    @Inject
    lateinit var observeInvites: TestObserveInvites

    @Inject
    lateinit var acceptInvite: TestAcceptInvite

    @Inject
    lateinit var rejectInvite: TestRejectInvite

    @Before
    fun setup() {
        hiltRule.inject()
        observeInvites.emitInvites(listOf(TEST_INVITE))
    }

    @Test
    fun showsTheRightData() {
        composeTestRule.apply {
            setContent {
                PassTheme {
                    AcceptInviteBottomSheet(
                        onNavigateEvent = {}
                    )
                }
            }

            onNodeWithText(TEST_INVITE.name).assertExists()
            onNodeWithText(TEST_INVITE.inviterEmail, substring = true).assertExists()
        }
    }

    @Test
    fun acceptCallsAcceptAndCloses() {
        val checker = CallChecker<Unit>()
        composeTestRule.apply {
            setContent {
                PassTheme {
                    AcceptInviteBottomSheet(
                        onNavigateEvent = {
                            if (it == SharingNavigation.BackToHome) {
                                checker.call()
                            }
                        }
                    )
                }
            }

            val acceptText = activity.getString(R.string.sharing_join_shared_vault)
            onNodeWithText(acceptText).performClick()
            waitUntil { checker.isCalled }
        }

        val memory = acceptInvite.getMemory()
        assertThat(memory).isEqualTo(listOf(InviteToken(INVITE_TOKEN)))
    }

    @Test
    fun rejectCallsRejectAndCloses() {
        val checker = CallChecker<Unit>()
        composeTestRule.apply {
            setContent {
                PassTheme {
                    AcceptInviteBottomSheet(
                        onNavigateEvent = {
                            if (it == SharingNavigation.BackToHome) {
                                checker.call()
                            }
                        }
                    )
                }
            }

            val rejectText = activity.getString(R.string.sharing_reject_invitation)
            onNodeWithText(rejectText).performClick()
            waitUntil { checker.isCalled }
        }

        val memory = rejectInvite.getMemory()
        assertThat(memory).isEqualTo(listOf(InviteToken(INVITE_TOKEN)))
    }


    companion object {
        private const val INVITE_TOKEN = "AcceptBottomSheetTest.INVITE_TOKEN"
        private const val INVITE_NAME = "AcceptBottomSheetTest.INVITE_NAME"
        private val TEST_INVITE = TestPendingInvite.Vault.create(
            token = INVITE_TOKEN,
            name = INVITE_NAME
        )
    }
}
