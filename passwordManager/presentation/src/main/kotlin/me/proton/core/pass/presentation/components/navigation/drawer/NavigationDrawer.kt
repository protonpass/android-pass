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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DrawerState
import androidx.compose.material.DrawerValue
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.presentation.view.AccountPrimaryView
import me.proton.core.compose.component.NavigationDrawerAppVersion
import me.proton.core.compose.component.NavigationDrawerSectionHeader
import me.proton.core.compose.theme.DefaultSpacing
import me.proton.core.compose.theme.ProtonNavigationDrawerTheme
import me.proton.core.compose.theme.SmallSpacing
import me.proton.core.compose.component.NavigationDrawerListItem
import me.proton.core.pass.presentation.components.user.PREVIEW_USER
import me.proton.core.pass.presentation.components.user.UserSelector
import me.proton.core.pass.presentation.R

@Composable
fun NavigationDrawer(
    accountPrimaryView: AccountPrimaryView,
    drawerState: DrawerState,
    viewState: NavigationDrawerViewState,
    viewEvent: NavigationDrawerViewEvent,
    modifier: Modifier = Modifier,
) {
    MaterialTheme {
        val scope = rememberCoroutineScope()
        val closeDrawerAction: (() -> Unit) -> Unit = remember(viewState.closeOnActionEnabled, drawerState) {
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
            color = MaterialTheme.colors.background
        ) {
            Column(modifier) {
                if (viewState.currentUser != null) {
                    AndroidView(
                        factory = {
                            accountPrimaryView
                        },
                        Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                    )
                }
                Column(
                    modifier = Modifier
                        .padding(top = DefaultSpacing)
                        .verticalScroll(rememberScrollState())
                        .weight(1f),
                    verticalArrangement = Arrangement.Top
                ) {

                    NavigationDrawerSectionHeader(title = R.string.navigation_more_section_header)

                    SettingsListItem(closeDrawerAction, viewEvent)

                    ReportBugListItem(closeDrawerAction, viewEvent)

                    SignOutListItem(closeDrawerAction, viewEvent)

                    if (viewState.currentUser != null) {
                        NavigationDrawerSectionHeader(
                            title = stringResource(R.string.navigation_storage_section_header),
                            modifier = Modifier.padding(bottom = SmallSpacing)
                        )
                    }

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
private fun ReportBugListItem(
    closeDrawerAction: (() -> Unit) -> Unit,
    viewEvent: NavigationDrawerViewEvent,
    modifier: Modifier = Modifier,
) {
    NavigationDrawerListItem(
        title = R.string.navigation_item_bug_report,
        icon = R.drawable.ic_bug,
        closeDrawerAction = closeDrawerAction,
        modifier = modifier,
    ) {
        viewEvent.onBugReport()
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

@Preview(name = "Drawer opened")
@Composable
fun PreviewDrawerWithUser() {
    ProtonNavigationDrawerTheme {
        NavigationDrawer(
            accountPrimaryView = AccountPrimaryView(LocalContext.current),
            drawerState = DrawerState(DrawerValue.Open) { true },
            viewState = NavigationDrawerViewState(
                R.string.title_app,
                "Version",
                currentUser = PREVIEW_USER
            ),
            viewEvent = object : NavigationDrawerViewEvent {
                override val onSettings = {}
                override val onSignOut = {}
                override val onBugReport = {}
            }
        )
    }
}

@Preview(name = "Drawer opened")
@Composable
fun PreviewDrawerWithoutUser() {
    ProtonNavigationDrawerTheme {
        NavigationDrawer(
            accountPrimaryView = AccountPrimaryView(LocalContext.current),
            drawerState = DrawerState(DrawerValue.Open) { true },
            viewState = NavigationDrawerViewState(R.string.title_app, "Version"),
            viewEvent = object : NavigationDrawerViewEvent {
                override val onSettings = {}
                override val onSignOut = {}
                override val onBugReport = {}
            }
        )
    }
}
