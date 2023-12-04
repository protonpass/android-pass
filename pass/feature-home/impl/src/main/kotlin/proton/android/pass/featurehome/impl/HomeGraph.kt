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

package proton.android.pass.featurehome.impl

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.android.pass.commonuimodels.api.ItemTypeUiState
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable
import proton.android.pass.navigation.api.toPath

const val HOME_GO_TO_VAULT_KEY = "home_go_to_vault"
const val HOME_ENABLE_BULK_ACTIONS_KEY = "home_enable_bulk_actions"

object Home : NavItem(
    baseRoute = "home",
    optionalArgIds = listOf(CommonOptionalNavArgId.ShareId),
    isTopLevel = true
) {
    fun buildRoute(shareId: ShareId?): String = buildString {
        append(baseRoute)

        if (shareId != null) {
            val path = mapOf(CommonOptionalNavArgId.ShareId.key to shareId.id)
            append(path.toPath())
        }
    }
}

fun NavGraphBuilder.homeGraph(
    onNavigateEvent: (HomeNavigation) -> Unit,
) {
    composable(Home) { navBackStack ->
        val goToVault by navBackStack.savedStateHandle
            .getStateFlow<String?>(HOME_GO_TO_VAULT_KEY, null)
            .collectAsStateWithLifecycle()

        val enableBulkActions by navBackStack.savedStateHandle
            .getStateFlow(HOME_ENABLE_BULK_ACTIONS_KEY, false)
            .collectAsStateWithLifecycle()

        LaunchedEffect(goToVault) {
            navBackStack.savedStateHandle.remove<String?>(HOME_GO_TO_VAULT_KEY)
        }
        LaunchedEffect(enableBulkActions) {
            navBackStack.savedStateHandle.remove<Boolean?>(HOME_ENABLE_BULK_ACTIONS_KEY)
        }

        HomeScreen(
            modifier = Modifier.testTag(HomeScreenTestTag.screen),
            goToVault = goToVault.toOption().map { ShareId(it) }.value(),
            enableBulkActions = enableBulkActions,
            onNavigateEvent = onNavigateEvent
        )
    }
}

sealed interface HomeNavigation {
    data class AddItem(
        val shareId: Option<ShareId>,
        val itemTypeUiState: ItemTypeUiState
    ) : HomeNavigation

    data class EditLogin(val shareId: ShareId, val itemId: ItemId) : HomeNavigation
    data class EditNote(val shareId: ShareId, val itemId: ItemId) : HomeNavigation
    data class EditAlias(val shareId: ShareId, val itemId: ItemId) : HomeNavigation
    data class EditCreditCard(val shareId: ShareId, val itemId: ItemId) : HomeNavigation
    data class ItemDetail(val shareId: ShareId, val itemId: ItemId) : HomeNavigation
    object Profile : HomeNavigation
    object CreateVault : HomeNavigation
    data class VaultOptions(val shareId: ShareId) : HomeNavigation
    object SortingBottomsheet : HomeNavigation
    object TrialInfo : HomeNavigation
    object OpenInvite : HomeNavigation
    object Finish : HomeNavigation
    object OnBoarding : HomeNavigation
    object SyncDialog : HomeNavigation
    object ConfirmedInvite : HomeNavigation
    object SearchOptions : HomeNavigation
    object MoveToVault : HomeNavigation
}
