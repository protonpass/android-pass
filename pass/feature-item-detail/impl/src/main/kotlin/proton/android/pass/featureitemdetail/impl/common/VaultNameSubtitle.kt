package proton.android.pass.featureitemdetail.impl.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTypography
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.extension.toSmallResource
import proton.pass.domain.ShareColor
import proton.pass.domain.ShareIcon
import proton.pass.domain.ShareId
import proton.pass.domain.Vault

@Composable
fun VaultNameSubtitle(
    modifier: Modifier = Modifier,
    vault: Vault?
) {
    if (vault == null) return

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            modifier = Modifier.height(12.dp),
            painter = painterResource(vault.icon.toSmallResource()),
            contentDescription = stringResource(R.string.vault_icon_content_description),
            tint = PassTheme.colors.textWeak
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = vault.name,
            style = PassTypography.body3Regular,
            color = PassTheme.colors.textWeak
        )
    }
}

@Preview
@Composable
fun VaultNameSubtitlePreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            VaultNameSubtitle(
                vault = Vault(
                    shareId = ShareId("123"),
                    name = "Vault Name",
                    color = ShareColor.Color1,
                    icon = ShareIcon.Icon1,
                    isPrimary = false
                )
            )
        }
    }
}
