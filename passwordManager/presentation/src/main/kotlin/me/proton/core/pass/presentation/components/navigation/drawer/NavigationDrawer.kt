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
package me.proton.core.pass.presentation.components.navigation.drawer

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DrawerState
import androidx.compose.material.ModalDrawer
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.presentation.compose.AccountPrimaryItem
import me.proton.core.accountmanager.presentation.compose.AccountPrimaryState
import me.proton.core.accountmanager.presentation.compose.rememberAccountPrimaryState
import me.proton.core.compose.theme.ProtonDimens.DefaultSpacing
import me.proton.core.compose.theme.ProtonDimens.SmallSpacing
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.pass.presentation.R
import me.proton.core.pass.presentation.components.navigation.AuthNavigation

@Stable
data class NavDrawerNavigation(
    val onNavHome: () -> Unit,
    val onNavSettings: () -> Unit,
    val onNavTrash: () -> Unit,
    val onNavHelp: () -> Unit
)

@Composable
fun ModalNavigationDrawer(
    drawerUiState: DrawerUiState,
    drawerState: DrawerState,
    navDrawerNavigation: NavDrawerNavigation,
    authNavigation: AuthNavigation,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    onSignOutClick: () -> Unit,
    signOutDialog: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    ModalDrawer(
        drawerState = drawerState,
        drawerContent = {
            NavigationDrawer(
                drawerUiState = drawerUiState,
                authNavigation = authNavigation,
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
    authNavigation: AuthNavigation,
    onSignOutClick: () -> Unit = {},
    onCloseDrawer: () -> Unit
) {
    val sidebarColors = requireNotNull(ProtonTheme.colors.sidebarColors)
    ProtonTheme(colors = sidebarColors) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = ProtonTheme.colors.backgroundNorm
        ) {
            Column(modifier) {
                if (drawerUiState.currentUser != null) {
                    AccountPrimaryItem(
                        modifier = Modifier
                            .background(sidebarColors.backgroundNorm)
                            .padding(all = SmallSpacing)
                            .fillMaxWidth(),
                        onRemove = { authNavigation.onRemove(it) },
                        onSignIn = { authNavigation.onSignIn(it) },
                        onSignOut = { authNavigation.onSignOut(it) },
                        onSwitch = { authNavigation.onSwitch(it) },
                        viewState = accountPrimaryState
                    )
                }
                Column(
                    modifier = Modifier
                        .padding(top = DefaultSpacing)
                        .verticalScroll(rememberScrollState())
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.Top
                ) {
                    ItemsListItem(
                        isSelected = drawerUiState.selectedSection == NavigationDrawerSection.Items,
                        closeDrawerAction = { onCloseDrawer() },
                        onClick = { navDrawerNavigation.onNavHome() }
                    )
                    SettingsListItem(
                        isSelected = drawerUiState.selectedSection == NavigationDrawerSection.Settings,
                        closeDrawerAction = { onCloseDrawer() },
                        onClick = { navDrawerNavigation.onNavSettings() }
                    )
                    TrashListItem(
                        isSelected = drawerUiState.selectedSection == NavigationDrawerSection.Trash,
                        closeDrawerAction = { onCloseDrawer() },
                        onClick = { navDrawerNavigation.onNavTrash() }
                    )
                    HelpListItem(
                        isSelected = drawerUiState.selectedSection == NavigationDrawerSection.Help,
                        closeDrawerAction = { onCloseDrawer() },
                        onClick = { navDrawerNavigation.onNavHelp() }
                    )
                    SignOutListItem(
                        closeDrawerAction = { onCloseDrawer() },
                        onClick = { onSignOutClick() }
                    )
                    NavigationDrawerAppVersion(
                        name = stringResource(id = drawerUiState.appNameResId),
                        version = drawerUiState.appVersion
                    )
                }
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

@Composable
private fun ItemsListItem(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    closeDrawerAction: () -> Unit,
    onClick: () -> Unit
) {
    NavigationDrawerListItem(
        title = R.string.navigation_item_items,
        icon = R.drawable.ic_proton_key,
        isSelected = isSelected,
        closeDrawerAction = closeDrawerAction,
        modifier = modifier,
        onClick = onClick
    )
}

@Composable
private fun SettingsListItem(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    closeDrawerAction: () -> Unit,
    onClick: () -> Unit
) {
    NavigationDrawerListItem(
        title = R.string.navigation_item_settings,
        icon = R.drawable.ic_settings,
        isSelected = isSelected,
        closeDrawerAction = closeDrawerAction,
        modifier = modifier,
        onClick = onClick
    )
}

@Composable
private fun TrashListItem(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    closeDrawerAction: () -> Unit,
    onClick: () -> Unit
) {
    NavigationDrawerListItem(
        title = R.string.navigation_item_trash,
        icon = R.drawable.ic_proton_trash,
        isSelected = isSelected,
        closeDrawerAction = closeDrawerAction,
        modifier = modifier,
        onClick = onClick
    )
}

@Composable
private fun HelpListItem(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    closeDrawerAction: () -> Unit,
    onClick: () -> Unit
) {
    NavigationDrawerListItem(
        title = R.string.navigation_item_help,
        icon = R.drawable.ic_proton_question_circle,
        isSelected = isSelected,
        closeDrawerAction = closeDrawerAction,
        modifier = modifier,
        onClick = onClick
    )
}

@Composable
private fun SignOutListItem(
    modifier: Modifier = Modifier,
    closeDrawerAction: () -> Unit,
    onClick: () -> Unit
) {
    NavigationDrawerListItem(
        icon = R.drawable.ic_sign_out,
        title = R.string.navigation_item_sign_out,
        closeDrawerAction = closeDrawerAction,
        modifier = modifier,
        isSelected = false,
        onClick = onClick
    )
}

@Suppress("LongParameterList")
@Composable
fun NavigationDrawerListItem(
    @DrawableRes icon: Int,
    @StringRes title: Int,
    closeDrawerAction: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerListItem(icon, title, modifier, isSelected) {
        closeDrawerAction()
        onClick()
    }
}
