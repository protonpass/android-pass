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

package proton.android.pass.features.secure.links.overview.navigation

import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.securelinks.SecureLinkExpiration
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavParamEncoder
import proton.android.pass.navigation.api.toPath

object SecureLinksOverviewNavItem : NavItem(
    baseRoute = "secure-links/overview",
    navArgIds = listOf(
        CommonNavArgId.ShareId,
        CommonNavArgId.ItemId,
        SecureLinksOverviewExpirationNavArgId,
        SecureLinksOverviewLinkNavArgId
    ),
    optionalArgIds = listOf(SecureLinksOverviewMaxViewsNavArgId)
) {

    fun createNavRoute(
        shareId: ShareId,
        itemId: ItemId,
        expiration: SecureLinkExpiration,
        maxViewsAllowed: Int?,
        secureLink: String
    ) = buildString {
        append("$baseRoute/${shareId.id}/${itemId.id}/$expiration/${NavParamEncoder.encode(secureLink)}")

        mutableMapOf<String, Any>().apply {
            maxViewsAllowed?.let { put(SecureLinksOverviewMaxViewsNavArgId.key, it) }
        }.also { optionalArgs -> append(optionalArgs.toPath()) }
    }

}