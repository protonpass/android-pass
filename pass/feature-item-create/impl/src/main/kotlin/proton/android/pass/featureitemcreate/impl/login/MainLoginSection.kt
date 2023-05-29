package proton.android.pass.featureitemcreate.impl.login

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm

enum class MainLoginField {
    Username,
    Password,
    Totp
}

@Composable
fun MainLoginSection(
    modifier: Modifier = Modifier,
    loginItem: LoginItem,
    canUpdateUsername: Boolean,
    isEditAllowed: Boolean,
    isTotpError: Boolean,
    totpUiState: TotpUiState,
    onEvent: (LoginContentEvent) -> Unit,
    onFocusChange: (MainLoginField, Boolean) -> Unit,
    onAliasOptionsClick: () -> Unit,
    onUpgrade: () -> Unit
) {
    Column(
        modifier = modifier.roundedContainerNorm()
    ) {
        UsernameInput(
            value = loginItem.username,
            canUpdateUsername = canUpdateUsername,
            isEditAllowed = isEditAllowed,
            onChange = { onEvent(LoginContentEvent.OnUsernameChange(it)) },
            onAliasOptionsClick = onAliasOptionsClick,
            onFocus = { onFocusChange(MainLoginField.Username, it) }
        )
        Divider(color = PassTheme.colors.inputBorderNorm)
        PasswordInput(
            value = loginItem.password,
            isEditAllowed = isEditAllowed,
            onChange = { onEvent(LoginContentEvent.OnPasswordChange(it)) },
            onFocus = { onFocusChange(MainLoginField.Password, it) }
        )
        Divider(color = PassTheme.colors.inputBorderNorm)
        val enabled = when (totpUiState) {
            TotpUiState.NotInitialised,
            TotpUiState.Loading,
            TotpUiState.Error -> false

            is TotpUiState.Limited -> totpUiState.isEdit && isEditAllowed
            TotpUiState.Success -> isEditAllowed
        }
        if (totpUiState is TotpUiState.Limited) {
            TotpLimit(onUpgrade = onUpgrade)
        } else {
            TotpInput(
                value = loginItem.primaryTotp,
                enabled = enabled,
                isError = isTotpError,
                onTotpChanged = { onEvent(LoginContentEvent.OnTotpChange(it)) },
                onFocus = { onFocusChange(MainLoginField.Totp, it) }
            )
        }
    }
}
