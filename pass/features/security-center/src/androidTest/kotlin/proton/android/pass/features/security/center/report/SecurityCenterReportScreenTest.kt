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

package proton.android.pass.features.security.center.report

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.fakes.TestSavedStateHandleProvider
import proton.android.pass.data.fakes.usecases.breach.FakeObserveBreachesForEmail
import proton.android.pass.data.fakes.usecases.vaults.FakeObserveVaultsGroupedByShareId
import proton.android.pass.features.security.center.R
import proton.android.pass.features.security.center.report.navigation.SecurityCenterReportDestination
import proton.android.pass.features.security.center.report.ui.SecurityCenterReportScreen
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavParamEncoder
import proton.android.pass.test.CallChecker
import proton.android.pass.test.HiltComponentActivity
import javax.inject.Inject
import proton.android.pass.composecomponents.impl.R as ComposeR

@HiltAndroidTest
class SecurityCenterReportScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()

    @Inject
    lateinit var savedStateHandle: TestSavedStateHandleProvider

    @Inject
    lateinit var observeBreachesForEmail: FakeObserveBreachesForEmail

    @Inject
    lateinit var observeVaultsGroupedByShareId: FakeObserveVaultsGroupedByShareId

    @Before
    fun setup() {
        hiltRule.inject()
        savedStateHandle.get().apply {
            set(CommonNavArgId.Email.key, NavParamEncoder.encode("email"))
            set(CommonNavArgId.AddressId.key, "addressId")
        }
        observeBreachesForEmail.emitDefault()
        observeVaultsGroupedByShareId.emitDefault()
    }

    @Test
    fun onNavigateBackCalled() {
        val checker = CallChecker<Unit>()
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    SecurityCenterReportScreen(
                        onNavigated = {
                            if (it is SecurityCenterReportDestination.Back) {
                                checker.call()
                            }
                        }
                    )
                }
            }

            val backArrow =
                composeTestRule.activity.getString(ComposeR.string.navigate_back_icon_content_description)
            onNodeWithContentDescription(backArrow).performClick()

            waitUntil { checker.isCalled }
        }
    }

    @Test
    fun onOptionsClickCalled() {
        val checker = CallChecker<Unit>()
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    SecurityCenterReportScreen(
                        onNavigated = {
                            if (it is SecurityCenterReportDestination.OnMenuClick) {
                                checker.call()
                            }
                        }
                    )
                }
            }
            val menu =
                composeTestRule.activity.getString(R.string.security_center_email_report_options_menu)
            onNodeWithContentDescription(menu).performClick()

            waitUntil { checker.isCalled }
        }
    }
}
