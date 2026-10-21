/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.uitest.smoke

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import proton.android.pass.ui.MainActivity
import proton.android.pass.uitest.BaseTest.Companion.RuleOrder_00_First
import proton.android.pass.uitest.BaseTest.Companion.RuleOrder_10_Initialization
import proton.android.pass.uitest.BaseTest.Companion.RuleOrder_20_Injection
import proton.android.pass.uitest.BaseTest.Companion.RuleOrder_30_ActivityLaunch
import proton.android.pass.uitest.rule.HiltInjectRule
import proton.android.pass.uitest.rule.MainInitializerRule

@HiltAndroidTest
class AppLaunchSmokeTest {

    @get:Rule(order = RuleOrder_00_First)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = RuleOrder_10_Initialization)
    val mainInitializerRule = MainInitializerRule()

    @get:Rule(order = RuleOrder_20_Injection)
    val hiltInjectRule = HiltInjectRule(hiltRule)

    @get:Rule(order = RuleOrder_30_ActivityLaunch)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun mainActivityLaunchesAndComposes() {
        composeTestRule.waitForIdle()

        assertTrue(composeTestRule.activity.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
        composeTestRule.onAllNodes(isRoot()).assertCountEquals(1)
    }
}
