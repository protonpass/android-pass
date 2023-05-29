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

package proton.android.pass.featureitemcreate.impl.login

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.toOption
import proton.android.pass.featureitemcreate.impl.common.KEY_VAULT_SELECTED
import proton.android.pass.featureitemcreate.impl.login.bottomsheet.aliasoptions.CLEAR_ALIAS_NAV_PARAMETER_KEY
import proton.android.pass.featureitemcreate.impl.totp.INDEX_NAV_PARAMETER_KEY
import proton.android.pass.featureitemcreate.impl.totp.TOTP_NAV_PARAMETER_KEY
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.OptionalNavArgId
import proton.android.pass.navigation.api.composable
import proton.android.pass.navigation.api.toPath
import proton.pass.domain.ShareId

object CreateLoginDefaultUsernameArg : OptionalNavArgId {
    override val key = "username"
    override val navType = NavType.StringType
}

object CreateLogin : NavItem(
    baseRoute = "login/create",
    optionalArgIds = listOf(CommonOptionalNavArgId.ShareId, CreateLoginDefaultUsernameArg)
) {
    fun createNavRoute(
        shareId: Option<ShareId> = None,
        username: Option<String> = None
    ) = buildString {
        append(baseRoute)
        val map = mutableMapOf<String, Any>()
        if (shareId is Some) {
            map[CommonOptionalNavArgId.ShareId.key] = shareId.value.id
        }
        if (username is Some) {
            map[CreateLoginDefaultUsernameArg.key] = username.value
        }
        val path = map.toPath()
        append(path)
    }
}

@OptIn(ExperimentalAnimationApi::class,)
fun NavGraphBuilder.createLoginGraph(
    initialCreateLoginUiState: InitialCreateLoginUiState = InitialCreateLoginUiState(),
    showCreateAliasButton: Boolean = true,
    onNavigate: (BaseLoginNavigation) -> Unit
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
}
