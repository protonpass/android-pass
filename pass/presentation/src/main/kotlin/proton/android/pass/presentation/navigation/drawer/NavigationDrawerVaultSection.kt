package proton.android.pass.presentation.navigation.drawer

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.pass.presentation.R
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTypography
import proton.android.pass.commonuimodels.api.ShareUiModelWithItemCount

@Composable
fun NavigationDrawerVaultSection(
    modifier: Modifier = Modifier,
    drawerUiState: DrawerUiState,
    navDrawerNavigation: NavDrawerNavigation,
    onVaultOptionsClick: (ShareUiModelWithItemCount) -> Unit,
    onCloseDrawer: () -> Unit = {},
) {
    LazyColumn(
        modifier = modifier.fillMaxHeight().padding(horizontal = 16.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.navdrawer_vaults_title),
                color = PassTheme.colors.textNorm,
                style = PassTypography.hero
            )
        }

        items(items = drawerUiState.shares, key = { it.id.id }) { share ->
            NavigationDrawerVaultRow(
                name = share.name,
                itemCount = share.activeItemCount,
                icon = me.proton.core.presentation.R.drawable.ic_proton_house,
                color = PassTheme.colors.accentYellowNorm,
                isShared = false,
                isSelected = drawerUiState.selectedSection == NavigationDrawerSection.AllItems(share.id),
                showMenuIcon = true,
                onOptionsClick = { onVaultOptionsClick(share) },
                onClick = {
                    navDrawerNavigation.onNavHome(SelectedItemTypes.AllItems, SelectedVaults.Vault(share.id))
                    onCloseDrawer()
                }
            )
            Divider()
        }
        item {
            NavigationDrawerVaultRow(
                name = stringResource(R.string.navigation_item_trash),
                itemCount = drawerUiState.trashedItemCount,
                icon = me.proton.core.presentation.R.drawable.ic_proton_trash,
                color = PassTheme.colors.textDisabled,
                iconColor = PassTheme.colors.textWeak,
                isSelected = drawerUiState.selectedSection == NavigationDrawerSection.Trash,
                isShared = false,
                showMenuIcon = false,
                onOptionsClick = {},
                onClick = {
                    navDrawerNavigation.onNavTrash()
                    onCloseDrawer()
                }
            )
        }
    }
}

