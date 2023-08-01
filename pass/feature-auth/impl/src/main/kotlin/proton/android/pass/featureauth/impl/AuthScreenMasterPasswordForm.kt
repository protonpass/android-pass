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

package proton.android.pass.featureauth.impl

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.body3Norm
import proton.android.pass.commonui.api.body3Weak
import proton.android.pass.commonui.api.heroNorm
import proton.android.pass.composecomponents.impl.buttons.CircleIconButton
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldLabel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import me.proton.core.presentation.R as CoreR

@Composable
fun AuthScreenMasterPasswordForm(
    modifier: Modifier = Modifier,
    state: AuthContent,
    onEvent: (AuthUiEvent) -> Unit,
    onSubmit: () -> Unit
) {
    val (visualTransformation, actionIcon, actionContent) = if (state.isPasswordVisible) {
        AuthUiContent(
            visualTransformation = VisualTransformation.None,
            trailingIcon = CoreR.drawable.ic_proton_eye,
            trailingIconContentDescription = stringResource(R.string.auth_action_conceal_password)
        )
    } else {
        AuthUiContent(
            visualTransformation = PasswordVisualTransformation(),
            trailingIcon = CoreR.drawable.ic_proton_eye_slash,
            trailingIconContentDescription = stringResource(R.string.auth_action_reveal_password)
        )
    }

    val errorMessage = when (state.passwordError.value()) {
        PasswordError.EmptyPassword -> stringResource(R.string.auth_error_empty_password)
        else -> ""
    }

    val isEditAllowed = !state.isLoadingState.value()

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.auth_unlock_app_title),
            textAlign = TextAlign.Center,
            style = PassTheme.typography.heroNorm()
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.auth_unlock_app_subtitle, state.address),
            textAlign = TextAlign.Center,
            style = PassTheme.typography.body3Weak()
        )
        Spacer(modifier = Modifier.height(40.dp))

        ProtonTextField(
            modifier = Modifier
                .roundedContainerNorm()
                .fillMaxWidth()
                .padding(start = 0.dp, top = 16.dp, end = 4.dp, bottom = 16.dp),
            value = state.password,
            editable = isEditAllowed,
            keyboardOptions = KeyboardOptions(
                autoCorrect = false,
                keyboardType = KeyboardType.Password
            ),
            textStyle = ProtonTheme.typography.defaultNorm(isEditAllowed),
            onChange = { onEvent(AuthUiEvent.OnPasswordUpdate(it)) },
            label = {
                ProtonTextFieldLabel(
                    text = stringResource(R.string.auth_master_password_label),
                    isError = state.passwordError.value() == PasswordError.EmptyPassword
                )
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(CoreR.drawable.ic_proton_lock),
                    contentDescription = null,
                    tint = ProtonTheme.colors.iconWeak
                )
            },
            trailingIcon = {
                CircleIconButton(
                    backgroundColor = Color.Unspecified,
                    onClick = {
                        onEvent(AuthUiEvent.OnTogglePasswordVisibility(!state.isPasswordVisible))
                    }
                ) {
                    Icon(
                        painter = painterResource(actionIcon),
                        contentDescription = actionContent,
                        tint = PassTheme.colors.interactionNormMajor2,
                    )
                }
            },
            isError = state.passwordError.isNotEmpty(),
            errorMessage = errorMessage,
            visualTransformation = visualTransformation,
            onDoneClick = onSubmit
        )

        if (state.error is Some) {
            val errorText = when (val error = state.error.value) {
                AuthError.UnknownError -> stringResource(R.string.auth_error_verifying_password)
                is AuthError.WrongPassword -> pluralStringResource(
                    R.plurals.auth_error_wrong_password,
                    error.remainingAttempts,
                    error.remainingAttempts
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = errorText,
                textAlign = TextAlign.Center,
                style = PassTheme.typography.body3Norm(),
                color = ProtonTheme.colors.notificationError
            )
        }
    }
}

private data class AuthUiContent(
    val visualTransformation: VisualTransformation,
    @DrawableRes val trailingIcon: Int,
    val trailingIconContentDescription: String
)

@Preview
@Composable
fun AuthScreenMasterPasswordFormPreview(
    @PreviewParameter(ThemeMasterPasswordPreviewProvider::class) input: Pair<Boolean, MasterPasswordInput>
) {
    PassTheme(isDark = input.first) {
        Surface {
            AuthScreenMasterPasswordForm(
                state = AuthContent(
                    password = input.second.password,
                    address = "some@address.test",
                    isLoadingState = IsLoadingState.NotLoading,
                    error = input.second.error,
                    isPasswordVisible = input.second.isPasswordVisible,
                    passwordError = input.second.passwordError
                ),
                onEvent = {},
                onSubmit = {}
            )
        }
    }
}
