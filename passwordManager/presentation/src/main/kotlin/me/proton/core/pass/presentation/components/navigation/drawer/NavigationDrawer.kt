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

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.presentation.compose.AccountPrimaryItem
import me.proton.core.accountmanager.presentation.compose.AccountPrimaryState
import me.proton.core.accountmanager.presentation.compose.rememberAccountPrimaryState
import me.proton.core.compose.theme.ProtonDimens.DefaultSpacing
import me.proton.core.compose.theme.ProtonDimens.SmallSpacing
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.domain.entity.UserId
import me.proton.core.pass.presentation.R

enum class NavigationDrawerSection { Items, Settings, Trash, Help }

interface NavDrawerNavigation {
    val selectedSection: NavigationDrawerSection
    val onDrawerStateChanged: (Boolean) -> Unit
    val onSignIn: (UserId?) -> Unit
    val onSignOut: (UserId) -> Unit
    val onRemove: (UserId?) -> Unit
    val onSwitch: (UserId) -> Unit
    val onSectionSelected: (NavigationDrawerSection) -> Unit
}

@Composable
fun NavigationDrawer(
    drawerState: DrawerState,
    viewState: NavigationDrawerViewState,
    modifier: Modifier = Modifier,
    accountPrimaryState: AccountPrimaryState = rememberAccountPrimaryState(),
    navigation: NavDrawerNavigation
) {
    val sidebarColors = requireNotNull(ProtonTheme.colors.sidebarColors)
    ProtonTheme(colors = sidebarColors) {
        val scope = rememberCoroutineScope()
        val closeDrawerAction: (() -> Unit) -> Unit =
            remember(viewState.closeOnActionEnabled, drawerState) {
                if (viewState.closeOnBackEnabled) {
                    { onClose ->
                        scope.launch {
                            if (drawerState.isOpen) drawerState.close()
                            onClose()
                        }
                    }
                } else {
                    { onClose -> onClose() }
                }
            }
        BackHandler(enabled = viewState.closeOnBackEnabled && drawerState.isOpen) {
            scope.launch { drawerState.close() }
        }
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = ProtonTheme.colors.backgroundNorm
        ) {
            Column(modifier) {
                if (viewState.currentUser != null) {
                    AccountPrimaryItem(
                        modifier = Modifier
                            .background(sidebarColors.backgroundNorm)
                            .padding(all = SmallSpacing)
                            .fillMaxWidth(),
                        onRemove = { navigation.onRemove(it) },
                        onSignIn = { navigation.onSignIn(it) },
                        onSignOut = { navigation.onSignOut(it) },
                        onSwitch = { navigation.onSwitch(it) },
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
                    ItemsListItem(navigation, closeDrawerAction)
                    SettingsListItem(navigation, closeDrawerAction)
                    TrashListItem(navigation, closeDrawerAction)
                    HelpListItem(navigation, closeDrawerAction)
                    SignOutListItem(navigation, closeDrawerAction)

                    NavigationDrawerAppVersion(
                        name = stringResource(id = viewState.appNameResId),
                        version = viewState.appVersion
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
                Icon(painterResource(R.drawable.ic_proton_vault), contentDescription = null, tint = ProtonTheme.colors.iconWeak)
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
    navigation: NavDrawerNavigation,
    closeDrawerAction: (() -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationDrawerListItem(
        title = R.string.navigation_item_items,
        icon = R.drawable.ic_proton_key,
        isSelected = navigation.selectedSection == NavigationDrawerSection.Items,
        closeDrawerAction = closeDrawerAction,
        modifier = modifier
    ) {
        navigation.onSectionSelected(NavigationDrawerSection.Items)
    }
}

@Composable
private fun SettingsListItem(
    navigation: NavDrawerNavigation,
    closeDrawerAction: (() -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationDrawerListItem(
        title = R.string.navigation_item_settings,
        icon = R.drawable.ic_settings,
        isSelected = navigation.selectedSection == NavigationDrawerSection.Settings,
        closeDrawerAction = closeDrawerAction,
        modifier = modifier
    ) {
        navigation.onSectionSelected(NavigationDrawerSection.Settings)
    }
}

@Composable
private fun TrashListItem(
    navigation: NavDrawerNavigation,
    closeDrawerAction: (() -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationDrawerListItem(
        title = R.string.navigation_item_trash,
        icon = R.drawable.ic_proton_trash,
        isSelected = navigation.selectedSection == NavigationDrawerSection.Trash,
        closeDrawerAction = closeDrawerAction,
        modifier = modifier
    ) {
        navigation.onSectionSelected(NavigationDrawerSection.Trash)
    }
}

@Composable
private fun HelpListItem(
    navigation: NavDrawerNavigation,
    closeDrawerAction: (() -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationDrawerListItem(
        title = R.string.navigation_item_help,
        icon = R.drawable.ic_proton_question_circle,
        isSelected = navigation.selectedSection == NavigationDrawerSection.Help,
        closeDrawerAction = closeDrawerAction,
        modifier = modifier
    ) {
        navigation.onSectionSelected(NavigationDrawerSection.Help)
    }
}

@Composable
private fun SignOutListItem(
    navigation: NavDrawerNavigation,
    closeDrawerAction: (() -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationDrawerListItem(
        icon = R.drawable.ic_sign_out,
        title = R.string.navigation_item_sign_out,
        closeDrawerAction = closeDrawerAction,
        modifier = modifier,
        isSelected = false
    ) {
        navigation.onSignOut
    }
}

@Composable
fun NavigationDrawerListItem(
    @DrawableRes icon: Int,
    @StringRes title: Int,
    closeDrawerAction: (() -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerListItem(icon, title, modifier, isSelected) {
        closeDrawerAction(onClick)
    }
}
