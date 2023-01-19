package proton.android.pass.featurevault.impl

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.default
import proton.android.pass.commonuimodels.api.ShareUiModel
import proton.pass.domain.ShareId

@Composable
fun MigrateVaultItem(
    modifier: Modifier = Modifier,
    share: ShareUiModel,
    onVaultSelect: (ShareId) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onVaultSelect(share.id) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            modifier = Modifier.weight(1f),
            text = share.name,
            style = ProtonTheme.typography.default
        )

    }
}
