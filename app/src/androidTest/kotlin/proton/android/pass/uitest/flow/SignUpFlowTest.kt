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

package proton.android.pass.uitest.flow

import androidx.test.filters.SdkSuppress
import dagger.hilt.android.testing.HiltAndroidTest
import me.proton.core.auth.test.MinimalSignUpExternalTests
import me.proton.core.auth.test.robot.signup.CongratsRobot
import me.proton.core.auth.test.robot.signup.SetPasswordRobot
import me.proton.core.auth.test.robot.signup.SignupExternal
import me.proton.core.auth.test.rule.AcceptExternalRule
import me.proton.core.humanverification.test.robot.HvCodeRobot
import me.proton.core.network.domain.client.ExtraHeaderProvider
import me.proton.core.test.rule.ProtonRule
import me.proton.core.test.rule.extension.protonAndroidComposeRule
import me.proton.core.util.kotlin.random
import org.junit.Rule
import org.junit.Test
import proton.android.pass.initializer.MainInitializer
import proton.android.pass.ui.MainActivity
import proton.android.pass.uitest.robot.OnBoardingRobot
import proton.android.pass.uitest.robot.Robot
import javax.inject.Inject

@SdkSuppress(minSdkVersion = 29)
@HiltAndroidTest
open class SignUpFlowTest : MinimalSignUpExternalTests {

    @Inject
    lateinit var extraHeaderProvider: ExtraHeaderProvider

    @get:Rule
    val protonRule: ProtonRule = protonAndroidComposeRule<MainActivity>(
        logoutBefore = true,
        additionalRules = linkedSetOf(AcceptExternalRule { extraHeaderProvider }),
        afterHilt = { MainInitializer.init(it.targetContext) }
    )

    val isCongratsDisplayed = true

    @Test
    override fun signupExternalAccountHappyPath() {
        val testEmail = "${String.random()}@gmail.com"

        SignupExternal
            .fillUsername(testEmail)
            .clickNext()
        HvCodeRobot
            .fillCode()
            .clickVerify()

        SetPasswordRobot
            .fillAndClickNext(String.random(12))

        CongratsRobot.uiElementsDisplayed()
    }

    @Test
    override fun signupSwitchToInternalAccountCreationHappyPath() {
        val username = "test-${String.random(15)}"

        SignupExternal
            .clickSwitch()
            .apply {
                robotDisplayed()
            }
            .fillEmail(username)
            .clickNext()
            .fillAndClickNext(String.random(12))
            .skip()
            .skipConfirm()
        HvCodeRobot
            .fillCodeForInternal(username)
            .clickVerify()

        CongratsRobot.uiElementsDisplayed()
    }

    private inline fun <T : Robot> T.verify(crossinline block: T.() -> Any): T = apply { block() }

    fun verifyAfter() {
        OnBoardingRobot.verify { onBoardingScreenDisplayed() }
    }
}
