package proton.android.pass.featureitemcreate.impl.login

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm

@Composable
fun MainLoginSection(
    modifier: Modifier = Modifier,
    loginItem: LoginItem,
    canUpdateUsername: Boolean,
    isEditAllowed: Boolean,
    isTotpError: Boolean,
    totpUiState: TotpUiState,
    onUsernameChange: (String) -> Unit,
    onUsernameFocus: (Boolean) -> Unit,
    onAliasOptionsClick: () -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordFocus: (Boolean) -> Unit,
    onTotpChanged: (String) -> Unit,
    onTotpFocus: (Boolean) -> Unit,
    onUpgrade: () -> Unit
) {
    Column(
        modifier = modifier.roundedContainerNorm()
    ) {
        UsernameInput(
            value = loginItem.username,
            canUpdateUsername = canUpdateUsername,
            isEditAllowed = isEditAllowed,
            onChange = onUsernameChange,
            onAliasOptionsClick = onAliasOptionsClick,
            onFocus = onUsernameFocus
        )
        Divider(color = PassTheme.colors.inputBorderNorm)
        PasswordInput(
            value = loginItem.password,
            isEditAllowed = isEditAllowed,
            onChange = onPasswordChange,
            onFocus = onPasswordFocus
        )
        Divider(color = PassTheme.colors.inputBorderNorm)
        when (totpUiState) {
            TotpUiState.NotInitialised,
            TotpUiState.Loading,
            TotpUiState.Error -> TotpInput(
                value = loginItem.primaryTotp,
                enabled = false,
                isError = isTotpError,
                onTotpChanged = onTotpChanged,
                onFocus = onTotpFocus
            )

            is TotpUiState.Limited -> if (totpUiState.isEdit) {
                TotpInput(
                    value = loginItem.primaryTotp,
                    enabled = isEditAllowed,
                    isError = isTotpError,
                    onTotpChanged = onTotpChanged,
                    onFocus = onTotpFocus
                )
            } else {
                TotpLimit(onUpgrade = onUpgrade)
            }

            TotpUiState.Success -> TotpInput(
                value = loginItem.primaryTotp,
                enabled = isEditAllowed,
                isError = isTotpError,
                onTotpChanged = onTotpChanged,
                onFocus = onTotpFocus
            )
        }
    }
}
