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
import proton.android.pass.features.password.R
import proton.android.pass.features.password.GeneratePasswordNavigation

@Suppress("CyclomaticComplexMethod", "ComplexMethod")
@Composable
fun GeneratePasswordBottomSheet(modifier: Modifier = Modifier, onNavigate: (GeneratePasswordNavigation) -> Unit) {
    val viewModel = hiltViewModel<GeneratePasswordViewModel>()
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    GeneratePasswordBottomSheetContent(
        modifier = modifier,
        state = state,
        onEvent = {
            when (it) {
                is GeneratePasswordEvent.OnPasswordModeChange -> {
                    viewModel.onPasswordModeChange(it.mode)
                }
                GeneratePasswordEvent.OnPasswordModeChangeClick -> {
                    onNavigate(GeneratePasswordNavigation.OnSelectPasswordMode)
                }
                is GeneratePasswordEvent.OnRandomIncludeNumbersChange -> {
                    viewModel.onIncludeNumbersChange(it.value)
                }
                is GeneratePasswordEvent.OnRandomUseCapitalLettersChange -> {
                    viewModel.onCapitalLettersChange(it.value)
                }
                GeneratePasswordEvent.OnRegeneratePasswordClick -> {
                    viewModel.onRegeneratePassword()
                }
                is GeneratePasswordEvent.OnWordsCapitalizeChange -> {
                    viewModel.onWordsCapitalizeChange(it.value)
                }
                is GeneratePasswordEvent.OnWordsIncludeNumbersChange -> {
                    viewModel.onWordsIncludeNumbersChange(it.value)
                }
                is GeneratePasswordEvent.OnWordsCountChange -> {
                    viewModel.onWordsCountChange(it.count)
                }
                GeneratePasswordEvent.OnWordsSeparatorClick -> {
                    onNavigate(GeneratePasswordNavigation.OnSelectWordSeparator)
                }
                is GeneratePasswordEvent.OnWordsSeparatorChange -> {
                    viewModel.onWordsSeparatorChange(it.separator)
                }

                is GeneratePasswordEvent.OnPasswordConfigChanged -> {
                    viewModel.onChangePasswordConfig(it.config)
                }
            }
        },
        buttonSection = {
            when (state.mode) {
                GeneratePasswordMode.CopyAndClose ->
                    @Composable
                    {
                        CircleButton(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(14.dp),
                            color = PassTheme.colors.loginInteractionNormMajor1,
                            elevation = ButtonDefaults.elevation(0.dp),
                            onClick = {
                                viewModel.onConfirm()
                                onNavigate(GeneratePasswordNavigation.DismissBottomsheet)
                            }
                        ) {
                            Text(
                                text = stringResource(R.string.generate_password_copy),
                                style = PassTheme.typography.body3Inverted(),
                                color = PassTheme.colors.textInvert
                            )
                        }
                    }

                GeneratePasswordMode.CancelConfirm -> @Composable {
                    BottomSheetCancelConfirm(
                        modifier = Modifier.fillMaxWidth(),
                        onCancel = { onNavigate(GeneratePasswordNavigation.DismissBottomsheet) },
                        onConfirm = {
                            viewModel.onConfirm()
                            onNavigate(GeneratePasswordNavigation.DismissBottomsheet)
                        }
                    )
                }
            }
        }
    )
}
