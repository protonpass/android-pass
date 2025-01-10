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

package proton.android.pass.features.auth

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentMapOf
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.some
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.body3Norm
import proton.android.pass.commonui.api.body3Weak
import proton.android.pass.commonui.api.heroNorm
import proton.android.pass.composecomponents.impl.buttons.CircleIconButton
import proton.android.pass.composecomponents.impl.buttons.TransparentTextButton
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldLabel
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import me.proton.core.presentation.R as CoreR
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
fun AuthScreenMasterPasswordForm(
    modifier: Modifier = Modifier,
    state: AuthStateContent,
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
        PasswordError.IncorrectPassword -> stringResource(R.string.auth_error_wrong_password_no_attempts)
        null -> ""
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

        if (state.accountSwitcherState.hasMultipleAccounts) {
            val text = when (state.showExtraPassword) {
                is LoadingResult.Error,
                LoadingResult.Loading -> ""

                is LoadingResult.Success -> if (state.showExtraPassword.data) {
                    stringResource(R.string.auth_unlock_app_extra_password_subtitle_account_switch)
                } else {
                    stringResource(R.string.auth_unlock_app_subtitle_account_switch)
                }
            }
            var isExpanded by remember { mutableStateOf(false) }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text.Body3Regular(text, Modifier, ProtonTheme.colors.textWeak)
                Box {
                    TransparentTextButton(
                        text = state.accountSwitcherState.accounts.values
                            .firstOrNull(AccountItem::isPrimary)
                            ?.email
                            .orEmpty(),
                        color = PassTheme.colors.textNorm,
                        suffixIcon = CompR.drawable.ic_chevron_tiny_down,
                        onClick = { isExpanded = !isExpanded }
                    )
                    DropdownMenu(
                        modifier = Modifier.background(PassTheme.colors.inputBackgroundNorm),
                        expanded = isExpanded,
                        onDismissRequest = { isExpanded = false }
                    ) {
                        state.accountSwitcherState.accounts.forEach { (userId, accountItem) ->
                            DropdownMenuItem(
                                onClick = {
                                    onEvent(AuthUiEvent.OnAccountSwitch(userId))
                                    isExpanded = false
                                }
                            ) {
                                Text.Body1Regular(text = accountItem.email)
                            }
                        }
                    }
                }
            }
        } else {
            if (state.address is Some && state.address.value.isNotBlank()) {
                val text = when (state.showExtraPassword) {
                    is LoadingResult.Error,
                    LoadingResult.Loading -> ""

                    is LoadingResult.Success -> if (state.showExtraPassword.data) {
                        stringResource(
                            R.string.auth_unlock_app_extra_password_subtitle,
                            state.address.value
                        )
                    } else {
                        stringResource(R.string.auth_unlock_app_subtitle, state.address.value)
                    }
                }
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = text,
                    textAlign = TextAlign.Center,
                    style = PassTheme.typography.body3Weak()
                )
            }
        }
        Spacer(modifier = Modifier.height(40.dp))

        ProtonTextField(
            modifier = Modifier
                .roundedContainerNorm()
                .fillMaxWidth()
                .padding(
                    start = Spacing.none,
                    top = Spacing.medium,
                    end = Spacing.extraSmall,
                    bottom = Spacing.medium
                ),
            value = state.password,
            editable = isEditAllowed,
            keyboardOptions = KeyboardOptions(
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            textStyle = ProtonTheme.typography.defaultNorm(isEditAllowed),
            onChange = { onEvent(AuthUiEvent.OnPasswordUpdate(it)) },
            label = {
                val text = when (state.showExtraPassword) {
                    is LoadingResult.Error,
                    LoadingResult.Loading -> ""

                    is LoadingResult.Success -> if (state.showExtraPassword.data) {
                        stringResource(R.string.auth_extra_password_label)
                    } else {
                        stringResource(R.string.auth_master_password_label)
                    }
                }
                ProtonTextFieldLabel(
                    text = text,
                    isError = state.passwordError.isNotEmpty()
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
                        tint = PassTheme.colors.interactionNormMajor2
                    )
                }
            },
            isError = state.passwordError.isNotEmpty(),
            errorMessage = errorMessage,
            visualTransformation = visualTransformation,
            onDoneClick = onSubmit
        )

        if (state.authMethod is Some && state.showPinOrBiometry) {
            val text = when (state.authMethod.value) {
                AuthMethod.Pin -> stringResource(R.string.auth_action_enter_pin_instead)
                AuthMethod.Fingerprint -> stringResource(R.string.auth_action_enter_fingerprint_instead)
            }
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .align(Alignment.CenterHorizontally)
                    .clickable { onEvent(AuthUiEvent.OnAuthAgainClick) }
                    .padding(8.dp),
                text = text,
                textAlign = TextAlign.Center,
                color = PassTheme.colors.interactionNormMajor2
            )
        }

        if (state.remainingPasswordAttempts is Some) {
            val remainingAttemptsText = pluralStringResource(
                R.plurals.auth_attempts_remaining,
                state.remainingPasswordAttempts.value,
                state.remainingPasswordAttempts.value
            )

            Spacer(modifier = Modifier.height(40.dp))
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = remainingAttemptsText,
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
                state = AuthStateContent(
                    userId = None,
                    password = input.second.password,
                    address = "some@address.test".some(),
                    isLoadingState = IsLoadingState.NotLoading,
                    isPasswordVisible = input.second.isPasswordVisible,
                    remainingPasswordAttempts = input.second.remainingAttempts,
                    passwordError = input.second.passwordError,
                    authMethod = None,
                    showExtraPassword = LoadingResult.Success(input.second.hasExtraPassword),
                    showPinOrBiometry = true,
                    showLogout = true,
                    showBackNavigation = false,
                    accountSwitcherState = AccountSwitcherState(
                        accounts = persistentMapOf()
                    )
                ),
                onEvent = {},
                onSubmit = {}
            )
        }
    }
}
