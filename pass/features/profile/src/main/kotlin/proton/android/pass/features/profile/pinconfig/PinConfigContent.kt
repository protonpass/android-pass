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

package proton.android.pass.features.profile.pinconfig

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.RequestFocusLaunchedEffect
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.commonui.api.body3Weak
import proton.android.pass.commonui.api.heroNorm
import proton.android.pass.commonui.api.heroWeak
import proton.android.pass.composecomponents.impl.buttons.CircleButton
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.composecomponents.impl.topbar.iconbutton.BackArrowCircleIconButton
import proton.android.pass.features.profile.ProfileNavigation
import proton.android.pass.features.profile.R
import proton.android.pass.features.profile.pinconfig.PinConfigValidationErrors.PinBlank
import proton.android.pass.features.profile.pinconfig.PinConfigValidationErrors.PinDoesNotMatch
import proton.android.pass.features.profile.pinconfig.PinConfigValidationErrors.PinTooShort

@Composable
fun PinConfigContent(
    modifier: Modifier = Modifier,
    state: PinConfigUiState,
    onNavigateEvent: (ProfileNavigation) -> Unit,
    onPinChange: (String) -> Unit,
    onRepeatPinChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding(),
        topBar = {
            ProtonTopAppBar(
                modifier = modifier,
                backgroundColor = PassTheme.colors.backgroundStrong,
                title = {},
                navigationIcon = {
                    BackArrowCircleIconButton(
                        modifier = Modifier.padding(Spacing.mediumSmall, Spacing.extraSmall),
                        color = PassTheme.colors.interactionNorm,
                        backgroundColor = PassTheme.colors.interactionNormMinor1,
                        onUpClick = { onNavigateEvent(ProfileNavigation.CloseScreen) }
                    )
                },
                actions = {
                    CircleButton(
                        modifier = Modifier.padding(Spacing.medium, Spacing.none),
                        contentPadding = PaddingValues(Spacing.medium, Spacing.none),
                        color = PassTheme.colors.interactionNormMajor1,
                        content = {
                            Text(
                                text = stringResource(R.string.configure_pin_continue),
                                style = ProtonTheme.typography.defaultSmallNorm,
                                color = PassTheme.colors.textInvert
                            )
                        },
                        onClick = onSubmit
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium),
            horizontalAlignment = if (state.isQuest) {
                Alignment.CenterHorizontally
            } else {
                Alignment.Start
            }
        ) {
            Text(
                text = stringResource(R.string.configure_pin_set_pin_code),
                style = PassTheme.typography.heroNorm()
            )
            Text(
                text = stringResource(R.string.configure_pin_unlock_the_app_with_this_code),
                style = PassTheme.typography.body3Weak()
            )
            Spacer(modifier = Modifier.height(16.dp))
            val (isError, errorMessage) = when {
                state.validationErrors.contains(PinBlank) ->
                    true to stringResource(R.string.configure_pin_pin_cannot_be_blank)

                state.validationErrors.contains(PinTooShort) ->
                    true to stringResource(R.string.configure_pin_pin_too_short)

                else -> false to ""
            }
            val focusRequester = remember { FocusRequester() }
            ProtonTextField(
                modifier = Modifier.focusRequester(focusRequester),
                textFieldModifier = Modifier.applyIf(
                    condition = !state.isQuest,
                    ifTrue = {
                        fillMaxWidth()
                    }
                ),
                value = state.pin,
                textStyle = PassTheme.typography.heroNorm(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Next
                ),
                isError = isError,
                errorMessage = errorMessage,
                placeholder = {
                    ProtonTextFieldPlaceHolder(
                        text = stringResource(R.string.configure_pin_enter_pin_code),
                        textStyle = PassTheme.typography.heroWeak()
                    )
                },
                moveToNextOnEnter = true,
                onChange = onPinChange
            )
            RequestFocusLaunchedEffect(focusRequester, true)
            ProtonTextField(
                textFieldModifier = Modifier.applyIf(
                    condition = !state.isQuest,
                    ifTrue = {
                        fillMaxWidth()
                    }
                ),
                value = state.repeatPin,
                textStyle = PassTheme.typography.heroNorm(),
                keyboardOptions = KeyboardOptions(
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Done
                ),
                isError = state.validationErrors.contains(PinDoesNotMatch),
                errorMessage = stringResource(R.string.configure_pin_pin_does_not_match),
                visualTransformation = PasswordVisualTransformation(),
                placeholder = {
                    ProtonTextFieldPlaceHolder(
                        text = stringResource(R.string.configure_pin_repeat_pin_code),
                        textStyle = PassTheme.typography.heroWeak()
                    )
                },
                onChange = onRepeatPinChange,
                onDoneClick = onSubmit
            )
        }
    }
}

@Composable
@Preview
fun PinConfigContentPreview() {
    PassTheme {
        PinConfigContent(
            state = PinConfigUiState(),
            onNavigateEvent = {},
            onPinChange = {},
            onRepeatPinChange = {},
            onSubmit = {}
        )
    }
}

@Composable
@Preview(
    name = "VR Headset",
    widthDp = 2000,
    heightDp = 1000,
    showBackground = true
)
fun PinConfigContentForQuestPreview() {
    PassTheme {
        PinConfigContent(
            state = PinConfigUiState(
                isQuest = true
            ),
            onNavigateEvent = {},
            onPinChange = {},
            onRepeatPinChange = {},
            onSubmit = {}
        )
    }
}
