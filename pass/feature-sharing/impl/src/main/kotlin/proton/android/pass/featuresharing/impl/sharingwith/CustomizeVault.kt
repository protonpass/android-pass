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

package proton.android.pass.featuresharing.impl.sharingwith

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.buttons.CircleButton
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.extension.toColor
import proton.android.pass.composecomponents.impl.extension.toResource
import proton.android.pass.composecomponents.impl.icon.VaultIcon
import proton.android.pass.featuresharing.impl.R
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault

@Composable
fun CustomizeVault(
    modifier: Modifier = Modifier,
    vault: Vault,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .roundedContainerNorm()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            VaultIcon(
                backgroundColor = vault.color.toColor(isBackground = true),
                iconColor = vault.color.toColor(isBackground = false),
                icon = vault.icon.toResource()
            )

            Text(
                text = vault.name,
                style = ProtonTheme.typography.defaultSmallNorm,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        CircleButton(
            modifier = Modifier.padding(horizontal = 8.dp),
            color = PassTheme.colors.interactionNormMinor1,
            elevation = ButtonDefaults.elevation(0.dp),
            onClick = onClick
        ) {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(12.dp),
                text = stringResource(R.string.sharing_with_customize_vault_action),
                color = PassTheme.colors.interactionNormMajor2,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview
@Composable
fun CustomizeVaultPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            CustomizeVault(
                vault = Vault(
                    shareId = ShareId("1234"),
                    name = "Vault name",
                ),
                onClick = {}
            )
        }
    }
}
