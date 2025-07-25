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

package proton.android.pass.features.item.details.detail.navigation

import androidx.navigation.NavType
import proton.android.pass.commonuimodels.api.items.ItemDetailNavScope
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.OptionalNavArgId
import proton.android.pass.navigation.api.toPath

object ItemDetailScopeNavArgId : OptionalNavArgId {
    override val key: String = "itemDetailNavScope"
    override val navType: NavType<*> = NavType.EnumType(ItemDetailNavScope::class.java)
    override val default: Any = ItemDetailNavScope.Default
}

object ItemDetailsNavItem : NavItem(
    baseRoute = "item/details",
    navArgIds = listOf(
        CommonNavArgId.ShareId,
        CommonNavArgId.ItemId
    ),
    optionalArgIds = listOf(ItemDetailScopeNavArgId)
) {

    fun createNavRoute(
        shareId: ShareId,
        itemId: ItemId,
        scope: ItemDetailNavScope = ItemDetailNavScope.Default
    ) = buildString {
        append("$baseRoute/${shareId.id}/${itemId.id}")
        val optionalPath = mapOf(ItemDetailScopeNavArgId.key to scope).toPath()
        append(optionalPath)
    }

}
