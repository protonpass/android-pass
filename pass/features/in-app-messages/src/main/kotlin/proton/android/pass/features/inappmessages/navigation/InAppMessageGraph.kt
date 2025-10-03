/*
 * Copyright (c) 2024-2025 Proton AG
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

package proton.android.pass.features.inappmessages.navigation

import androidx.navigation.NavGraphBuilder
import proton.android.pass.features.inappmessages.bottomsheet.navigation.InAppMessageModalDestination
import proton.android.pass.features.inappmessages.bottomsheet.navigation.InAppMessageModalNavItem
import proton.android.pass.features.inappmessages.bottomsheet.ui.InAppMessageBottomsheet
import proton.android.pass.features.inappmessages.promo.navigation.InAppMessagePromoNavItem
import proton.android.pass.features.inappmessages.promo.ui.InAppMessagePromoScreen
import proton.android.pass.navigation.api.bottomSheet
import proton.android.pass.navigation.api.composable

fun NavGraphBuilder.inAppMessageGraph(onNavigate: (InAppMessageModalDestination) -> Unit) {

    bottomSheet(navItem = InAppMessageModalNavItem) {
        InAppMessageBottomsheet(onNavigate = onNavigate)
    }
    composable(navItem = InAppMessagePromoNavItem) {
        InAppMessagePromoScreen(onNavigate = onNavigate)
    }
}

