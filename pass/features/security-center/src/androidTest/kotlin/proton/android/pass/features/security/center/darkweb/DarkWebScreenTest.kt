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

package proton.android.pass.features.security.center.darkweb

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.espresso.intent.rule.IntentsRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.data.fakes.usecases.FakeObserveItems
import proton.android.pass.features.security.center.R
import proton.android.pass.features.security.center.darkweb.navigation.DarkWebMonitorNavDestination
import proton.android.pass.features.security.center.darkweb.ui.DarkWebScreen
import proton.android.pass.test.CallChecker
import proton.android.pass.test.HiltComponentActivity
import javax.inject.Inject

@HiltAndroidTest
class DarkWebScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()

    @get:Rule(order = 2)
    val intentsRule = IntentsRule()

    @Inject
    lateinit var observeItems: FakeObserveItems

    @Before
    fun setup() {
        hiltRule.inject()
        observeItems.emitValue(FakeObserveItems.defaultValues.asList())
    }

    @Test
    fun onAddCustomEmailClickCalled() {
        val checker = CallChecker<Unit>()
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    DarkWebScreen(
                        onNavigate = {
                            if (it is DarkWebMonitorNavDestination.AddEmail) {
                                checker.call()
                            }
                        }
                    )
                }
            }
            val desc =
                activity.getString(R.string.security_center_dark_web_monitor_custom_emails_add_content_description)
            onNodeWithContentDescription(desc).performClick()

            waitUntil { checker.isCalled }
        }
    }

}
