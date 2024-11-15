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

import androidx.annotation.StringRes
import proton.android.pass.common.api.Option
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.breach.BreachEmailId
import proton.android.pass.domain.breach.CustomEmailId
import proton.android.pass.domain.features.PaidFeature
import proton.android.pass.features.security.center.addressoptions.navigation.AddressOptionsType
import proton.android.pass.features.security.center.addressoptions.navigation.GlobalMonitorAddressType

sealed interface SecurityCenterNavDestination {

    data class Back(
        val comesFromBottomSheet: Boolean = false,
        val force: Boolean = false
    ) : SecurityCenterNavDestination

    data object Empty : SecurityCenterNavDestination

    data object Home : SecurityCenterNavDestination

    data class ItemDetails(
        val shareId: ShareId,
        val itemId: ItemId,
        val origin: Origin
    ) : SecurityCenterNavDestination {
        sealed interface Origin {
            data object WeakPasswords : Origin
            data object ReusedPassword : Origin
            data object Missing2fa : Origin
            data object Excluded : Origin
            data object Report : Origin
        }
    }

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

    @JvmInline
    value class AddCustomEmail(val email: Option<String>) : SecurityCenterNavDestination

    data class VerifyEmail(val id: CustomEmailId, val email: String) :
        SecurityCenterNavDestination

    data class UnverifiedEmailOptions(
        val id: CustomEmailId,
        val email: String
    ) : SecurityCenterNavDestination

    data object EmailVerified : SecurityCenterNavDestination

    data class CustomEmailReport(
        val id: CustomEmailId,
        val email: String
    ) : SecurityCenterNavDestination

    data class AliasEmailReport(
        val id: BreachEmailId.Alias,
        val email: String
    ) : SecurityCenterNavDestination

    data class ProtonEmailReport(
        val id: BreachEmailId.Proton,
        val email: String
    ) : SecurityCenterNavDestination

    data class CustomEmailBreachDetail(
        val id: BreachEmailId.Custom
    ) : SecurityCenterNavDestination

    data class AliasEmailBreachDetail(
        val id: BreachEmailId.Alias
    ) : SecurityCenterNavDestination

    data class ProtonEmailBreachDetail(
        val id: BreachEmailId.Proton
    ) : SecurityCenterNavDestination

    data object ExcludedItems : SecurityCenterNavDestination

    data object AllProtonEmails : SecurityCenterNavDestination

    data object AllAliasEmails : SecurityCenterNavDestination

    data class DarkWebHelp(
        @StringRes val titleResId: Int,
        @StringRes val textResId: Int
    ) : SecurityCenterNavDestination

    data object CannotAddCustomEmails : SecurityCenterNavDestination

    data class GlobalMonitorAddressOptions(
        val addressOptionsType: AddressOptionsType,
        val globalMonitorAddressType: GlobalMonitorAddressType
    ) : SecurityCenterNavDestination

    data class ReportAliasAddressOptions(
        val breachEmailId: BreachEmailId.Alias,
        val addressOptionsType: AddressOptionsType
    ) : SecurityCenterNavDestination

    data class ReportProtonAddressOptions(
        val breachEmailId: BreachEmailId.Proton,
        val addressOptionsType: AddressOptionsType
    ) : SecurityCenterNavDestination

    data class ReportCustomAddressOptions(
        val breachEmailId: BreachEmailId.Custom,
        val addressOptionsType: AddressOptionsType
    ) : SecurityCenterNavDestination

    data object BackToDarkWebMonitoring : SecurityCenterNavDestination
}
