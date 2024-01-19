/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

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
import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.ShareIcon
import proton.android.pass.domain.Vault
import proton.android.pass.composecomponents.impl.R as PassR

@Composable
fun DefaultVaultSection(
    modifier: Modifier = Modifier,
    defaultVault: Option<Vault>,
    onEvent: (SettingsContentEvent) -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(R.string.settings_default_vault_section_title),
            style = ProtonTheme.typography.defaultSmallWeak,
        )
        Box(modifier = Modifier.roundedContainerNorm()) {
            VaultSelector(
                selectorTitle = stringResource(R.string.settings_default_vault_vault_selector_title),
                vaultName = defaultVault.map { it.name }.value() ?: "",
                color = defaultVault.map { it.color }.value() ?: ShareColor.Color1,
                icon = defaultVault.map { it.icon }.value() ?: ShareIcon.Icon1,
                trailingIcon = {
                    Icon(
                        painter = painterResource(PassR.drawable.ic_chevron_tiny_right),
                        contentDescription = null,
                        tint = PassTheme.colors.textHint
                    )
                },
                onVaultClicked = { onEvent(SettingsContentEvent.DefaultVault) }
            )
        }
        Text(
            text = stringResource(R.string.settings_default_vault_section_subtitle),
            style = ProtonTheme.typography.captionWeak.copy(PassTheme.colors.textWeak)
        )
    }
}
