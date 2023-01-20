package proton.android.pass.presentation.navigation.drawer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.presentation.R
import proton.android.pass.commonuimodels.api.ShareUiModel

@Composable
fun DrawerVaultSection(
    modifier: Modifier = Modifier,
    shares: List<ShareUiModel>,
    selectedSection: NavigationDrawerSection?,
    closeDrawerAction: () -> Unit,
    onVaultClick: (SelectedVaults) -> Unit
) {
    Column(modifier = modifier) {
        NavigationDrawerListItem(
            title = stringResource(R.string.navigation_all_vaults),
            closeDrawerAction = closeDrawerAction,
            isSelected = selectedSection is ItemTypeSection && selectedSection.shareId == null,
            onClick = { onVaultClick(SelectedVaults.AllVaults) },
            startContent = {
                Icon(
                    painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_list_bullets),
                    contentDescription = null,
                    tint = ProtonTheme.colors.iconWeak
                )
            },
            endContent = {}
        )
        shares.forEach { share ->
            NavigationDrawerListItem(
                title = share.name,
                closeDrawerAction = closeDrawerAction,
                isSelected = selectedSection is ItemTypeSection && selectedSection.shareId == share.id,
                onClick = { onVaultClick(SelectedVaults.Vault(share.id)) },
                startContent = {
                    Spacer(modifier = Modifier.width(horizontalSpacerWidth))
                    Icon(
                        painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_vault),
                        contentDescription = null,
                        tint = ProtonTheme.colors.iconWeak
                    )
                },
                endContent = {}
            )
        }
    }
}
