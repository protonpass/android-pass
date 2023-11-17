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

package proton.android.pass.composecomponents.impl.form

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.body3Norm
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.extension.toColor
import proton.android.pass.composecomponents.impl.extension.toResource
import proton.android.pass.composecomponents.impl.icon.VaultIcon
import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.ShareIcon

@Suppress("MagicNumber")
@Composable
fun VaultSelector(
    modifier: Modifier = Modifier,
    vaultName: String,
    color: ShareColor,
    icon: ShareIcon,
    selectorTitle: String = stringResource(R.string.vault_selector_title),
    trailingIcon: @Composable () -> Unit = { ChevronDownIcon() },
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
                text = selectorTitle,
                color = PassTheme.colors.textWeak,
                style = PassTheme.typography.body3Norm()
            )
            Text(
                text = vaultName,
                color = PassTheme.colors.textNorm,
            )
        }
        IconButton(onClick = onVaultClicked) {
            trailingIcon()
        }
    }
}

@Preview
@Composable
fun VaultSelectorPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            VaultSelector(
                vaultName = "Some vault",
                color = ShareColor.Color3,
                icon = ShareIcon.Icon4,
                onVaultClicked = {}
            )
        }
    }
}
