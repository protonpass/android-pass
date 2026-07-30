/*
 * Copyright (c) 2026 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package proton.android.pass.features.upsell.v2.navigation

import androidx.navigation.NavGraphBuilder
import proton.android.pass.navigation.api.NavItem

object UpsellV2NavItem : NavItem(baseRoute = "upsellv2") {
    fun createRoute(displayOnBoardingAfter: Boolean = false, manualDisplay: Boolean = false): String = baseRoute
}

fun NavGraphBuilder.upsellV2NavGraph(
    onNextScreen: (Boolean) -> Unit,
    onSkip: (Boolean) -> Unit,
    onNavigateBack: (Boolean) -> Unit
) {
    onNextScreen.hashCode()
}
