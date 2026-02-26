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

package proton.android.pass.uitest.flow

import dagger.hilt.android.testing.HiltAndroidTest
import me.proton.core.accountmanager.test.robot.AccountSettingsRobot
import me.proton.core.test.rule.ProtonRule
import me.proton.core.test.rule.extension.protonAndroidComposeRule
import me.proton.core.usersettings.test.MinimalUserSettingsTest
import org.junit.Rule
import proton.android.pass.features.onboarding.OnBoardingPageName
import proton.android.pass.ui.MainActivity
import proton.android.pass.uitest.robot.HomeRobot
import proton.android.pass.uitest.robot.OnBoardingRobot

@HiltAndroidTest
open class AccountSettingsFlowTest : MinimalUserSettingsTest {

    // TODO: rework and fix account tests - CP-8721.

    @get:Rule
    override val protonRule: ProtonRule = protonAndroidComposeRule<MainActivity>(
        logoutBefore = true
    )

    private fun startAccountSettings(): AccountSettingsRobot {
        OnBoardingRobot.onBoardingScreenDisplayed()
            .clickSkip(OnBoardingPageName.Autofill)
            .clickMain(OnBoardingPageName.Last)
        return HomeRobot
            .clickProfile()
            .clickAccount()
            .coreAccountSettings()
    }

    override fun startPasswordManagement() {
        startAccountSettings().clickPasswordManagement()
    }

    override fun startRecoveryEmail() {
        startAccountSettings().clickRecoveryEmail()
    }
}
