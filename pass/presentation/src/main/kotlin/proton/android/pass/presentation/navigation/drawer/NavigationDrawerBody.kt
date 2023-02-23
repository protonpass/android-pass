package proton.android.pass.presentation.navigation.drawer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallWeak
import me.proton.pass.presentation.R
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.presentation.components.navigation.drawer.InternalDrawerItem

@Composable
fun NavigationDrawerBody(
    modifier: Modifier = Modifier,
    drawerUiState: DrawerUiState,
    navDrawerNavigation: NavDrawerNavigation,
    onSignOutClick: () -> Unit = {},
    onCloseDrawer: () -> Unit
) {
    var selectedItemTypes: SelectedItemTypes by remember { mutableStateOf(SelectedItemTypes.AllItems) }
    var selectedVaults: SelectedVaults by remember { mutableStateOf(SelectedVaults.AllVaults) }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top
    ) {
        DrawerItemTypeSection(
            itemCount = drawerUiState.itemCountSummary,
            closeDrawerAction = { onCloseDrawer() },
            selectedSection = drawerUiState.selectedSection,
            onSectionClick = {
                selectedItemTypes = it
                navDrawerNavigation.onNavHome(selectedItemTypes, selectedVaults)
            }
        )
        Divider(modifier = Modifier.fillMaxWidth())
        DrawerVaultSection(
            modifier = Modifier,
            shares = drawerUiState.shares,
            selectedSection = drawerUiState.selectedSection,
            closeDrawerAction = { onCloseDrawer() },
            onVaultClick = {
                selectedVaults = it
                navDrawerNavigation.onNavHome(selectedItemTypes, selectedVaults)
            }
        )
        Divider(modifier = Modifier.fillMaxWidth())
        Text(
            modifier = Modifier
                .padding(
                    horizontal = ProtonDimens.DefaultSpacing,
                    vertical = ProtonDimens.SmallSpacing
                ),
            text = stringResource(R.string.navigation_text_more),
            style = ProtonTheme.typography.defaultSmallWeak
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
        InternalDrawerItem(
            closeDrawerAction = { onCloseDrawer() },
            onClick = { navDrawerNavigation.onInternalDrawerClick() }
        )
    }
}

@Preview
@Composable
fun NavigationDrawerBodyPreview() {
    PassTheme(
        protonColors = requireNotNull(ProtonTheme.colors.sidebarColors)
    ) {
        Surface(color = ProtonTheme.colors.backgroundNorm) {
            NavigationDrawerBody(
                drawerUiState = DrawerUiState(
                    appNameResId = R.string.title_app
                ),
                navDrawerNavigation = NavDrawerNavigation(
                    onNavHome = { _, _ -> },
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
