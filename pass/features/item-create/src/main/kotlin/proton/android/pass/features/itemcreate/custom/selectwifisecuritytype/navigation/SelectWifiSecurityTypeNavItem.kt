/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.itemcreate.custom.selectwifisecuritytype.navigation

import androidx.navigation.NavType
import proton.android.pass.domain.WifiSecurityType
import proton.android.pass.navigation.api.NavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavItemType

const val WIFI_SECURITY_TYPE_PARAMETER_KEY = "wifiSecurityType"

internal object SelectedWifiSecurityTypeNavArgId : NavArgId {
    override val key = "selectedWifiSecurityType"
    override val navType = NavType.IntType
}

object SelectWifiSecurityTypeNavItem : NavItem(
    baseRoute = "customitem/selectwifisecuritytype/bottomsheet",
    navArgIds = listOf(SelectedWifiSecurityTypeNavArgId),
    navItemType = NavItemType.Bottomsheet
) {
    fun createNavRoute(wifiSecurityType: WifiSecurityType) = "$baseRoute/${wifiSecurityType.id}"
}
