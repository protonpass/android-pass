package proton.android.pass.featuresettings.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.captionWeak
import me.proton.core.compose.theme.defaultSmallWeak
import proton.android.pass.common.api.Option
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.VaultSelector
import proton.pass.domain.ShareColor
import proton.pass.domain.ShareIcon
import proton.pass.domain.Vault
import proton.android.pass.composecomponents.impl.R as PassR

@Composable
fun PrimaryVaultSection(
    modifier: Modifier = Modifier,
    primaryVault: Option<Vault>,
    onPrimaryVaultClick: () -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(R.string.settings_primary_vault_section_title),
            style = ProtonTheme.typography.defaultSmallWeak,
        )
        Box(modifier = Modifier.roundedContainerNorm()) {
            VaultSelector(
                selectorTitle = stringResource(R.string.settings_primary_vault_vault_selector_title),
                vaultName = primaryVault.map { it.name }.value() ?: "",
                color = primaryVault.map { it.color }.value() ?: ShareColor.Color1,
                icon = primaryVault.map { it.icon }.value() ?: ShareIcon.Icon1,
                trailingIcon = {
                    Icon(
                        painter = painterResource(PassR.drawable.ic_chevron_tiny_right),
                        contentDescription = stringResource(PassR.string.setting_option_icon_content_description),
                        tint = PassTheme.colors.textHint
                    )
                },
                onVaultClicked = onPrimaryVaultClick
            )
        }
        Text(
            text = stringResource(R.string.settings_primary_vault_section_subtitle),
            style = ProtonTheme.typography.captionWeak.copy(PassTheme.colors.textWeak)
        )
    }
}
