package proton.android.pass.featurehome.impl.vault

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import me.proton.core.compose.theme.ProtonDimens
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTypography
import proton.android.pass.commonuimodels.api.ShareUiModelWithItemCount
import proton.android.pass.composecomponents.impl.buttons.CircleButton
import proton.android.pass.featurehome.impl.HomeVaultSelection
import proton.android.pass.featurehome.impl.R
import proton.pass.domain.ShareId

@Composable
fun VaultDrawerContent(
    modifier: Modifier = Modifier,
    homeVaultSelection: HomeVaultSelection,
    list: ImmutableList<ShareUiModelWithItemCount>,
    totalTrashedItems: Long,
    onAllVaultsClick: () -> Unit,
    onVaultClick: (ShareId) -> Unit,
    onTrashClick: () -> Unit,
    onCreateVaultClick: () -> Unit
) {
    Column(modifier = modifier.background(PassTheme.colors.backgroundNorm)) {
        VaultDrawerSection(
            modifier = Modifier
                .padding(top = ProtonDimens.DefaultSpacing)
                .weight(1f, fill = true),
            homeVaultSelection = homeVaultSelection,
            list = list,
            totalTrashedItems = totalTrashedItems,
            onVaultOptionsClick = {}, // To be implemented
            onAllVaultsClick = onAllVaultsClick,
            onVaultClick = onVaultClick,
            onTrashClick = onTrashClick
        )
        CircleButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            color = PassTheme.colors.accentPurpleWeakest,
            elevation = ButtonDefaults.elevation(0.dp),
            onClick = onCreateVaultClick
        ) {
            Text(
                text = stringResource(R.string.vault_drawer_create_vault),
                color = PassTheme.colors.accentPurpleOpaque,
                style = PassTypography.body3Regular
            )
        }

    }
}
