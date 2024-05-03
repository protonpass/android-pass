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

package proton.android.pass.features.security.center.aliaslist

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.MonitorState
import proton.android.pass.data.api.usecases.items.ItemIsBreachedFilter
import proton.android.pass.data.api.usecases.items.ItemSecurityCheckFilter
import proton.android.pass.data.fakes.usecases.TestObserveItems
import proton.android.pass.data.fakes.usecases.breach.FakeObserveGlobalMonitorState
import proton.android.pass.features.security.center.R
import proton.android.pass.features.security.center.aliaslist.navigation.SecurityCenterAliasListNavDestination
import proton.android.pass.features.security.center.aliaslist.ui.SecurityCenterAliasListScreen
import proton.android.pass.test.CallChecker
import proton.android.pass.test.HiltComponentActivity
import javax.inject.Inject
import proton.android.pass.composecomponents.impl.R as ComposeR

@HiltAndroidTest
class SecurityCenterAliasListScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()

    @Inject
    lateinit var observeItems: TestObserveItems

    @Inject
    lateinit var observeGlobalMonitorState: FakeObserveGlobalMonitorState

    @Before
    fun setup() {
        hiltRule.inject()
        observeGlobalMonitorState.emit(
            MonitorState(
                protonMonitorEnabled = true,
                aliasMonitorEnabled = true
            )
        )
    }

    @Test
    fun onNavigateBackCalled() {
        val checker = CallChecker<Unit>()
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    SecurityCenterAliasListScreen(
                        onNavigated = {
                            if (it is SecurityCenterAliasListNavDestination.Back) {
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
                    SecurityCenterAliasListScreen(
                        onNavigated = {
                            if (it is SecurityCenterAliasListNavDestination.OnOptionsClick) {
                                checker.call()
                            }
                        }
                    )
                }
            }
            val menu =
                composeTestRule.activity.getString(R.string.security_center_alias_list_options_menu)
            onNodeWithContentDescription(menu).performClick()

            waitUntil { checker.isCalled }
        }
    }

    @Test
    fun onEmailClickCalled() {
        val checker = CallChecker<Unit>()
        val aliasEmail = "aliasEmail"
        observeItems.emit(
            params = TestObserveItems.Params(
                filter = ItemTypeFilter.Aliases,
                securityCheckFilter = ItemSecurityCheckFilter.Included,
                isBreachedFilter = ItemIsBreachedFilter.NotBreached
            ),
            value = listOf(TestObserveItems.createAlias(alias = aliasEmail))
        )
        observeItems.emitValue(emptyList())
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    SecurityCenterAliasListScreen(
                        onNavigated = {
                            if (it is SecurityCenterAliasListNavDestination.OnEmailClick) {
                                checker.call()
                            }
                        }
                    )
                }
            }

            onNodeWithText(aliasEmail).performClick()
            waitUntil { checker.isCalled }
        }
    }
}
