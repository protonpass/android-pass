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

package proton.android.pass.features.sharing.sharefromitem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.domain.entity.UserId
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.body3Weak
import proton.android.pass.composecomponents.impl.buttons.CircleButton
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
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
internal fun ShareThisVaultRow(
    modifier: Modifier = Modifier,
    vault: VaultWithItemCount,
    onShareClick: () -> Unit
) {
    val itemCount = remember {
        vault.activeItemCount + vault.trashedItemCount
    }

    Row(
        modifier = modifier
            .roundedContainerNorm()
            .padding(Spacing.medium)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        VaultIcon(
            backgroundColor = vault.vault.color.toColor(isBackground = true),
            icon = vault.vault.icon.toResource(),
            iconColor = vault.vault.color.toColor()
        )

        Column(
            modifier = Modifier.weight(weight = 1f)
        ) {
            Text(
                text = vault.vault.name,
                style = ProtonTheme.typography.body2Regular,
                color = PassTheme.colors.textNorm,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = pluralStringResource(
                    R.plurals.sharing_item_count,
                    itemCount.toInt(),
                    itemCount.toInt()
                ),
                style = PassTheme.typography.body3Weak(),
                color = PassTheme.colors.textWeak
            )
        }

        CircleButton(
            modifier = Modifier.padding(Spacing.none, Spacing.extraSmall),
            color = PassTheme.colors.interactionNormMinor1,
            elevation = ButtonDefaults.elevation(0.dp),
            onClick = onShareClick
        ) {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(Spacing.mediumSmall),
                text = stringResource(R.string.sharing_from_item_share_this_vault_action),
                style = ProtonTheme.typography.body2Regular,
                color = PassTheme.colors.interactionNormMajor2,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview
@Composable
internal fun ShareThisVaultRowPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            ShareThisVaultRow(
                vault = VaultWithItemCount(
                    vault = Vault(
                        userId = UserId(id = ""),
                        shareId = ShareId("Share"),
                        vaultId = VaultId("123"),
                        name = "Test vault with a very very long name",
                        createTime = Date(),
                        shareFlags = ShareFlags(0)
                    ),
                    activeItemCount = 1,
                    trashedItemCount = 1
                ),
                onShareClick = {}
            )
        }
    }
}
