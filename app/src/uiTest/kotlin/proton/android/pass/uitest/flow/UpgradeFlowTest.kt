/*
 * Copyright (c) 2023 Proton AG.
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
import me.proton.core.plan.test.BillingPlan
import me.proton.core.plan.test.MinimalUpgradeTests
import me.proton.core.plan.test.robot.SubscriptionRobot
import me.proton.core.test.rule.ProtonRule
import me.proton.core.test.rule.extension.protonAndroidComposeRule
import org.junit.Rule
import proton.android.pass.features.onboarding.OnBoardingPageName
import proton.android.pass.ui.MainActivity
import proton.android.pass.uitest.robot.HomeRobot
import proton.android.pass.uitest.robot.OnBoardingRobot

@HiltAndroidTest
open class UpgradeFlowTest : MinimalUpgradeTests {

    // TODO: rework and fix account tests - CP-8721.

    @get:Rule
    val protonRule: ProtonRule = protonAndroidComposeRule<MainActivity>(
        logoutBefore = true
    )

    override fun startUpgrade(): SubscriptionRobot {

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
            .clickUpgrade()

        return SubscriptionRobot
    }

    override fun providePlans(): List<BillingPlan> = emptyList()
}
