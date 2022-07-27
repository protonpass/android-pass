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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.presentation.compose.AccountPrimaryItem
import me.proton.core.accountmanager.presentation.compose.AccountPrimaryState
import me.proton.core.accountmanager.presentation.compose.rememberAccountPrimaryState
import me.proton.core.compose.theme.ProtonDimens.DefaultSpacing
import me.proton.core.compose.theme.ProtonDimens.ListItemHeight
import me.proton.core.compose.theme.ProtonDimens.ListItemTextStartPadding
import me.proton.core.compose.theme.ProtonDimens.MediumSpacing
import me.proton.core.compose.theme.ProtonDimens.SmallSpacing
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.domain.entity.UserId
import me.proton.core.pass.presentation.R
import me.proton.core.pass.presentation.components.model.ShareUiModel
import me.proton.core.pass.presentation.components.user.PREVIEW_USER

@Composable
fun NavigationDrawer(
    drawerState: DrawerState,
    viewState: NavigationDrawerViewState,
    viewEvent: NavigationDrawerViewEvent,
    modifier: Modifier = Modifier,
    accountPrimaryState: AccountPrimaryState = rememberAccountPrimaryState(),
    shares: List<ShareUiModel> = emptyList(),
    onSignIn: (UserId?) -> Unit = {},
    onSignOut: (UserId) -> Unit = {},
    onRemove: (UserId) -> Unit = {},
    onSwitch: (UserId) -> Unit = {},
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
                        onRemove = onRemove,
                        onSignIn = onSignIn,
                        onSignOut = onSignOut,
                        onSwitch = onSwitch,
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
                    SharesList(closeDrawerAction, viewEvent, shares)
                    SettingsListItem(closeDrawerAction, viewEvent)
                    HelpListItem(closeDrawerAction, viewEvent)
                    SignOutListItem(closeDrawerAction, viewEvent)

                    NavigationDrawerAppVersion(
                        name = stringResource(id = viewState.appNameResId),
                        version = viewState.appVersion
                    )
                }
            }
        }
    }
}

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

@Composable
private fun SettingsListItem(
    closeDrawerAction: (() -> Unit) -> Unit,
    viewEvent: NavigationDrawerViewEvent,
    modifier: Modifier = Modifier,
) {
    NavigationDrawerListItem(
        title = R.string.navigation_item_settings,
        icon = R.drawable.ic_settings,
        closeDrawerAction = closeDrawerAction,
        modifier = modifier,
    ) {
        viewEvent.onSettings()
    }
}

@Composable
private fun HelpListItem(
    closeDrawerAction: (() -> Unit) -> Unit,
    viewEvent: NavigationDrawerViewEvent,
    modifier: Modifier = Modifier,
) {
    NavigationDrawerListItem(
        title = R.string.navigation_item_help,
        icon = R.drawable.ic_proton_question_circle,
        closeDrawerAction = closeDrawerAction,
        modifier = modifier,
    ) {
        viewEvent.onHelp()
    }
}

@Composable
private fun SignOutListItem(
    closeDrawerAction: (() -> Unit) -> Unit,
    viewEvent: NavigationDrawerViewEvent,
    modifier: Modifier = Modifier,
) {
    NavigationDrawerListItem(
        icon = R.drawable.ic_sign_out,
        title = R.string.navigation_item_sign_out,
        closeDrawerAction = closeDrawerAction,
        modifier = modifier,
    ) {
        viewEvent.onSignOut()
    }
}

@Composable
fun NavigationDrawerListItem(
    @DrawableRes icon: Int,
    @StringRes title: Int,
    closeDrawerAction: (() -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    NavigationDrawerListItem(icon, title, modifier) {
        closeDrawerAction(onClick)
    }
}
@Composable
fun NavigationDrawerListItem(
    @DrawableRes icon: Int,
    title: String,
    closeDrawerAction: (() -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    NavigationDrawerListItem(icon, title, modifier) {
        closeDrawerAction(onClick)
    }
}

@Preview(name = "Drawer opened")
@Composable
fun PreviewDrawerWithUser() {
    ProtonTheme {
        NavigationDrawer(
            drawerState = DrawerState(DrawerValue.Open) { true },
            viewState = NavigationDrawerViewState(
                R.string.title_app,
                "Version",
                currentUser = PREVIEW_USER
            ),
            viewEvent = object : NavigationDrawerViewEvent {
                override val onSettings = {}
                override val onSignOut = {}
                override val onHelp = {}
                override val onShareSelected = { _: ShareClickEvent -> }
            }
        )
    }
}

@Preview(name = "Drawer opened")
@Composable
fun PreviewDrawerWithoutUser() {
    ProtonTheme {
        NavigationDrawer(
            drawerState = DrawerState(DrawerValue.Open) { true },
            viewState = NavigationDrawerViewState(R.string.title_app, "Version"),
            viewEvent = object : NavigationDrawerViewEvent {
                override val onSettings = {}
                override val onSignOut = {}
                override val onHelp = {}
                override val onShareSelected = { _: ShareClickEvent -> }
            }
        )
    }
}
