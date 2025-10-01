/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.onboarding

import androidx.activity.compose.BackHandler
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.OptionalNavArgId
import proton.android.pass.navigation.api.composable
import proton.android.pass.navigation.api.toPath

object ComeFromUpsellArg : OptionalNavArgId {
    override val key = "comeFromUpsell"
    override val navType = NavType.BoolType
}

object OnBoarding : NavItem(
    baseRoute = "onboarding",
    isTopLevel = false,
    optionalArgIds = listOf(ComeFromUpsellArg)
) {
    fun createRoute(comeFromUpsell: Boolean = false): String = buildString {
        append(baseRoute)
        val params = mapOf(
            ComeFromUpsellArg.key to comeFromUpsell
        )
        append(params.toPath())
    }
}

fun NavGraphBuilder.onBoardingGraph(onOnBoardingFinished: () -> Unit, onNavigateBack: (Boolean) -> Unit) {
    composable(OnBoarding) {
        val comFromUpsell = it.arguments?.getBoolean(ComeFromUpsellArg.key) ?: false
        BackHandler { onNavigateBack(comFromUpsell) }
        OnBoardingScreen(
            onBoardingShown = onOnBoardingFinished
        )
    }
}
