package proton.android.pass.featurehome.impl.vault

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
import kotlinx.collections.immutable.ImmutableList
import proton.android.pass.commonui.api.PassPalette
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTypography
import proton.android.pass.commonuimodels.api.ShareUiModelWithItemCount
import proton.android.pass.composecomponents.impl.icon.TrashVaultIcon
import proton.android.pass.composecomponents.impl.icon.VaultIcon
import proton.android.pass.featurehome.impl.HomeVaultSelection
import proton.android.pass.featurehome.impl.R
import proton.pass.domain.ShareId

@Composable
fun VaultDrawerSection(
    modifier: Modifier = Modifier,
    homeVaultSelection: HomeVaultSelection,
    list: ImmutableList<ShareUiModelWithItemCount>,
    totalTrashedItems: Long,
    onVaultOptionsClick: (ShareUiModelWithItemCount) -> Unit,
    onAllVaultsClick: () -> Unit,
    onVaultClick: (ShareId) -> Unit = {},
    onTrashClick: () -> Unit = {}
) {
    LazyColumn(
        modifier = modifier
            .fillMaxHeight()
            .padding(horizontal = 16.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.vault_drawer_vaults_title),
                color = PassTheme.colors.textNorm,
                style = PassTypography.hero
            )
        }
        if (list.size > 1) {
            item {
                VaultDrawerRow(
                    name = stringResource(R.string.vault_drawer_all_vaults),
                    itemCount = list.sumOf { it.activeItemCount },
                    icon = {
                        VaultIcon(
                            backgroundColor = PassPalette.Purple16,
                            icon = me.proton.core.presentation.R.drawable.ic_proton_house,
                            iconColor = PassPalette.Purple100
                        )
                    },
                    isShared = false,
                    isSelected = homeVaultSelection == HomeVaultSelection.AllVaults,
                    showMenuIcon = false,
                    onClick = { onAllVaultsClick() }
                )
                Divider()
            }
        }

        items(items = list, key = { it.id.id }) { share ->
            VaultDrawerRow(
                name = share.name,
                itemCount = share.activeItemCount,
                icon = {
                    VaultIcon(
                        backgroundColor = PassPalette.Yellow16,
                        icon = me.proton.core.presentation.R.drawable.ic_proton_house,
                        iconColor = PassPalette.Yellow100
                    )
                },
                isShared = false,
                isSelected = homeVaultSelection == HomeVaultSelection.Vault(share.id),
                showMenuIcon = true,
                onOptionsClick = { onVaultOptionsClick(share) },
                onClick = { onVaultClick(share.id) }
            )
            Divider()
        }
        item {
            VaultDrawerRow(
                name = stringResource(R.string.vault_drawer_item_trash),
                itemCount = totalTrashedItems,
                icon = { TrashVaultIcon() },
                isSelected = homeVaultSelection == HomeVaultSelection.Trash,
                isShared = false,
                showMenuIcon = false,
                onOptionsClick = {},
                onClick = onTrashClick
            )
        }
    }
}

