package me.proton.pass.presentation.components.navigation.drawer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.presentation.R

@Composable
fun PassNavigationDrawer(
    modifier: Modifier = Modifier,
    drawerUiState: DrawerUiState,
    navDrawerNavigation: NavDrawerNavigation,
    onSignOutClick: () -> Unit = {},
    onCloseDrawer: () -> Unit
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top
    ) {
        DrawerItemTypeSection(
            itemCount = drawerUiState.itemCountSummary,
            closeDrawerAction = { onCloseDrawer() },
            selectedSection = drawerUiState.selectedSection,
            onSectionClick = { section ->
                navDrawerNavigation.onNavHome(section)
            }
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
        ReportProblemItem(
            isSelected = false,
            closeDrawerAction = { onCloseDrawer() },
            onClick = { navDrawerNavigation.onBugReport() }
        )
        SignOutListItem(
            closeDrawerAction = { onCloseDrawer() },
            onClick = { onSignOutClick() }
        )
        NavigationDrawerAppVersion(
            name = stringResource(id = drawerUiState.appNameResId),
            version = drawerUiState.appVersion
        )
        if (drawerUiState.internalDrawerEnabled) {
            InternalDrawerItem(
                closeDrawerAction = { onCloseDrawer() },
                onClick = { navDrawerNavigation.onInternalDrawerClick() }
            )
        }
    }
}

@Preview
@Composable
fun PassNavigationDrawerPreview() {
    ProtonTheme(
        colors = requireNotNull(ProtonTheme.colors.sidebarColors)
    ) {
        Surface(color = ProtonTheme.colors.backgroundNorm) {
            PassNavigationDrawer(
                drawerUiState = DrawerUiState(
                    appNameResId = R.string.title_app,
                    appVersion = "1.2.3"
                ),
                navDrawerNavigation = NavDrawerNavigation(
                    onNavHome = {},
                    onNavSettings = {},
                    onNavTrash = {},
                    onBugReport = {},
                    onInternalDrawerClick = {}
                ),
                onSignOutClick = {},
                onCloseDrawer = {}
            )
        }
    }
}
