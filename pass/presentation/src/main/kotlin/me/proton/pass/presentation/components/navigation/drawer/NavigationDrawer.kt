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
package me.proton.pass.presentation.components.navigation.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.DrawerState
import androidx.compose.material.ModalDrawer
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.presentation.compose.AccountPrimaryItem
import me.proton.core.accountmanager.presentation.compose.AccountPrimaryState
import me.proton.core.accountmanager.presentation.compose.rememberAccountPrimaryState
import me.proton.core.compose.theme.ProtonDimens.DefaultSpacing
import me.proton.core.compose.theme.ProtonDimens.SmallSpacing
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.presentation.components.navigation.CoreNavigation

@Stable
enum class HomeSection {
    Items,
    Logins,
    Aliases,
    Notes
}

@Stable
data class NavDrawerNavigation(
    val onNavHome: (HomeSection) -> Unit,
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
            NavigationDrawer(
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

@Composable
fun NavigationDrawer(
    modifier: Modifier = Modifier,
    drawerUiState: DrawerUiState,
    accountPrimaryState: AccountPrimaryState = rememberAccountPrimaryState(),
    navDrawerNavigation: NavDrawerNavigation,
    coreNavigation: CoreNavigation,
    onSignOutClick: () -> Unit = {},
    onCloseDrawer: () -> Unit
) {
    val sidebarColors = requireNotNull(ProtonTheme.colors.sidebarColors)
    ProtonTheme(colors = sidebarColors) {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = ProtonTheme.colors.backgroundNorm
        ) {
            Column {
                if (drawerUiState.currentUser != null) {
                    AccountPrimaryItem(
                        modifier = Modifier
                            .background(sidebarColors.backgroundNorm)
                            .padding(all = SmallSpacing)
                            .fillMaxWidth(),
                        onRemove = { coreNavigation.onRemove(it) },
                        onSignIn = { coreNavigation.onSignIn(it) },
                        onSignOut = { coreNavigation.onSignOut(it) },
                        onSwitch = { coreNavigation.onSwitch(it) },
                        viewState = accountPrimaryState
                    )
                }
                PassNavigationDrawer(
                    modifier = Modifier
                        .padding(top = DefaultSpacing)
                        .weight(1f, fill = false),
                    drawerUiState = drawerUiState,
                    navDrawerNavigation = navDrawerNavigation,
                    onSignOutClick = onSignOutClick,
                    onCloseDrawer = onCloseDrawer
                )
            }
        }
    }
}

/*
@Composable
private fun SharesList(
    closeDrawerAction: (() -> Unit) -> Unit,
    viewEvent: NavigationDrawerViewEvent,
    shares: List<ShareUiModel>,
    modifier: Modifier = Modifier,
) {
    val title = stringResource(R.string.navigation_my_vaults)
    Column {
        Row {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .height(ListItemHeight)
                    .clickable(onClick = {
                        closeDrawerAction { viewEvent.onShareSelected(ShareClickEvent.AllShares) }
                    })
                    .padding(
                        top = SmallSpacing,
                        bottom = SmallSpacing,
                        start = DefaultSpacing,
                        end = MediumSpacing
                    )
                    .semantics(mergeDescendants = true) {
                        contentDescription = title
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painterResource(R.drawable.ic_proton_vault),
                    contentDescription = null,
                    tint = ProtonTheme.colors.iconWeak
                )
                Text(
                    text = title,
                    modifier = Modifier.padding(start = ListItemTextStartPadding),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(painterResource(R.drawable.ic_proton_chevron_down), "", tint = Color.White)
            }
        }

        shares.map {
            ShareItem(
                closeDrawerAction = closeDrawerAction,
                share = it,
                viewEvent = viewEvent,
                modifier = modifier.padding(PaddingValues(start = 22.dp))
            )
        }
    }
}

@Composable
private fun ShareItem(
    closeDrawerAction: (() -> Unit) -> Unit,
    share: ShareUiModel,
    viewEvent: NavigationDrawerViewEvent,
    modifier: Modifier = Modifier,
) {
    NavigationDrawerListItem(
        title = share.name,
        icon = R.drawable.ic_proton_vault,
        closeDrawerAction = closeDrawerAction,
        modifier = modifier,
    ) {
        viewEvent.onShareSelected(ShareClickEvent.Share(share))
    }
}
*/

