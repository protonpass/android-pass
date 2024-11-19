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

package proton.android.pass.features.security.center.report.navigation

import proton.android.pass.domain.breach.BreachEmailId
import proton.android.pass.domain.breach.CustomEmailId
import proton.android.pass.features.security.center.shared.navigation.BreachIdArgId
import proton.android.pass.features.security.center.shared.navigation.EmailArgId
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavParamEncoder

object SecurityCenterCustomEmailReportNavItem : NavItem(
    baseRoute = "security/center/customemailreport",
    navArgIds = listOf(BreachIdArgId, EmailArgId),
    baseDeepLinkRoute = listOf("custom_email_breach")
) {
    fun createNavRoute(id: CustomEmailId, email: String): String =
        "$baseRoute/${id.id}/${NavParamEncoder.encode(email)}"
}

object SecurityCenterAliasEmailReportNavItem : NavItem(
    baseRoute = "security/center/aliasemailreport",
    navArgIds = listOf(CommonNavArgId.ShareId, CommonNavArgId.ItemId, EmailArgId),
    baseDeepLinkRoute = listOf("alias_breach")
) {
    fun createNavRoute(id: BreachEmailId.Alias, email: String): String =
        "$baseRoute/${id.shareId.id}/${id.itemId.id}/${NavParamEncoder.encode(email)}"
}

object SecurityCenterProtonEmailReportNavItem : NavItem(
    baseRoute = "security/center/protonemailreport",
    navArgIds = listOf(CommonNavArgId.AddressId, EmailArgId),
    baseDeepLinkRoute = listOf("address_breach")
) {
    fun createNavRoute(id: BreachEmailId.Proton, email: String): String =
        "$baseRoute/${id.addressId.id}/${NavParamEncoder.encode(email)}"
}
