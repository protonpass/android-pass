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

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.android.pass.domain.CustomFieldType
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.WifiSecurityType
import proton.android.pass.features.itemcreate.bottomsheets.customfield.customFieldBottomSheetGraph
import proton.android.pass.features.itemcreate.common.CustomFieldPrefix
import proton.android.pass.features.itemcreate.common.customsection.ExtraSectionNavigation
import proton.android.pass.features.itemcreate.common.customsection.extraSectionGraph
import proton.android.pass.features.itemcreate.custom.createupdate.ui.UpdateCustomItemScreen
import proton.android.pass.features.itemcreate.custom.selectwifisecuritytype.navigation.WIFI_SECURITY_TYPE_PARAMETER_KEY
import proton.android.pass.features.itemcreate.dialogs.customfield.CustomFieldNameNavigation
import proton.android.pass.features.itemcreate.dialogs.customfield.customFieldNameDialogGraph
import proton.android.pass.features.itemcreate.totp.INDEX_NAV_PARAMETER_KEY
import proton.android.pass.features.itemcreate.totp.SECTION_INDEX_NAV_PARAMETER_KEY
import proton.android.pass.features.itemcreate.totp.TOTP_NAV_PARAMETER_KEY
import proton.android.pass.features.itemcreate.totp.createTotpGraph
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

@Suppress("LongMethod")
fun NavGraphBuilder.updateCustomItemGraph(onNavigate: (BaseCustomItemNavigation) -> Unit) {
    navigation(
        route = UPDATE_CUSTOM_ITEM_GRAPH,
        startDestination = UpdateCustomItemNavItem.route
    ) {
        composable(UpdateCustomItemNavItem) { navBackStack ->
            val navTotpUri by navBackStack.savedStateHandle
                .getStateFlow<String?>(TOTP_NAV_PARAMETER_KEY, null)
                .collectAsStateWithLifecycle()
            val navTotpSectionIndex by navBackStack.savedStateHandle
                .getStateFlow<Int?>(SECTION_INDEX_NAV_PARAMETER_KEY, null)
                .collectAsStateWithLifecycle()
            val navTotpIndex by navBackStack.savedStateHandle
                .getStateFlow<Int?>(INDEX_NAV_PARAMETER_KEY, null)
                .collectAsStateWithLifecycle()

            LaunchedEffect(navTotpUri) {
                navBackStack.savedStateHandle.remove<String?>(TOTP_NAV_PARAMETER_KEY)
            }
            LaunchedEffect(navTotpSectionIndex) {
                navBackStack.savedStateHandle.remove<Int?>(SECTION_INDEX_NAV_PARAMETER_KEY)
            }
            LaunchedEffect(navTotpIndex) {
                navBackStack.savedStateHandle.remove<Int?>(INDEX_NAV_PARAMETER_KEY)
            }

            val wifiSecurityType by navBackStack.savedStateHandle
                .getStateFlow<Int?>(WIFI_SECURITY_TYPE_PARAMETER_KEY, null)
                .collectAsStateWithLifecycle()

            UpdateCustomItemScreen(
                selectTotp = Triple(
                    first = navTotpUri.toOption(),
                    second = navTotpSectionIndex.takeIf { value: Int? -> value != null && value >= 0 }.toOption(),
                    third = navTotpIndex.takeIf { value: Int? -> value != null && value >= 0 }.toOption()
                ),
                selectWifiSecurityType = wifiSecurityType.toOption().map { WifiSecurityType.fromId(it) },
                onNavigate = onNavigate
            )
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
        createTotpGraph(
            prefix = CustomFieldPrefix.UpdateCustomItem,
            onSuccess = { totp, sectionIndex, index ->
                val values = buildMap<String, Any> {
                    put(TOTP_NAV_PARAMETER_KEY, totp)
                    sectionIndex?.let { put(SECTION_INDEX_NAV_PARAMETER_KEY, it) }
                    index?.let { put(INDEX_NAV_PARAMETER_KEY, it) }
                }
                onNavigate(BaseCustomItemNavigation.TotpSuccess(values))
            },
            onCloseTotp = { onNavigate(BaseCustomItemNavigation.TotpCancel) },
            onOpenImagePicker = { sectionIndex, index ->
                onNavigate(
                    BaseCustomItemNavigation.OpenImagePicker(
                        sectionIndex = sectionIndex.toOption(),
                        index = index ?: -1
                    )
                )
            }
        )
    }
}

