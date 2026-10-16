/*
 * Copyright (c) 2026 Proton AG.
 * This file is part of Proton Pass.
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

package proton.android.pass.uitest.robot

import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.performClick
import me.proton.test.fusion.Fusion.node
import proton.android.pass.composecomponents.impl.bottombar.BottomBarTestTag
import proton.android.pass.features.home.HomeContentTestTag
import proton.android.pass.features.home.HomeScreenTestTag

object HomeRobot : Robot {

    private val homeScreen get() = node.withTag(HomeScreenTestTag.SCREEN)

    private val profile get() = node.withTag(BottomBarTestTag.PROFILE_TEST_TAG)

    fun homeScreenDisplayed(): HomeRobot = apply {
        homeScreen.await { assertIsDisplayed() }
    }

    fun openDrawer(timeoutMillis: Long = TIMEOUT_MILLIS): DrawerRobot {
        val drawerIcon = hasTestTag(HomeContentTestTag.DRAWER_ICON_TEST_TAG)
        composeRule.waitUntil(timeoutMillis) { runCatching { exists(drawerIcon) }.getOrDefault(false) }
        composeRule.onNode(drawerIcon, useUnmergedTree = true).performClick()
        return DrawerRobot
    }

    fun clickProfile(): ProfileRobot {
        profile.await { assertIsDisplayed() }
        profile.click()
        return ProfileRobot
    }
}
