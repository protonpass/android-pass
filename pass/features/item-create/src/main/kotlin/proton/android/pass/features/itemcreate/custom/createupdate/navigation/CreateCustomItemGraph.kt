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

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navigation
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.toOption
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.domain.CustomFieldType
import proton.android.pass.domain.ShareId
import proton.android.pass.features.itemcreate.bottomsheets.customfield.customFieldBottomSheetGraph
import proton.android.pass.features.itemcreate.common.CustomFieldPrefix
import proton.android.pass.features.itemcreate.common.KEY_VAULT_SELECTED
import proton.android.pass.features.itemcreate.common.customsection.ExtraSectionNavigation
import proton.android.pass.features.itemcreate.common.customsection.extraSectionGraph
import proton.android.pass.features.itemcreate.custom.createupdate.ui.CreateCustomItemScreen
import proton.android.pass.features.itemcreate.custom.shared.TemplateType
import proton.android.pass.features.itemcreate.dialogs.customfield.CustomFieldNameNavigation
import proton.android.pass.features.itemcreate.dialogs.customfield.customFieldNameDialogGraph
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.OptionalNavArgId
import proton.android.pass.navigation.api.composable
import proton.android.pass.navigation.api.toPath

const val CREATE_CUSTOM_ITEM_GRAPH = "create_custom_item_graph"

internal object TemplateTypeNavArgId : OptionalNavArgId {
    override val key = "templateType"
    override val navType = NavType.IntType
}

object CreateCustomItemNavItem : NavItem(
    baseRoute = "customitem/create/screen",
    optionalArgIds = listOf(CommonOptionalNavArgId.ShareId, TemplateTypeNavArgId)
) {
    fun createNavRoute(shareId: Option<ShareId> = None, templateType: Option<TemplateType> = None) = buildString {
        append(baseRoute)
        val map = mutableMapOf<String, Any>()
        if (shareId is Some) {
            map[CommonOptionalNavArgId.ShareId.key] = shareId.value.id
        }
        map[TemplateTypeNavArgId.key] = templateType.value()?.id ?: -1
        val path = map.toPath()
        append(path)
    }
}

sealed interface CreateCustomItemNavigation : BaseCustomItemNavigation {
    @JvmInline
    value class ItemCreated(val itemUiModel: ItemUiModel) : CreateCustomItemNavigation

    @JvmInline
    value class SelectVault(val shareId: ShareId) : CreateCustomItemNavigation
}

fun NavGraphBuilder.createCustomItemGraph(onNavigate: (BaseCustomItemNavigation) -> Unit) {
    navigation(
        route = CREATE_CUSTOM_ITEM_GRAPH,
        startDestination = CreateCustomItemNavItem.route
    ) {
        composable(CreateCustomItemNavItem) { navBackStack ->
            val selectVault by navBackStack.savedStateHandle
                .getStateFlow<String?>(KEY_VAULT_SELECTED, null)
                .collectAsStateWithLifecycle()

            CreateCustomItemScreen(
                selectVault = selectVault.toOption().map { ShareId(it) }.value(),
                onNavigate = onNavigate
            )
        }
        customFieldBottomSheetGraph(
            prefix = CustomFieldPrefix.CreateCustomItem,
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
        customFieldNameDialogGraph(CustomFieldPrefix.CreateCustomItem) {
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

