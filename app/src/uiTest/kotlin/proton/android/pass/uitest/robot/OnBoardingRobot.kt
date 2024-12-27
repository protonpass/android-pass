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

package proton.android.pass.uitest.robot

import me.proton.test.fusion.Fusion.node
import proton.android.pass.features.onboarding.OnBoardingPageName
import proton.android.pass.features.onboarding.OnBoardingPageTestTag
import proton.android.pass.features.onboarding.OnBoardingScreenTestTag

object OnBoardingRobot : Robot {

    private val onBoardingScreen get() = node.withTag(OnBoardingScreenTestTag.screen)
    private val mainButton get() = node.withTag(OnBoardingPageTestTag.mainButton)
    private val skipButton get() = node.withTag(OnBoardingPageTestTag.skipButton)

    fun onBoardingScreenDisplayed(): OnBoardingRobot = apply {
        onBoardingScreen.await { assertIsDisplayed() }
    }

    fun clickMain(page: OnBoardingPageName): OnBoardingRobot = apply {
        mainButton.hasAncestor(node.withTag(page.name)).apply {
            await { assertIsDisplayed() }
            click()
        }
    }

    fun clickSkip(page: OnBoardingPageName): OnBoardingRobot = apply {
        skipButton.hasAncestor(node.withTag(page.name)).apply {
            await { assertIsDisplayed() }
            click()
        }
    }
}
