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

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.features.itemcreate.bottomsheets.customfield.customFieldBottomSheetGraph
import proton.android.pass.features.itemcreate.common.CustomFieldPrefix
import proton.android.pass.features.itemcreate.common.customsection.ExtraSectionNavigation
import proton.android.pass.features.itemcreate.common.customsection.extraSectionGraph
import proton.android.pass.features.itemcreate.dialogs.customfield.CustomFieldNameNavigation
import proton.android.pass.features.itemcreate.dialogs.customfield.customFieldNameDialogGraph
import proton.android.pass.features.itemcreate.identity.navigation.bottomsheets.IdentityFieldsNavigation
import proton.android.pass.features.itemcreate.identity.navigation.bottomsheets.identityFieldsGraph
import proton.android.pass.features.itemcreate.identity.ui.TotpNavParams
import proton.android.pass.features.itemcreate.identity.ui.UpdateIdentityScreen
import proton.android.pass.features.itemcreate.totp.INDEX_NAV_PARAMETER_KEY
import proton.android.pass.features.itemcreate.totp.SECTION_INDEX_NAV_PARAMETER_KEY
import proton.android.pass.features.itemcreate.totp.SPECIAL_SECTION_INDEX_NAV_PARAMETER_KEY
import proton.android.pass.features.itemcreate.totp.TOTP_NAV_PARAMETER_KEY
import proton.android.pass.features.itemcreate.totp.createTotpGraph
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable

const val UPDATE_IDENTITY_GRAPH = "update_identity_graph"

object UpdateIdentityNavItem : NavItem(
    baseRoute = "identity/update/screen",
    navArgIds = listOf(CommonNavArgId.ShareId, CommonNavArgId.ItemId)
) {
    fun createNavRoute(shareId: ShareId, itemId: ItemId) = "$baseRoute/${shareId.id}/${itemId.id}"
}

sealed interface UpdateIdentityNavigation {
    data class IdentityUpdated(val shareId: ShareId, val itemId: ItemId) : BaseIdentityNavigation
}

@Suppress("LongMethod")
fun NavGraphBuilder.updateIdentityGraph(onNavigate: (BaseIdentityNavigation) -> Unit) {
    navigation(
        route = UPDATE_IDENTITY_GRAPH,
        startDestination = UpdateIdentityNavItem.route
    ) {
        composable(UpdateIdentityNavItem) { navBackStack ->
            val navTotpUri by navBackStack.savedStateHandle
                .getStateFlow<String?>(TOTP_NAV_PARAMETER_KEY, null)
                .collectAsStateWithLifecycle()
            val navTotpSectionIndex by navBackStack.savedStateHandle
                .getStateFlow<Int?>(SECTION_INDEX_NAV_PARAMETER_KEY, null)
                .collectAsStateWithLifecycle()
            val navTotpSpecialIndex by navBackStack.savedStateHandle
                .getStateFlow<Int?>(SPECIAL_SECTION_INDEX_NAV_PARAMETER_KEY, null)
                .collectAsStateWithLifecycle()
            val navTotpIndex by navBackStack.savedStateHandle
                .getStateFlow<Int?>(INDEX_NAV_PARAMETER_KEY, null)
                .collectAsStateWithLifecycle()
            val totpNavParams = navTotpUri?.let {
                TotpNavParams(
                    uri = it,
                    specialSectionIndex = navTotpSpecialIndex.toOption(),
                    sectionIndex = navTotpSectionIndex.toOption(),
                    index = navTotpIndex.toOption()
                )
            }
            LaunchedEffect(navTotpUri) {
                navBackStack.savedStateHandle.remove<String?>(TOTP_NAV_PARAMETER_KEY)
            }
            LaunchedEffect(navTotpSectionIndex) {
                navBackStack.savedStateHandle.remove<Int?>(SECTION_INDEX_NAV_PARAMETER_KEY)
            }
            LaunchedEffect(navTotpSpecialIndex) {
                navBackStack.savedStateHandle.remove<Int?>(SPECIAL_SECTION_INDEX_NAV_PARAMETER_KEY)
            }
            LaunchedEffect(navTotpIndex) {
                navBackStack.savedStateHandle.remove<Int?>(INDEX_NAV_PARAMETER_KEY)
            }
            UpdateIdentityScreen(
                totpNavParams = totpNavParams,
                onNavigate = onNavigate
            )
        }
        identityFieldsGraph {
            when (it) {
                IdentityFieldsNavigation.DismissBottomsheet ->
                    onNavigate(BaseIdentityNavigation.DismissBottomsheet)
                IdentityFieldsNavigation.AddCustomField ->
                    onNavigate(BaseIdentityNavigation.OpenCustomFieldBottomSheet)
            }
        }
        customFieldBottomSheetGraph(
            prefix = CustomFieldPrefix.UpdateIdentity,
            onAddCustomFieldNavigate = { type, _ ->
                onNavigate(BaseIdentityNavigation.CustomFieldTypeSelected(type))
            },
            onEditCustomFieldNavigate = { title: String, index: Int, _: Option<Int> ->
                onNavigate(BaseIdentityNavigation.EditCustomField(title, index))
            },
            onRemoveCustomFieldNavigate = {
                onNavigate(BaseIdentityNavigation.RemovedCustomField)
            },
            onDismissBottomsheet = { onNavigate(BaseIdentityNavigation.DismissBottomsheet) }
        )
        customFieldNameDialogGraph(CustomFieldPrefix.UpdateIdentity) {
            when (it) {
                is CustomFieldNameNavigation.CloseScreen -> onNavigate(BaseIdentityNavigation.CloseScreen)
            }
        }
        extraSectionGraph {
            when (it) {
                is ExtraSectionNavigation.CloseScreen ->
                    onNavigate(BaseIdentityNavigation.CloseScreen)
                is ExtraSectionNavigation.EditCustomSection ->
                    onNavigate(BaseIdentityNavigation.EditCustomSection(it.title, it.index))

                ExtraSectionNavigation.RemoveCustomSection ->
                    onNavigate(BaseIdentityNavigation.RemoveCustomSection)
            }
        }
        createTotpGraph(
            prefix = CustomFieldPrefix.UpdateIdentity,
            onSuccess = { totp, specialIndex, sectionIndex, index ->
                val values = buildMap<String, Any> {
                    put(TOTP_NAV_PARAMETER_KEY, totp)
                    specialIndex?.let { put(SPECIAL_SECTION_INDEX_NAV_PARAMETER_KEY, it) }
                    sectionIndex?.let { put(SECTION_INDEX_NAV_PARAMETER_KEY, it) }
                    index?.let { put(INDEX_NAV_PARAMETER_KEY, it) }
                }
                onNavigate(BaseIdentityNavigation.TotpSuccess(values))
            },
            onCloseTotp = { onNavigate(BaseIdentityNavigation.TotpCancel) },
            onOpenImagePicker = { specialIndex, sectionIndex, index ->
                onNavigate(
                    BaseIdentityNavigation.OpenImagePicker(
                        specialIndex = specialIndex.toOption(),
                        sectionIndex = sectionIndex.toOption(),
                        index = index.toOption()
                    )
                )
            }
        )
    }
}

