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

package proton.android.pass.featuresharing.impl.sharingsummary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.body3Weak
import proton.android.pass.composecomponents.impl.extension.toColor
import proton.android.pass.composecomponents.impl.extension.toResource
import proton.android.pass.composecomponents.impl.icon.VaultIcon
import proton.android.pass.featuresharing.impl.R
import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.ShareIcon
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import proton.android.pass.domain.VaultWithItemCount

@Composable
fun VaultRowSection(modifier: Modifier = Modifier, vaultWithItemCount: VaultWithItemCount?) {
    vaultWithItemCount ?: return
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(R.string.share_summary_vault_title),
            style = PassTheme.typography.body3Weak()
        )
        VaultRow(
            name = vaultWithItemCount.vault.name,
            itemCount = vaultWithItemCount.activeItemCount + vaultWithItemCount.trashedItemCount,
            icon = {
                VaultIcon(
                    backgroundColor = vaultWithItemCount.vault.color.toColor(isBackground = true),
                    icon = vaultWithItemCount.vault.icon.toResource(),
                    iconColor = vaultWithItemCount.vault.color.toColor()
                )
            }
        )
    }
}

@Preview
@Composable
fun VaultRowSectionPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            VaultRowSection(
                vaultWithItemCount = VaultWithItemCount(
                    vault = Vault(
                        shareId = ShareId("id"),
                        name = "Vault name",
                        color = ShareColor.Color1,
                        icon = ShareIcon.Icon1,
                    ),
                    activeItemCount = 1,
                    trashedItemCount = 2
                )
            )
        }
    }
}
