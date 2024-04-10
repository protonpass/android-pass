/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.features.security.center.shared.navigation

import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.breach.BreachCustomEmailId
import proton.android.pass.domain.features.PaidFeature

sealed interface SecurityCenterNavDestination {

    @JvmInline
    value class Back(val comesFromBottomSheet: Boolean = false) : SecurityCenterNavDestination

    data object Empty : SecurityCenterNavDestination

    data object Home : SecurityCenterNavDestination

    data class ItemDetails(val shareId: ShareId, val itemId: ItemId) : SecurityCenterNavDestination

    data object MainHome : SecurityCenterNavDestination

    data object MainNewItem : SecurityCenterNavDestination

    data object MainProfile : SecurityCenterNavDestination

    data object DarkWebMonitoring : SecurityCenterNavDestination

    data object ReusedPasswords : SecurityCenterNavDestination

    data object WeakPasswords : SecurityCenterNavDestination

    data object MissingTFA : SecurityCenterNavDestination

    data object Sentinel : SecurityCenterNavDestination

    data object DarkWebMonitor : SecurityCenterNavDestination

    @JvmInline
    value class Upsell(val paidFeature: PaidFeature) : SecurityCenterNavDestination

    data object AddCustomEmail : SecurityCenterNavDestination

    data class VerifyEmail(val id: BreachCustomEmailId, val email: String) : SecurityCenterNavDestination

    data object EmailVerified : SecurityCenterNavDestination
}
