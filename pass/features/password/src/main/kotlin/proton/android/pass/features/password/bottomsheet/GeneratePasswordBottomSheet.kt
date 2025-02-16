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
import proton.android.pass.features.password.GeneratePasswordNavigation
import proton.android.pass.features.password.R

@Composable
fun GeneratePasswordBottomSheet(
    modifier: Modifier = Modifier,
    onNavigate: (GeneratePasswordNavigation) -> Unit,
    viewModel: GeneratePasswordViewModel = hiltViewModel()
) = with(viewModel) {
    val state by stateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(state.event) {
        when (state.event) {
            GeneratePasswordEvent.Idle -> Unit

            GeneratePasswordEvent.OnPasswordConfirmed,
            GeneratePasswordEvent.OnPasswordCopied -> {
                onNavigate(GeneratePasswordNavigation.DismissBottomsheet)
            }
        }

        onConsumeEvent(state.event)
    }

    GeneratePasswordBottomSheetContent(
        modifier = modifier,
        state = state,
        onEvent = { uiEvent ->
            when (uiEvent) {
                GeneratePasswordUiEvent.OnPasswordModeChangeClick -> {
                    onNavigate(GeneratePasswordNavigation.OnSelectPasswordMode)
                }

                GeneratePasswordUiEvent.OnWordsSeparatorClick -> {
                    onNavigate(GeneratePasswordNavigation.OnSelectWordSeparator)
                }

                is GeneratePasswordUiEvent.OnPasswordConfigChanged -> {
                    onChangePasswordConfig(uiEvent.config)
                }

                GeneratePasswordUiEvent.OnRegeneratePasswordClick -> {
                    onRegeneratePassword()
                }
            }
        },
        buttonSection = {
            when (state.mode) {
                GeneratePasswordMode.CopyAndClose -> {
                    CircleButton(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(14.dp),
                        color = PassTheme.colors.loginInteractionNormMajor1,
                        elevation = ButtonDefaults.elevation(0.dp),
                        onClick = ::onCopyPassword
                    ) {
                        Text(
                            text = stringResource(R.string.generate_password_copy),
                            style = PassTheme.typography.body3Inverted(),
                            color = PassTheme.colors.textInvert
                        )
                    }
                }

                GeneratePasswordMode.CancelConfirm -> {
                    BottomSheetCancelConfirm(
                        modifier = Modifier.fillMaxWidth(),
                        onCancel = { onNavigate(GeneratePasswordNavigation.DismissBottomsheet) },
                        onConfirm = ::onConfirmPassword
                    )
                }
            }
        }
    )
}
