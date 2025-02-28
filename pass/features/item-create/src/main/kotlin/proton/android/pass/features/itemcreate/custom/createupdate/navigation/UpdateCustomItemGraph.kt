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

package proton.android.pass.features.itemcreate.custom.createupdate.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import proton.android.pass.common.api.Option
import proton.android.pass.domain.CustomFieldType
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.features.itemcreate.bottomsheets.customfield.customFieldBottomSheetGraph
import proton.android.pass.features.itemcreate.common.CustomFieldPrefix
import proton.android.pass.features.itemcreate.common.customsection.ExtraSectionNavigation
import proton.android.pass.features.itemcreate.common.customsection.extraSectionGraph
import proton.android.pass.features.itemcreate.custom.createupdate.ui.UpdateCustomItemScreen
import proton.android.pass.features.itemcreate.dialogs.customfield.CustomFieldNameNavigation
import proton.android.pass.features.itemcreate.dialogs.customfield.customFieldNameDialogGraph
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable

const val UPDATE_CUSTOM_ITEM_GRAPH = "update_custom_item_graph"

object UpdateCustomItemNavItem : NavItem(
    baseRoute = "customitem/update/screen",
    navArgIds = listOf(CommonNavArgId.ShareId, CommonNavArgId.ItemId)
) {
    fun createNavRoute(shareId: ShareId, itemId: ItemId) = "$baseRoute/${shareId.id}/${itemId.id}"
}

sealed interface UpdateCustomItemNavigation : BaseCustomItemNavigation {
    data class ItemUpdated(val shareId: ShareId, val itemId: ItemId) : CreateCustomItemNavigation
}

fun NavGraphBuilder.updateCustomItemGraph(onNavigate: (BaseCustomItemNavigation) -> Unit) {
    navigation(
        route = UPDATE_CUSTOM_ITEM_GRAPH,
        startDestination = UpdateCustomItemNavItem.route
    ) {
        composable(UpdateCustomItemNavItem) {
            UpdateCustomItemScreen(onNavigate = onNavigate)
        }
        customFieldBottomSheetGraph(
            prefix = CustomFieldPrefix.UpdateCustomItem,
            onAddCustomFieldNavigate = { type: CustomFieldType, sectionIndex: Option<Int> ->
                onNavigate(BaseCustomItemNavigation.CustomFieldTypeSelected(type, sectionIndex))
            },
            onEditCustomFieldNavigate = { title: String, index: Int, sectionIndex: Option<Int> ->
                onNavigate(BaseCustomItemNavigation.EditCustomField(title, index, sectionIndex))
            },
            onRemoveCustomFieldNavigate = {
                onNavigate(BaseCustomItemNavigation.RemoveCustomField)
            },
            onDismissBottomsheet = { onNavigate(BaseCustomItemNavigation.DismissBottomsheet) }
        )
        customFieldNameDialogGraph(CustomFieldPrefix.UpdateCustomItem) {
            when (it) {
                is CustomFieldNameNavigation.CloseScreen -> onNavigate(BaseCustomItemNavigation.CloseScreen)
            }
        }
        extraSectionGraph {
            when (it) {
                is ExtraSectionNavigation.CloseScreen -> onNavigate(BaseCustomItemNavigation.CloseScreen)
                is ExtraSectionNavigation.EditCustomSection ->
                    onNavigate(BaseCustomItemNavigation.EditSection(it.title, it.index))

                ExtraSectionNavigation.RemoveCustomSection ->
                    onNavigate(BaseCustomItemNavigation.RemoveSection)
            }
        }
    }
}

