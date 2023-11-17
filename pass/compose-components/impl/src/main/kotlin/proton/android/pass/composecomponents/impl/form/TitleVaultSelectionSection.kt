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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.ShareIcon

@Composable
fun TitleVaultSelectionSection(
    modifier: Modifier = Modifier,
    showVaultSelector: Boolean,
    vaultName: String?,
    vaultColor: ShareColor?,
    vaultIcon: ShareIcon?,
    titleValue: String,
    onTitleRequiredError: Boolean,
    enabled: Boolean = true,
    onTitleChanged: (String) -> Unit,
    onVaultClicked: () -> Unit
) {

    Column(
        modifier = modifier.roundedContainerNorm()
    ) {
        if (showVaultSelector) {
            VaultSelector(
                vaultName = vaultName ?: "",
                color = vaultColor ?: ShareColor.Color1,
                icon = vaultIcon ?: ShareIcon.Icon1,
                onVaultClicked = onVaultClicked
            )
            Divider(color = PassTheme.colors.inputBorderNorm)
        }
        TitleSection(
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 4.dp, bottom = 16.dp),
            value = titleValue,
            requestFocus = true,
            onTitleRequiredError = onTitleRequiredError,
            enabled = enabled,
            isRounded = true,
            onChange = onTitleChanged
        )
    }
}

@Preview
@Composable
fun TitleVaultSelectionSectionPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    PassTheme(isDark = input.first) {
        Surface {
            TitleVaultSelectionSection(
                showVaultSelector = input.second,
                vaultName = "Test vault",
                titleValue = "Some title",
                enabled = true,
                onTitleChanged = {},
                onTitleRequiredError = false,
                onVaultClicked = {},
                vaultColor = ShareColor.Color1,
                vaultIcon = ShareIcon.Icon1
            )
        }
    }
}
