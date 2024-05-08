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

package proton.android.pass.featureitemcreate.impl.login

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.domain.ShareId
import proton.android.pass.featureitemcreate.impl.R

@Composable
internal fun MainLoginSection(
    modifier: Modifier = Modifier,
    loginItemFormState: LoginItemFormState,
    canUpdateUsername: Boolean,
    isEditAllowed: Boolean,
    isTotpError: Boolean,
    totpUiState: TotpUiState,
    selectedShareId: ShareId?,
    hasReachedAliasLimit: Boolean,
    onEvent: (LoginContentEvent) -> Unit,
    onFocusChange: (LoginField, Boolean) -> Unit,
    onAliasOptionsClick: () -> Unit,
    onUpgrade: () -> Unit,
    isUsernameSplitEnabled: Boolean
) {
    Column(
        modifier = modifier.roundedContainerNorm()
    ) {
        if (isUsernameSplitEnabled) {
            ExpandableEmailUsernameInput(
                email = loginItemFormState.email,
                username = loginItemFormState.username,
                canUpdateUsername = canUpdateUsername,
                isEditAllowed = isEditAllowed,
                onEvent = onEvent,
                onFocusChange = onFocusChange,
                onAliasOptionsClick = {
                    selectedShareId ?: return@ExpandableEmailUsernameInput
                    onEvent(LoginContentEvent.OnAliasOptions(selectedShareId, hasReachedAliasLimit))
                }
            )
        } else {
            // Here we're using email field and email callback to support proto backwards compatibility
            UsernameInput(
                labelResId = R.string.field_username_or_email_title,
                placeholderResId = R.string.field_username_or_email_hint,
                value = loginItemFormState.email,
                canUpdateUsername = canUpdateUsername,
                isEditAllowed = isEditAllowed,
                onChange = { newEmail ->
                    onEvent(LoginContentEvent.OnEmailChanged(newEmail))
                },
                onAliasOptionsClick = onAliasOptionsClick,
                onFocus = { isFocused ->
                    onFocusChange(LoginField.Email, isFocused)
                }
            )
        }

        Divider(color = PassTheme.colors.inputBorderNorm)

        PasswordInput(
            value = loginItemFormState.password,
            passwordStrength = loginItemFormState.passwordStrength,
            isEditAllowed = isEditAllowed,
            onChange = { onEvent(LoginContentEvent.OnPasswordChange(it)) },
            onFocus = { isFocused ->
                onEvent(LoginContentEvent.OnFocusChange(LoginField.Password, isFocused))
            }
        )

        Divider(color = PassTheme.colors.inputBorderNorm)

        val enabled = when (totpUiState) {
            TotpUiState.NotInitialised,
            TotpUiState.Loading,
            TotpUiState.Error -> false

            is TotpUiState.Limited -> totpUiState.isEdit && isEditAllowed
            TotpUiState.Success -> isEditAllowed
        }

        if (totpUiState is TotpUiState.Limited && !totpUiState.isEdit) {
            TotpLimit(onUpgrade = { onEvent(LoginContentEvent.OnUpgrade) })
        } else {
            TotpInput(
                value = loginItemFormState.primaryTotp,
                enabled = enabled,
                isError = isTotpError,
                onTotpChanged = { onEvent(LoginContentEvent.OnTotpChange(it)) },
                onFocus = { isFocused ->
                    onEvent(LoginContentEvent.OnFocusChange(LoginField.PrimaryTotp, isFocused))
                }
            )
        }
    }
}
