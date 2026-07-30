/*
 * Copyright (c) 2023-2026 Proton AG.
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
import org.junit.Assert.assertEquals
import org.junit.Test
import proton.android.pass.features.onboarding.OnBoardingPageName
import proton.android.pass.uitest.BaseTest
import proton.android.pass.payments.fakes.FakePayments
import proton.android.pass.uitest.robot.AccountRobot
import proton.android.pass.uitest.robot.HomeRobot
import proton.android.pass.uitest.robot.OnBoardingRobot
import proton.android.pass.uitest.robot.SubscriptionManagementRobot
import proton.android.pass.uitest.robot.UpsellRobot
import javax.inject.Inject

@HiltAndroidTest
class SubscriptionFlowTest : BaseTest() {

    @Inject
    lateinit var fakePayments: FakePayments

    @Test
    fun opensPaymentSubscriptionManagementFromAccount() {
        navigateToAccount()

        SubscriptionManagementRobot.screenDisplayed()
    }

    @Test
    fun startsPurchaseFromUpsell() {
        navigateToAccount(upgrade = true)

        UpsellRobot
            .annualPlansDisplayed()
            .clickUpgrade()

        composeTestRule.waitUntil { fakePayments.purchases.isNotEmpty() }
        assertEquals(
            listOf(FakePayments.Purchase(productId = "pass_plus_12", offerToken = "pass_plus_12-token")),
            fakePayments.purchases
        )
    }

    private fun navigateToAccount(upgrade: Boolean = false) {
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

        if (upgrade) {
            AccountRobot.clickUpgrade()
        } else {
            AccountRobot.clickSubscription()
        }
    }
}
