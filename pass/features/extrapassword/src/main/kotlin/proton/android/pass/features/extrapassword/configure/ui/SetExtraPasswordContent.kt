/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.features.extrapassword.configure.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallNorm
import me.proton.core.compose.theme.defaultSmallWeak
import me.proton.core.compose.theme.subheadlineNorm
import me.proton.core.compose.theme.subheadlineUnspecified
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.RequestFocusLaunchedEffect
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.heroNorm
import proton.android.pass.composecomponents.impl.buttons.CircleButton
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.composecomponents.impl.topbar.iconbutton.CrossBackCircleIconButton
import proton.android.pass.features.extrapassword.R
import proton.android.pass.features.extrapassword.configure.navigation.SetExtraPasswordContentNavEvent
import proton.android.pass.features.extrapassword.configure.navigation.SetExtraPasswordContentNavEvent.OnExtraPasswordRepeatValueChangedNav
import proton.android.pass.features.extrapassword.configure.navigation.SetExtraPasswordContentNavEvent.Submit
import proton.android.pass.features.extrapassword.configure.presentation.SetExtraPasswordState
import proton.android.pass.features.extrapassword.configure.presentation.SetExtraPasswordValidationErrors
import proton.android.pass.features.extrapassword.configure.presentation.SetExtraPasswordValidationErrors.BlankPassword
import proton.android.pass.features.extrapassword.configure.presentation.SetExtraPasswordValidationErrors.MinimumLengthPassword
import proton.android.pass.features.extrapassword.configure.presentation.SetExtraPasswordValidationErrors.PasswordMismatch
import me.proton.core.presentation.R as CoreR

@Composable
internal fun SetExtraPasswordContent(
    modifier: Modifier = Modifier,
    formState: SetExtraPasswordState,
    validationError: Option<SetExtraPasswordValidationErrors>,
    onEvent: (SetExtraPasswordContentNavEvent) -> Unit
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding(),
        topBar = {
            ProtonTopAppBar(
                backgroundColor = PassTheme.colors.backgroundStrong,
                title = {},
                navigationIcon = {
                    CrossBackCircleIconButton(
                        modifier = Modifier.padding(12.dp, Spacing.extraSmall),
                        color = PassTheme.colors.interactionNorm,
                        backgroundColor = PassTheme.colors.interactionNormMinor1,
                        onUpClick = { onEvent(SetExtraPasswordContentNavEvent.Back) }
                    )
                },
                actions = {
                    CircleButton(
                        modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.none),
                        contentPadding = PaddingValues(Spacing.medium, Spacing.small),
                        color = PassTheme.colors.interactionNormMajor1,
                        content = {
                            Text(
                                text = stringResource(R.string.configure_extra_password_continue),
                                style = ProtonTheme.typography.defaultSmallNorm,
                                color = PassTheme.colors.textInvert
                            )
                        },
                        onClick = { onEvent(Submit) }
                    )
                }
            )
        }
    ) { padding ->
        val focusRequester = remember { FocusRequester() }
        RequestFocusLaunchedEffect(focusRequester, true)
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            Text(
                text = stringResource(R.string.configure_extra_password_title),
                style = PassTheme.typography.heroNorm()
            )
            Spacer(modifier = Modifier.height(Spacing.medium))
            var isPasswordConcealed: Boolean by remember { mutableStateOf(true) }
            var isRepeatPasswordConcealed: Boolean by remember { mutableStateOf(true) }
            val (isPasswordError, errorMessage) = when (validationError) {
                is Some -> when (validationError.value) {
                    BlankPassword ->
                        true to stringResource(R.string.configure_extra_password_cannot_be_blank)
                    MinimumLengthPassword ->
                        true to stringResource(R.string.configure_extra_password_password_too_short)
                    else -> false to ""
                }
                else -> false to ""
            }
            ProtonTextField(
                modifier = Modifier.focusRequester(focusRequester),
                value = formState.password,
                textStyle = ProtonTheme.typography.subheadlineNorm,
                isError = isPasswordError,
                errorMessage = errorMessage,
                visualTransformation = if (isPasswordConcealed) {
                    PasswordVisualTransformation()
                } else {
                    VisualTransformation.None
                },
                keyboardOptions = KeyboardOptions(
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                placeholder = {
                    ProtonTextFieldPlaceHolder(
                        text = stringResource(R.string.configure_extra_password_enter_extra_password),
                        textStyle = ProtonTheme.typography.subheadlineUnspecified
                            .copy(color = ProtonTheme.colors.textWeak)
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = { isPasswordConcealed = !isPasswordConcealed }
                    ) {
                        val icon = if (!isPasswordConcealed) {
                            CoreR.drawable.ic_proton_eye
                        } else {
                            CoreR.drawable.ic_proton_eye_slash
                        }
                        Icon(
                            painter = painterResource(id = icon),
                            contentDescription = null,
                            tint = PassTheme.colors.loginInteractionNorm
                        )
                    }
                },
                moveToNextOnEnter = true,
                onChange = { onEvent(SetExtraPasswordContentNavEvent.OnExtraPasswordValueChangedNav(it)) }
            )
            ProtonTextField(
                value = formState.repeatPassword,
                textStyle = ProtonTheme.typography.subheadlineNorm,
                isError = validationError is Some && validationError.value == PasswordMismatch,
                errorMessage = stringResource(R.string.configure_extra_password_mismatch),
                keyboardOptions = KeyboardOptions(
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                visualTransformation = if (isRepeatPasswordConcealed) {
                    PasswordVisualTransformation()
                } else {
                    VisualTransformation.None
                },
                placeholder = {
                    ProtonTextFieldPlaceHolder(
                        text = stringResource(R.string.configure_extra_password_repeat_extra_password),
                        textStyle = ProtonTheme.typography.subheadlineUnspecified
                            .copy(color = ProtonTheme.colors.textWeak)
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = { isRepeatPasswordConcealed = !isRepeatPasswordConcealed }
                    ) {
                        val icon = if (!isRepeatPasswordConcealed) {
                            CoreR.drawable.ic_proton_eye
                        } else {
                            CoreR.drawable.ic_proton_eye_slash
                        }
                        Icon(
                            painter = painterResource(id = icon),
                            contentDescription = null,
                            tint = PassTheme.colors.loginInteractionNorm
                        )
                    }
                },
                onChange = { onEvent(OnExtraPasswordRepeatValueChangedNav(it)) },
                onDoneClick = { onEvent(Submit) }
            )
            PassDivider()
            Text(
                text = stringResource(R.string.configure_extra_password_description),
                style = ProtonTheme.typography.defaultSmallWeak
            )
            Text(
                text = stringResource(R.string.configure_extra_password_warning),
                style = ProtonTheme.typography.defaultSmallNorm
                    .copy(color = PassTheme.colors.signalDanger)
            )
        }
    }
}
