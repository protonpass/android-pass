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

package proton.android.pass.featurevault.impl.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.extension.toColor
import proton.android.pass.composecomponents.impl.extension.toResource
import proton.android.pass.composecomponents.impl.form.TitleSection
import proton.android.pass.composecomponents.impl.icon.VaultIcon
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun VaultPreviewSection(
    modifier: Modifier = Modifier,
    state: BaseVaultUiState,
    onNameChange: (String) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        VaultIcon(
            backgroundColor = state.color.toColor(true),
            iconColor = state.color.toColor(),
            icon = state.icon.toResource()
        )

        TitleSection(
            value = state.name,
            onTitleRequiredError = state.isTitleRequiredError,
            enabled = state.isLoading == IsLoadingState.NotLoading,
            moveToNextOnEnter = false,
            onChange = onNameChange,
            onDoneClick = {
                keyboardController?.hide()
            }
        )
    }
}

class ThemeVaultPreviewProvider : ThemePairPreviewProvider<BaseVaultUiState>(CreateVaultProvider())

@Preview
@Composable
fun VaultPreviewSectionPreview(
    @PreviewParameter(ThemeVaultPreviewProvider::class) input: Pair<Boolean, BaseVaultUiState>
) {
    PassTheme(isDark = input.first) {
        Surface {
            VaultPreviewSection(
                state = input.second,
                onNameChange = {}
            )
        }
    }
}
