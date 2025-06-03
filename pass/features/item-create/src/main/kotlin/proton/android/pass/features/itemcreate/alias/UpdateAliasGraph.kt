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

package proton.android.pass.features.itemcreate.alias

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.features.itemcreate.bottomsheets.customfield.customFieldBottomSheetGraph
import proton.android.pass.features.itemcreate.common.CustomFieldPrefix
import proton.android.pass.features.itemcreate.dialogs.customfield.CustomFieldNameNavigation
import proton.android.pass.features.itemcreate.dialogs.customfield.customFieldNameDialogGraph
import proton.android.pass.features.itemcreate.totp.INDEX_NAV_PARAMETER_KEY
import proton.android.pass.features.itemcreate.totp.TOTP_NAV_PARAMETER_KEY
import proton.android.pass.features.itemcreate.totp.createTotpGraph
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable

object EditAliasNavItem : NavItem(
    baseRoute = "alias/edit",
    navArgIds = listOf(CommonNavArgId.ShareId, CommonNavArgId.ItemId),
    optionalArgIds = emptyList()
) {
    fun createNavRoute(shareId: ShareId, itemId: ItemId) = buildString {
        append("$baseRoute/${shareId.id}/${itemId.id}")
    }
}

@OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class
)
fun NavGraphBuilder.updateAliasGraph(onNavigate: (BaseAliasNavigation) -> Unit) {
    composable(EditAliasNavItem) {
        UpdateAlias(
            onNavigate = onNavigate
        )
    }
    customFieldBottomSheetGraph(
        prefix = CustomFieldPrefix.UpdateAlias,
        onAddCustomFieldNavigate = { type, _ ->
            onNavigate(BaseAliasNavigation.CustomFieldTypeSelected(type))
        },
        onEditCustomFieldNavigate = { title: String, index: Int, _: Option<Int> ->
            onNavigate(BaseAliasNavigation.EditCustomField(title, index))
        },
        onRemoveCustomFieldNavigate = { onNavigate(BaseAliasNavigation.RemovedCustomField) },
        onDismissBottomsheet = { onNavigate(BaseAliasNavigation.CloseBottomsheet) }
    )
    customFieldNameDialogGraph(CustomFieldPrefix.UpdateAlias) {
        when (it) {
            is CustomFieldNameNavigation.CloseScreen -> {
                onNavigate(BaseAliasNavigation.CloseScreen)
            }
        }
    }
    createTotpGraph(
        prefix = CustomFieldPrefix.UpdateAlias,
        onSuccess = { totp, _, index ->
            val values = buildMap<String, Any> {
                put(TOTP_NAV_PARAMETER_KEY, totp)
                index?.let { put(INDEX_NAV_PARAMETER_KEY, it) }
            }
            onNavigate(BaseAliasNavigation.TotpSuccess(values))
        },
        onCloseTotp = { onNavigate(BaseAliasNavigation.TotpCancel) },
        onOpenImagePicker = { _, index ->
            onNavigate(BaseAliasNavigation.OpenImagePicker(index.toOption()))
        }
    )
}
