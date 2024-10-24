/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.password.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.commonui.api.body3Inverted
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.buttons.CircleButton
import proton.android.pass.features.password.R

@Composable
internal fun GeneratePasswordBottomSheetContent(
    modifier: Modifier = Modifier,
    state: GeneratePasswordUiState,
    onEvent: (GeneratePasswordEvent) -> Unit,
    buttonSection: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .bottomSheet(horizontalPadding = PassTheme.dimens.bottomsheetHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(Spacing.mediumSmall)
    ) {
        GeneratePasswordBottomSheetTitle(onRegenerate = {
            onEvent(GeneratePasswordEvent.OnRegeneratePasswordClick)
        })
        GeneratePasswordViewContent(
            state = state,
            onEvent = onEvent
        )
        buttonSection()
    }
}

internal class ThemeAndCreatePasswordUiStateProvider :
    ThemePairPreviewProvider<GeneratePasswordUiState>(GeneratePasswordStatePreviewProvider())

@[Preview Composable]
internal fun GenPasswordBottomSheetContentPreview(
    @PreviewParameter(ThemeAndCreatePasswordUiStateProvider::class)
    input: Pair<Boolean, GeneratePasswordUiState>
) {
    PassTheme(isDark = input.first) {
        Surface {
            GeneratePasswordBottomSheetContent(
                state = input.second,
                onEvent = {},
                buttonSection = {
                    CircleButton(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(14.dp),
                        color = PassTheme.colors.loginInteractionNormMajor1,
                        elevation = ButtonDefaults.elevation(0.dp),
                        onClick = { }
                    ) {
                        Text(
                            text = stringResource(R.string.generate_password_copy),
                            style = PassTheme.typography.body3Inverted(),
                            color = PassTheme.colors.textInvert
                        )
                    }
                }
            )
        }
    }
}
