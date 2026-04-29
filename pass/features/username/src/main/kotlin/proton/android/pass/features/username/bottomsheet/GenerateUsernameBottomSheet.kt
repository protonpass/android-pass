/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.features.username.bottomsheet

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.body3Inverted
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetCancelConfirm
import proton.android.pass.composecomponents.impl.buttons.CircleButton
import proton.android.pass.features.username.GenerateUsernameNavigation
import proton.android.pass.features.username.R

@Composable
fun GenerateUsernameBottomSheet(
    modifier: Modifier = Modifier,
    onNavigate: (GenerateUsernameNavigation) -> Unit,
    viewModel: GenerateUsernameViewModel = hiltViewModel()
) = with(viewModel) {
    val state by stateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(state.event) {
        when (state.event) {
            GenerateUsernameEvent.Idle -> Unit

            GenerateUsernameEvent.OnUsernameConfirmed,
            GenerateUsernameEvent.OnUsernameCopied -> {
                onNavigate(GenerateUsernameNavigation.DismissBottomsheet)
            }
        }

        onConsumeEvent(state.event)
    }

    GenerateUsernameBottomSheetContent(
        modifier = modifier,
        state = state,
        onEvent = { uiEvent ->
            when (uiEvent) {
                is GenerateUsernameUiEvent.OnWordCountChange ->
                    onChangeConfig(state.config.copy(wordCount = uiEvent.value))

                is GenerateUsernameUiEvent.OnIncludeNumbersChange ->
                    onChangeConfig(state.config.copy(includeNumbers = uiEvent.value))

                is GenerateUsernameUiEvent.OnCapitaliseChange ->
                    onChangeConfig(state.config.copy(capitalise = uiEvent.value))

                is GenerateUsernameUiEvent.OnLeetspeakChange ->
                    onChangeConfig(state.config.copy(leetspeak = uiEvent.value))

                is GenerateUsernameUiEvent.OnSeparatorChange ->
                    onNavigate(GenerateUsernameNavigation.OnSelectWordSeparator)

                is GenerateUsernameUiEvent.OnWordTypesChange ->
                    onChangeConfig(state.config.copy(wordTypes = uiEvent.value))

                GenerateUsernameUiEvent.OnRegenerate -> onRegenerate()
                GenerateUsernameUiEvent.OnCopy -> onCopy()
                GenerateUsernameUiEvent.OnConfirm -> onConfirm()
                GenerateUsernameUiEvent.OnCancel ->
                    onNavigate(GenerateUsernameNavigation.DismissBottomsheet)
            }
        },
        buttonSection = {
            when (state.mode) {
                GenerateUsernameMode.CopyAndClose -> {
                    CircleButton(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(14.dp),
                        color = PassTheme.colors.loginInteractionNormMajor1,
                        elevation = ButtonDefaults.elevation(0.dp),
                        onClick = ::onCopy
                    ) {
                        Text(
                            text = stringResource(R.string.generate_username_button_copy),
                            style = PassTheme.typography.body3Inverted(),
                            color = PassTheme.colors.textInvert
                        )
                    }
                }

                GenerateUsernameMode.CancelConfirm -> {
                    BottomSheetCancelConfirm(
                        modifier = Modifier.fillMaxWidth(),
                        onCancel = { onNavigate(GenerateUsernameNavigation.DismissBottomsheet) },
                        onConfirm = ::onConfirm
                    )
                }
            }
        }
    )
}
