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

package proton.android.pass.features.security.center.addressoptions.navigation

import proton.android.pass.domain.breach.BreachEmailId
import proton.android.pass.features.security.center.shared.navigation.BreachIdArgId
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavItemType

object SecurityCenterCustomAddressOptionsNavItem : NavItem(
    baseRoute = "security/center/custom/addressoptions/bottomsheet",
    navArgIds = listOf(BreachIdArgId, AddressOptionsTypeArgId),
    navItemType = NavItemType.Bottomsheet
) {
    fun createNavRoute(id: BreachEmailId.Custom, addressOptionsType: AddressOptionsType): String =
        "$baseRoute/${id.customEmailId.id}/$addressOptionsType"
}

object SecurityCenterAliasAddressOptionsNavItem : NavItem(
    baseRoute = "security/center/alias/addressoptions/bottomsheet",
    navArgIds = listOf(
        CommonNavArgId.ShareId,
        CommonNavArgId.ItemId,
        AddressOptionsTypeArgId
    ),
    navItemType = NavItemType.Bottomsheet
) {
    fun createNavRoute(id: BreachEmailId.Alias, addressOptionsType: AddressOptionsType): String =
        "$baseRoute/${id.shareId.id}/${id.itemId.id}/$addressOptionsType"
}

object SecurityCenterProtonAddressOptionsNavItem : NavItem(
    baseRoute = "security/center/proton/addressoptions/bottomsheet",
    navArgIds = listOf(CommonNavArgId.AddressId, AddressOptionsTypeArgId),
    navItemType = NavItemType.Bottomsheet
) {
    fun createNavRoute(id: BreachEmailId.Proton, addressOptionsType: AddressOptionsType): String =
        "$baseRoute/${id.addressId.id}/$addressOptionsType"
}
