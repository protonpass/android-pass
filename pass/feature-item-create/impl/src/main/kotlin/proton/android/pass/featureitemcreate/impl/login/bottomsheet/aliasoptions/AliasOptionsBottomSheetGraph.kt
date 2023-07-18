/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.featureitemcreate.impl.login.bottomsheet.aliasoptions

import androidx.navigation.NavGraphBuilder
import proton.android.pass.featureitemcreate.impl.login.BaseLoginNavigation
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavItemType
import proton.android.pass.navigation.api.ShowUpgradeNavArgId
import proton.android.pass.navigation.api.bottomSheet
import proton.pass.domain.ShareId

const val CLEAR_ALIAS_NAV_PARAMETER_KEY = "clearAlias"

object AliasOptionsBottomSheet : NavItem(
    baseRoute = "login/alias-options",
    navArgIds = listOf(CommonNavArgId.ShareId, ShowUpgradeNavArgId),
    navItemType = NavItemType.Bottomsheet
) {
    fun createNavRoute(shareId: ShareId, showUpgrade: Boolean) =
        "$baseRoute/${shareId.id}/$showUpgrade"
}

sealed interface AliasOptionsNavigation {
    object OnEditAlias : AliasOptionsNavigation
    object OnDeleteAlias : AliasOptionsNavigation
}

fun NavGraphBuilder.aliasOptionsBottomSheetGraph(
    onNavigate: (BaseLoginNavigation) -> Unit
) {
    bottomSheet(AliasOptionsBottomSheet) { navStack ->
        val shareId = navStack.arguments?.getString(CommonNavArgId.ShareId.key)
            ?: throw IllegalStateException("ShareId is required")
        val showUpgrade = navStack.arguments?.getBoolean(ShowUpgradeNavArgId.key)
            ?: throw IllegalStateException("ShowUpgrade is required")

        AliasOptionsBottomSheet(
            onNavigate = {
                when (it) {
                    is AliasOptionsNavigation.OnEditAlias -> {
                        onNavigate(BaseLoginNavigation.EditAlias(ShareId(shareId), showUpgrade))
                    }
                    is AliasOptionsNavigation.OnDeleteAlias -> {
                        onNavigate(BaseLoginNavigation.DeleteAlias)
                    }
                }
            }
        )
    }
}
