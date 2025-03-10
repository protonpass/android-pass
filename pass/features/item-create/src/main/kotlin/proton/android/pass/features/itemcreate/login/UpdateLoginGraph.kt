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

package proton.android.pass.features.itemcreate.login

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
import proton.android.pass.features.itemcreate.dialogs.customfield.CustomFieldNameNavigation
import proton.android.pass.features.itemcreate.dialogs.customfield.customFieldNameDialogGraph
import proton.android.pass.features.itemcreate.login.bottomsheet.aliasoptions.CLEAR_ALIAS_NAV_PARAMETER_KEY
import proton.android.pass.features.itemcreate.login.bottomsheet.aliasoptions.aliasOptionsBottomSheetGraph
import proton.android.pass.features.itemcreate.totp.INDEX_NAV_PARAMETER_KEY
import proton.android.pass.features.itemcreate.totp.SECTION_INDEX_NAV_PARAMETER_KEY
import proton.android.pass.features.itemcreate.totp.TOTP_NAV_PARAMETER_KEY
import proton.android.pass.features.itemcreate.totp.createTotpGraph
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable

private const val EDIT_LOGIN_GRAPH = "edit_login_graph"

object EditLoginNavItem : NavItem(
    baseRoute = "login/edit",
    navArgIds = listOf(CommonNavArgId.ShareId, CommonNavArgId.ItemId)
) {
    fun createNavRoute(shareId: ShareId, itemId: ItemId) = "$baseRoute/${shareId.id}/${itemId.id}"
}

@Suppress("LongParameterList", "LongMethod")
fun NavGraphBuilder.updateLoginGraph(canUseAttachments: Boolean, onNavigate: (BaseLoginNavigation) -> Unit) {
    navigation(
        route = EDIT_LOGIN_GRAPH,
        startDestination = EditLoginNavItem.route
    ) {
        composable(EditLoginNavItem) { navBackStack ->
            val navTotpUri by navBackStack.savedStateHandle
                .getStateFlow<String?>(TOTP_NAV_PARAMETER_KEY, null)
                .collectAsStateWithLifecycle()

            LaunchedEffect(navTotpUri) {
                navBackStack.savedStateHandle.remove<String?>(TOTP_NAV_PARAMETER_KEY)
            }

            val navTotpIndex by navBackStack.savedStateHandle
                .getStateFlow<Int?>(INDEX_NAV_PARAMETER_KEY, null)
                .collectAsStateWithLifecycle()

            LaunchedEffect(navTotpIndex) {
                navBackStack.savedStateHandle.remove<Int?>(INDEX_NAV_PARAMETER_KEY)
            }

            val clearAlias by navBackStack.savedStateHandle
                .getStateFlow(CLEAR_ALIAS_NAV_PARAMETER_KEY, false)
                .collectAsStateWithLifecycle()

            LaunchedEffect(clearAlias) {
                navBackStack.savedStateHandle.remove<Boolean?>(CLEAR_ALIAS_NAV_PARAMETER_KEY)
            }

            UpdateLogin(
                clearAlias = clearAlias,
                navTotpUri = navTotpUri,
                navTotpIndex = navTotpIndex,
                canUseAttachments = canUseAttachments,
                onNavigate = onNavigate
            )
        }

        aliasOptionsBottomSheetGraph(onNavigate)
        customFieldBottomSheetGraph(
            prefix = CustomFieldPrefix.UpdateLogin,
            onAddCustomFieldNavigate = { type, _ ->
                onNavigate(BaseLoginNavigation.CustomFieldTypeSelected(type))
            },
            onEditCustomFieldNavigate = { title: String, index: Int, _: Option<Int> ->
                onNavigate(BaseLoginNavigation.EditCustomField(title, index))
            },
            onRemoveCustomFieldNavigate = { onNavigate(BaseLoginNavigation.RemovedCustomField) },
            onDismissBottomsheet = { onNavigate(BaseLoginNavigation.DismissBottomsheet) }
        )
        customFieldNameDialogGraph(CustomFieldPrefix.UpdateLogin) {
            when (it) {
                is CustomFieldNameNavigation.CloseScreen -> {
                    onNavigate(BaseLoginNavigation.CloseScreen)
                }
            }
        }
        createTotpGraph(
            onSuccess = { totp, sectionIndex, index ->
                val values = buildMap<String, Any> {
                    put(TOTP_NAV_PARAMETER_KEY, totp)
                    sectionIndex?.let { put(SECTION_INDEX_NAV_PARAMETER_KEY, it) }
                    index?.let { put(INDEX_NAV_PARAMETER_KEY, it) }
                }
                onNavigate(BaseLoginNavigation.TotpSuccess(values))
            },
            onCloseTotp = { onNavigate(BaseLoginNavigation.TotpCancel) },
            onOpenImagePicker = { _, index ->
                onNavigate(BaseLoginNavigation.OpenImagePicker(index.toOption()))
            }
        )
    }
}
