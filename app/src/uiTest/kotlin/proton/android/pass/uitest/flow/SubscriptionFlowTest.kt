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
import me.proton.core.auth.test.robot.AddAccountRobot
import me.proton.core.plan.test.MinimalSubscriptionTests
import me.proton.core.test.quark.Quark
import me.proton.core.test.quark.data.User
import proton.android.pass.features.onboarding.OnBoardingPageName
import proton.android.pass.uitest.BaseTest
import proton.android.pass.uitest.robot.HomeRobot
import proton.android.pass.uitest.robot.OnBoardingRobot

@HiltAndroidTest
class SubscriptionFlowTest : BaseTest(), MinimalSubscriptionTests {

    override val quark: Quark = BaseTest.quark
    override val users: User.Users = BaseTest.users

    override fun startSubscription(user: User) {
        AddAccountRobot
            .clickSignIn()
            .login(user)

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
            .clickSubscription()
    }
}
