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

package proton.android.pass.features.sharing.manage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.domain.entity.UserId
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.heroNorm
import proton.android.pass.composecomponents.impl.extension.toColor
import proton.android.pass.composecomponents.impl.extension.toResource
import proton.android.pass.composecomponents.impl.icon.VaultIcon
import proton.android.pass.domain.ShareFlags
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import proton.android.pass.domain.VaultId
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.features.sharing.R
import java.util.Date

@Composable
fun ManageVaultHeader(modifier: Modifier = Modifier, vault: VaultWithItemCount?) {
    if (vault == null) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VaultIcon(
            size = 64,
            iconSize = 32,
            backgroundColor = vault.vault.color.toColor(isBackground = true),
            iconColor = vault.vault.color.toColor(isBackground = false),
            icon = vault.vault.icon.toResource()
        )

        Text(
            text = vault.vault.name,
            color = PassTheme.colors.textNorm,
            style = PassTheme.typography.heroNorm(),
            textAlign = TextAlign.Center
        )

        Text(
            text = pluralStringResource(
                R.plurals.share_manage_vault_item_count,
                vault.activeItemCount.toInt(),
                vault.activeItemCount
            ),
            color = PassTheme.colors.textWeak
        )
    }
}

@Preview
@Composable
fun ManageVaultHeaderPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            ManageVaultHeader(
                vault = VaultWithItemCount(
                    vault = Vault(
                        userId = UserId(id = ""),
                        shareId = ShareId("123"),
                        vaultId = VaultId("123"),
                        name = "Vault name",
                        createTime = Date(),
                        shareFlags = ShareFlags(0)
                    ),
                    activeItemCount = 3,
                    trashedItemCount = 0
                )
            )
        }
    }
}
