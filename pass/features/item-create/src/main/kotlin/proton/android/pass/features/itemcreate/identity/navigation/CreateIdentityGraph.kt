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

package proton.android.pass.features.itemcreate.identity.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.toOption
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.domain.ShareId
import proton.android.pass.features.itemcreate.bottomsheets.customfield.customFieldBottomSheetGraph
import proton.android.pass.features.itemcreate.common.CustomFieldPrefix
import proton.android.pass.features.itemcreate.common.KEY_VAULT_SELECTED
import proton.android.pass.features.itemcreate.dialogs.customfield.CustomFieldNameNavigation
import proton.android.pass.features.itemcreate.dialogs.customfield.customFieldNameDialogGraph
import proton.android.pass.features.itemcreate.identity.navigation.bottomsheets.IdentityFieldsNavigation
import proton.android.pass.features.itemcreate.identity.navigation.bottomsheets.identityFieldsGraph
import proton.android.pass.features.itemcreate.identity.navigation.customsection.ExtraSectionNavigation
import proton.android.pass.features.itemcreate.identity.navigation.customsection.extraSectionGraph
import proton.android.pass.features.itemcreate.identity.ui.CreateIdentityScreen
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable
import proton.android.pass.navigation.api.toPath

const val CREATE_IDENTITY_GRAPH = "create_identity_graph"

object CreateIdentity : NavItem(
    baseRoute = "identity/create/screen",
    optionalArgIds = listOf(CommonOptionalNavArgId.ShareId)
) {
    fun createNavRoute(shareId: Option<ShareId> = None) = buildString {
        append(baseRoute)
        val map = mutableMapOf<String, Any>()
        if (shareId is Some) {
            map[CommonOptionalNavArgId.ShareId.key] = shareId.value.id
        }
        val path = map.toPath()
        append(path)
    }
}

sealed interface CreateIdentityNavigation : BaseIdentityNavigation {
    @JvmInline
    value class ItemCreated(val itemUiModel: ItemUiModel) : CreateIdentityNavigation

    @JvmInline
    value class SelectVault(val shareId: ShareId) : CreateIdentityNavigation
}

fun NavGraphBuilder.createIdentityGraph(onNavigate: (BaseIdentityNavigation) -> Unit) {
    navigation(
        route = CREATE_IDENTITY_GRAPH,
        startDestination = CreateIdentity.route
    ) {
        composable(CreateIdentity) { navBackStack ->
            val selectVault by navBackStack.savedStateHandle
                .getStateFlow<String?>(KEY_VAULT_SELECTED, null)
                .collectAsStateWithLifecycle()

            CreateIdentityScreen(
                selectVault = selectVault.toOption().map { ShareId(it) }.value(),
                onNavigate = onNavigate
            )
        }
        identityFieldsGraph {
            when (it) {
                IdentityFieldsNavigation.Close -> onNavigate(BaseIdentityNavigation.Close)
                IdentityFieldsNavigation.AddCustomField ->
                    onNavigate(BaseIdentityNavigation.OpenCustomFieldBottomSheet)
            }
        }
        customFieldBottomSheetGraph(
            prefix = CustomFieldPrefix.CreateIdentity,
            onAddCustomFieldNavigate = {
                onNavigate(BaseIdentityNavigation.CustomFieldTypeSelected(it))
            },
            onEditCustomFieldNavigate = { title: String, index: Int ->
                onNavigate(BaseIdentityNavigation.EditCustomField(title, index))
            },
            onRemoveCustomFieldNavigate = {
                onNavigate(BaseIdentityNavigation.RemovedCustomField)
            },
            onCloseNavigate = { onNavigate(BaseIdentityNavigation.Close) }
        )
        customFieldNameDialogGraph(CustomFieldPrefix.CreateIdentity) {
            when (it) {
                is CustomFieldNameNavigation.Close -> onNavigate(BaseIdentityNavigation.Close)
            }
        }
        extraSectionGraph {
            when (it) {
                is ExtraSectionNavigation.Close -> onNavigate(BaseIdentityNavigation.Close)
                is ExtraSectionNavigation.EditCustomSection ->
                    onNavigate(BaseIdentityNavigation.EditCustomSection(it.title, it.index))

                ExtraSectionNavigation.RemoveCustomSection ->
                    onNavigate(BaseIdentityNavigation.RemoveCustomSection)
            }
        }
    }
}

