package proton.android.pass.composecomponents.impl.form

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTypography
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.extension.toColor
import proton.android.pass.composecomponents.impl.extension.toResource
import proton.android.pass.composecomponents.impl.icon.VaultIcon
import proton.pass.domain.ShareColor
import proton.pass.domain.ShareIcon

@Suppress("MagicNumber")
@Composable
fun VaultSelector(
    modifier: Modifier = Modifier,
    vaultName: String,
    color: ShareColor,
    icon: ShareIcon,
    onVaultClicked: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onVaultClicked)
            .padding(start = 16.dp, top = 16.dp, end = 4.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        VaultIcon(
            iconColor = color.toColor(),
            backgroundColor = color.toColor(true),
            icon = icon.toResource(),
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = stringResource(R.string.vault_selector_title),
                color = PassTheme.colors.textWeak,
                style = PassTypography.body3Regular
            )
            Text(
                text = vaultName,
                color = PassTheme.colors.textNorm,
            )
        }
        IconButton(onClick = onVaultClicked) {
            Icon(
                painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_chevron_down),
                contentDescription = null,
                tint = ProtonTheme.colors.iconWeak
            )
        }
    }
}
