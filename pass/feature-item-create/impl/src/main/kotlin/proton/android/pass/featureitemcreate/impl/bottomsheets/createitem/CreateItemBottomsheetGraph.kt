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

package proton.android.pass.featureitemcreate.impl.bottomsheets.createitem

import androidx.navigation.NavGraphBuilder
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavItemType
import proton.android.pass.navigation.api.bottomSheet
import proton.android.pass.navigation.api.toPath
import proton.pass.domain.ShareId

object CreateItemBottomsheet : NavItem(
    baseRoute = "item/create/bottomsheet",
    optionalArgIds = listOf(CommonOptionalNavArgId.ShareId),
    navItemType = NavItemType.Bottomsheet
) {
    fun createNavRoute(
        shareId: Option<ShareId> = None,
    ) = buildString {
        append(baseRoute)
        val map = mutableMapOf<String, Any>()
        if (shareId is Some) {
            map[CommonOptionalNavArgId.ShareId.key] = shareId.value.id
        }
        val path = map.toPath()
        append(path)
    }
}

sealed interface CreateItemBottomsheetNavigation {
    data class CreateLogin(val shareId: Option<ShareId>) : CreateItemBottomsheetNavigation
    data class CreateAlias(val shareId: Option<ShareId>) : CreateItemBottomsheetNavigation
    data class CreateNote(val shareId: Option<ShareId>) : CreateItemBottomsheetNavigation
    data class CreateCreditCard(val shareId: Option<ShareId>) : CreateItemBottomsheetNavigation
    object CreatePassword : CreateItemBottomsheetNavigation
}

fun NavGraphBuilder.bottomsheetCreateItemGraph(
    mode: CreateItemBottomSheetMode,
    onNavigate: (CreateItemBottomsheetNavigation) -> Unit,
) {
    bottomSheet(CreateItemBottomsheet) {
        CreateItemBottomSheet(
            mode = mode,
            onNavigate = onNavigate
        )
    }
}
