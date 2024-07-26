/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.features.sl.sync.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.asAnnotatedString
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.extension.toColor
import proton.android.pass.composecomponents.impl.extension.toResource
import proton.android.pass.composecomponents.impl.icon.VaultIcon
import proton.android.pass.composecomponents.impl.item.SectionSubtitle
import proton.android.pass.composecomponents.impl.item.SectionTitle
import proton.android.pass.domain.Vault
import proton.android.pass.features.sl.sync.R
import proton.android.pass.features.sl.sync.shared.ui.SimpleLoginSyncDescriptionText
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun SimpleLoginSyncSettingsVaultSection(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    selectedVault: Vault,
) {
    Column(
        modifier = modifier.clickable { onClick() },
        verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
    ) {
        Row(
            modifier = Modifier
                .roundedContainerNorm()
                .fillMaxWidth()
                .padding(all = Spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = Spacing.medium)
        ) {
            VaultIcon(
                backgroundColor = selectedVault.color.toColor(true),
                icon = selectedVault.icon.toResource(),
                iconColor = selectedVault.color.toColor()
            )

            Column(
                modifier = Modifier.weight(weight = 1f),
                verticalArrangement = Arrangement.spacedBy(space = Spacing.extraSmall),
            ) {
                SectionTitle(
                    text = stringResource(id = R.string.simple_login_sync_settings_default_vault_title),
                )

                SectionSubtitle(
                    text = selectedVault.name.asAnnotatedString(),
                )
            }

            Icon(
                painter = painterResource(id = CompR.drawable.ic_chevron_tiny_right),
                contentDescription = null,
                tint = PassTheme.colors.textWeak
            )
        }

        SimpleLoginSyncDescriptionText(
            text = stringResource(id = R.string.simple_login_sync_settings_default_vault_description),
        )
    }
}

@[Preview Composable]
internal fun SimpleLoginSyncSettingsVaultPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
//            SimpleLoginSyncSettingsVault()
        }
    }
}
