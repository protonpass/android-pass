package proton.android.pass.featurevault.impl

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.default
import me.proton.core.presentation.R
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonuimodels.api.ShareUiModel
import proton.pass.domain.ShareId

@Composable
fun VaultItem(
    modifier: Modifier = Modifier,
    share: ShareUiModel,
    isSelected: Boolean,
    onVaultSelect: (ShareUiModel) -> Unit,
    onVaultEdit: (ShareUiModel) -> Unit,
    onVaultDelete: (ShareUiModel) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onVaultSelect(share) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelected) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_proton_checkmark),
                contentDescription = ""
            )
            Spacer(modifier = Modifier.width(16.dp))
        } else {
            Spacer(modifier = Modifier.width(40.dp))
        }

        Text(
            modifier = Modifier.weight(1f),
            text = share.name,
            style = ProtonTheme.typography.default
        )
        Spacer(modifier = Modifier.width(16.dp))
        IconButton(
            onClick = { onVaultEdit(share) },
            modifier = Modifier
                .size(24.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_proton_pencil),
                contentDescription = null,
                tint = ProtonTheme.colors.iconNorm
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        IconButton(
            onClick = { onVaultDelete(share) },
            modifier = Modifier.size(24.dp),
            enabled = !isSelected
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_proton_trash),
                contentDescription = null,
                tint = if (!isSelected) {
                    ProtonTheme.colors.iconNorm
                } else {
                    ProtonTheme.colors.iconDisabled
                }
            )
        }
    }
}

@Preview
@Composable
fun VaultItemPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    ProtonTheme(isDark = isDark) {
        Surface {
            VaultItem(
                share = ShareUiModel(
                    id = ShareId(""),
                    name = "Vault name"
                ),
                isSelected = true,
                onVaultSelect = {},
                onVaultEdit = {},
                onVaultDelete = {}
            )
        }
    }
}
