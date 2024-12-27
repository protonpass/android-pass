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
import androidx.navigation.NavType
import androidx.navigation.navigation
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.toOption
import proton.android.pass.domain.ShareId
import proton.android.pass.features.itemcreate.bottomsheets.customfield.customFieldBottomSheetGraph
import proton.android.pass.features.itemcreate.common.CustomFieldPrefix
import proton.android.pass.features.itemcreate.common.KEY_VAULT_SELECTED
import proton.android.pass.features.itemcreate.dialogs.customfield.CustomFieldNameNavigation
import proton.android.pass.features.itemcreate.dialogs.customfield.customFieldNameDialogGraph
import proton.android.pass.features.itemcreate.login.bottomsheet.aliasoptions.CLEAR_ALIAS_NAV_PARAMETER_KEY
import proton.android.pass.features.itemcreate.login.bottomsheet.aliasoptions.aliasOptionsBottomSheetGraph
import proton.android.pass.features.itemcreate.totp.INDEX_NAV_PARAMETER_KEY
import proton.android.pass.features.itemcreate.totp.TOTP_NAV_PARAMETER_KEY
import proton.android.pass.features.itemcreate.totp.createTotpGraph
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.OptionalNavArgId
import proton.android.pass.navigation.api.composable
import proton.android.pass.navigation.api.toPath

const val CREATE_LOGIN_GRAPH = "create_login_graph"

object CreateLoginDefaultEmailArg : OptionalNavArgId {

    override val key = "default_email"

    override val navType = NavType.StringType

}

object CreateLogin : NavItem(
    baseRoute = "login/create",
    optionalArgIds = listOf(CommonOptionalNavArgId.ShareId, CreateLoginDefaultEmailArg)
) {
    fun createNavRoute(shareId: Option<ShareId> = None, emailOption: Option<String> = None) = buildString {
        append(baseRoute)

        mutableMapOf<String, Any>().apply {
            if (shareId is Some) {
                set(CommonOptionalNavArgId.ShareId.key, shareId.value.id)
            }
            if (emailOption is Some) {
                set(CreateLoginDefaultEmailArg.key, emailOption.value)
            }
        }.also { optionalArgs -> append(optionalArgs.toPath()) }
    }
}

@Suppress("LongMethod")
fun NavGraphBuilder.createLoginGraph(
    initialCreateLoginUiState: InitialCreateLoginUiState = InitialCreateLoginUiState(),
    showCreateAliasButton: Boolean = true,
    onNavigate: (BaseLoginNavigation) -> Unit
) {
    navigation(
        route = CREATE_LOGIN_GRAPH,
        startDestination = CreateLogin.route
    ) {
        composable(CreateLogin) { navBackStack ->
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

            val selectVault by navBackStack.savedStateHandle
                .getStateFlow<String?>(KEY_VAULT_SELECTED, null)
                .collectAsStateWithLifecycle()

            val initialContents = initialCreateLoginUiState.copy(
                navTotpUri = navTotpUri,
                navTotpIndex = navTotpIndex ?: -1
            )

            CreateLoginScreen(
                initialContents = initialContents,
                clearAlias = clearAlias,
                selectVault = selectVault.toOption().map { ShareId(it) }.value(),
                showCreateAliasButton = showCreateAliasButton,
                onNavigate = onNavigate
            )
        }
        aliasOptionsBottomSheetGraph(onNavigate)
        customFieldBottomSheetGraph(
            prefix = CustomFieldPrefix.CreateLogin,
            onAddCustomFieldNavigate = { onNavigate(BaseLoginNavigation.CustomFieldTypeSelected(it)) },
            onEditCustomFieldNavigate = { title: String, index: Int ->
                onNavigate(BaseLoginNavigation.EditCustomField(title, index))
            },
            onRemoveCustomFieldNavigate = { onNavigate(BaseLoginNavigation.RemovedCustomField) },
            onCloseNavigate = { onNavigate(BaseLoginNavigation.Close) }
        )
        customFieldNameDialogGraph(CustomFieldPrefix.CreateLogin) {
            when (it) {
                is CustomFieldNameNavigation.Close -> {
                    onNavigate(BaseLoginNavigation.Close)
                }
            }
        }
        createTotpGraph(
            onSuccess = { totp, index ->
                val values = mutableMapOf<String, Any>(TOTP_NAV_PARAMETER_KEY to totp)
                index?.let { values.put(INDEX_NAV_PARAMETER_KEY, it) }
                onNavigate(BaseLoginNavigation.TotpSuccess(values))
            },
            onCloseTotp = { onNavigate(BaseLoginNavigation.TotpCancel) },
            onOpenImagePicker = {
                onNavigate(BaseLoginNavigation.OpenImagePicker(it.toOption()))
            }
        )
    }
}
