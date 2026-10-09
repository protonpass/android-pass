/*
 * Copyright (c) 2026 Proton AG.
 * This file is part of Proton Drive.
 *
 * Proton Drive is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Drive is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Drive.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.uitest.flow

import dagger.hilt.android.testing.HiltAndroidTest
import me.proton.core.test.rule.ProtonRule
import me.proton.core.test.rule.annotation.PrepareUser
import me.proton.core.test.rule.extension.protonAndroidComposeRule
import org.junit.Rule
import org.junit.Test
import proton.android.pass.features.onboarding.OnBoardingPageName
import proton.android.pass.initializer.MainInitializer
import proton.android.pass.ui.MainActivity
import proton.android.pass.uitest.robot.AccountRobot
import proton.android.pass.uitest.robot.HomeRobot
import proton.android.pass.uitest.robot.OnBoardingRobot
import proton.android.pass.uitest.robot.SubscriptionManagementRobot

@HiltAndroidTest
class SubscriptionFlowTest {

    @get:Rule
    val protonRule: ProtonRule = protonAndroidComposeRule<MainActivity>(
        logoutBefore = true,
        fusionEnabled = true,
        afterHilt = { MainInitializer.init(it.targetContext) }
    )

    @Test
    @PrepareUser(loginBefore = true)
    fun opensPaymentSubscriptionManagementFromAccount() {
        navigateToAccount()

        SubscriptionManagementRobot.screenDisplayed()
    }

    private fun navigateToAccount() {
        OnBoardingRobot
            .onBoardingScreenDisplayed()
            .clickSkip(OnBoardingPageName.Autofill)
            .clickMain(OnBoardingPageName.Last)

        HomeRobot
            .homeScreenDisplayed()
            .clickProfile()
            .profileScreenDisplayed()
            .clickAccount()
            .accountScreenDisplayed()

        AccountRobot.clickSubscription()
    }
}
