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

package proton.android.pass.features.security.center.aliaslist.navigation

import proton.android.pass.domain.breach.BreachEmailId
import proton.android.pass.features.security.center.addressoptions.navigation.AddressOptionsType
import proton.android.pass.features.security.center.addressoptions.navigation.GlobalMonitorAddressType

sealed interface SecurityCenterAliasListNavDestination {
    data object Back : SecurityCenterAliasListNavDestination
    data class OnEmailClick(
        val id: BreachEmailId.Alias,
        val email: String
    ) : SecurityCenterAliasListNavDestination

    data class OnOptionsClick(
        val globalMonitorAddressType: GlobalMonitorAddressType,
        val addressOptionsType: AddressOptionsType
    ) : SecurityCenterAliasListNavDestination
}
