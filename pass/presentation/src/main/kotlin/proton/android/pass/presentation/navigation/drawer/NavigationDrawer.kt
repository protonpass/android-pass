/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */
package proton.android.pass.presentation.navigation.drawer

import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.DrawerState
import androidx.compose.material.ModalDrawer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import proton.android.pass.presentation.navigation.CoreNavigation
import proton.pass.domain.ShareId

@Stable
enum class SelectedItemTypes {
    AllItems,
    Logins,
    Aliases,
    Notes
}

@Stable
sealed class SelectedVaults {
    object AllVaults : SelectedVaults()
    data class Vault(val shareId: ShareId) : SelectedVaults()
}

@Stable
data class NavDrawerNavigation(
    val onNavHome: (SelectedItemTypes, SelectedVaults) -> Unit,
    val onNavSettings: () -> Unit,
    val onNavTrash: () -> Unit,
    val onInternalDrawerClick: () -> Unit,
    val onBugReport: () -> Unit
)

@Composable
fun ModalNavigationDrawer(
    drawerUiState: DrawerUiState,
    drawerState: DrawerState,
    navDrawerNavigation: NavDrawerNavigation,
    coreNavigation: CoreNavigation,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    onSignOutClick: () -> Unit,
    signOutDialog: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    ModalDrawer(
        drawerState = drawerState,
        drawerShape = CutCornerShape(0.dp),
        drawerContent = {
            NavigationDrawerContent(
                drawerUiState = drawerUiState,
                coreNavigation = coreNavigation,
                navDrawerNavigation = navDrawerNavigation,
                onCloseDrawer = { coroutineScope.launch { drawerState.close() } },
                onSignOutClick = onSignOutClick
            )
            signOutDialog()
        }
    ) {
        content()
    }
}
