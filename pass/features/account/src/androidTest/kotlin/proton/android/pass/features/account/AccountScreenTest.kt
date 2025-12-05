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

package proton.android.pass.features.account

import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.espresso.intent.rule.IntentsRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.commonui.api.BrowserUtils
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.data.fakes.usecases.FakeObserveCurrentUser
import proton.android.pass.data.fakes.usecases.FakeObserveUpgradeInfo
import proton.android.pass.test.CallChecker
import proton.android.pass.test.HiltComponentActivity
import proton.android.pass.test.domain.UserTestFactory
import proton.android.pass.test.waitUntilExists
import javax.inject.Inject
import proton.android.pass.composecomponents.impl.R as CompR

@HiltAndroidTest
class AccountScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()

    @get:Rule(order = 2)
    val intentsRule = IntentsRule()

    @Inject
    lateinit var observeUpgradeInfo: FakeObserveUpgradeInfo

    @Inject
    lateinit var observeCurrentUser: FakeObserveCurrentUser

    @Before
    fun setup() {
        hiltRule.inject()
        observeCurrentUser.sendUser(UserTestFactory.create(email = "test@test.test", name = "test user"))
        BrowserUtils.resetLastUrl()
    }

    @Test
    fun accountScreenOnSignOutIsCalled() {
        val checker = CallChecker<Unit>()
        composeTestRule.apply {
            setContent {
                PassTheme {
                    AccountScreen(
                        onNavigate = { if (it is AccountNavigation.SignOut) checker.call() }
                    )
                }
            }

            val contentDescription = activity.getString(
                R.string.account_sign_out_icon_content_description
            )
            onNodeWithContentDescription(contentDescription)
                .performScrollTo()
                .performClick()
            waitUntil { checker.isCalled }
        }

    }

    @Test
    fun accountScreenOnBackIsCalled() {
        val checker = CallChecker<Unit>()
        composeTestRule.apply {
            setContent {
                PassTheme {
                    AccountScreen(
                        onNavigate = { if (it is AccountNavigation.CloseScreen) checker.call() }
                    )
                }
            }
            val contentDescription = activity.getString(
                CompR.string.navigate_back_icon_content_description
            )
            onNodeWithContentDescription(contentDescription).performClick()

            waitUntil { checker.isCalled }
        }


    }

    @Test
    fun accountScreenOnDeleteOpensWebsite() {
        composeTestRule.apply {
            setContent {
                PassTheme {
                    AccountScreen(
                        onNavigate = {}
                    )
                }
            }

            val contentDescription = activity.getString(
                R.string.account_delete_account_icon_content_description
            )
            
            // Verify the delete account button exists and is clickable
            onNodeWithContentDescription(contentDescription)
                .performScrollTo()
                .assertExists()
                .performClick()

            // Verify that BrowserUtils.openWebsite was called with the correct URL
            assert(BrowserUtils.wasCalled) { "BrowserUtils.openWebsite should have been called" }
            assert(BrowserUtils.lastAttemptedUrl == "https://account.proton.me/u/0/pass/account-password") { 
                "Expected URL 'https://account.proton.me/u/0/pass/account-password', but got '${BrowserUtils.lastAttemptedUrl}'"
            }
        }
    }

    @Test
    fun accountScreenOnManageSubscription() {
        val updated = FakeObserveUpgradeInfo.DEFAULT.copy(
            isUpgradeAvailable = true,
            isSubscriptionAvailable = true
        )
        observeUpgradeInfo.setResult(updated)


        val checker = CallChecker<Unit>()
        composeTestRule.apply {
            setContent {
                PassTheme {
                    AccountScreen(
                        onNavigate = { if (it is AccountNavigation.Subscription) checker.call() }
                    )
                }
            }

            val contentDescription = activity.getString(
                R.string.manage_subscription_icon_content_description
            )
            waitUntilExists(hasContentDescription(contentDescription))
            onNodeWithContentDescription(contentDescription).performClick()

            waitUntil { checker.isCalled }
        }
    }

    @Test
    fun accountScreenDoesNotShowManageSubscriptionIfSubscriptionNotAvailable() {
        val updated = FakeObserveUpgradeInfo.DEFAULT.copy(
            isUpgradeAvailable = true,
            isSubscriptionAvailable = false
        )
        observeUpgradeInfo.setResult(updated)

        composeTestRule.apply {
            setContent {
                PassTheme {
                    AccountScreen(
                        onNavigate = {}
                    )
                }
            }

            val contentDescription = activity.getString(
                R.string.manage_subscription_icon_content_description
            )
            onNode(hasContentDescription(contentDescription)).assertDoesNotExist()
        }
    }
}
