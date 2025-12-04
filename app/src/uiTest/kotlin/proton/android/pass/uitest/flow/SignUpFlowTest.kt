/*
 * Copyright (c) 2023 Proton AG.
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

import dagger.hilt.android.testing.HiltAndroidTest
import me.proton.core.auth.test.MinimalSignUpExternalTests
import me.proton.core.test.rule.ProtonRule
import me.proton.core.test.rule.extension.protonAndroidComposeRule
import org.junit.Rule
import proton.android.pass.ui.MainActivity
import proton.android.pass.uitest.robot.OnBoardingRobot
import proton.android.pass.uitest.robot.Robot

@HiltAndroidTest
open class SignUpFlowTest : MinimalSignUpExternalTests {

    // TODO: rework and fix account tests - CP-8721.

    @get:Rule
    val protonRule: ProtonRule = protonAndroidComposeRule<MainActivity>(
        logoutBefore = true
    )

    private inline fun <T : Robot> T.verify(crossinline block: T.() -> Any): T = apply { block() }

    val isCongratsDisplayed = true

    fun verifyAfter() {
        OnBoardingRobot.verify { onBoardingScreenDisplayed() }
    }
}
