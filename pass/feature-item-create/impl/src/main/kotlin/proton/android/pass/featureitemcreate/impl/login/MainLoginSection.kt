package proton.android.pass.featureitemcreate.impl.login

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.pass.domain.ItemContents

@Composable
fun MainLoginSection(
    modifier: Modifier = Modifier,
    contents: ItemContents.Login,
    canUpdateUsername: Boolean,
    isEditAllowed: Boolean,
    isTotpError: Boolean,
    totpUiState: TotpUiState,
    onEvent: (LoginContentEvent) -> Unit,
    onFocusChange: (LoginField, Boolean) -> Unit,
    onAliasOptionsClick: () -> Unit,
    onUpgrade: () -> Unit
) {
    Column(
        modifier = modifier.roundedContainerNorm()
    ) {
        UsernameInput(
            value = contents.username,
            canUpdateUsername = canUpdateUsername,
            isEditAllowed = isEditAllowed,
            onChange = { onEvent(LoginContentEvent.OnUsernameChange(it)) },
            onAliasOptionsClick = onAliasOptionsClick,
            onFocus = { onFocusChange(LoginField.Username, it) }
        )
        Divider(color = PassTheme.colors.inputBorderNorm)
        PasswordInput(
            value = contents.password,
            isEditAllowed = isEditAllowed,
            onChange = { onEvent(LoginContentEvent.OnPasswordChange(it)) },
            onFocus = { onFocusChange(LoginField.Password, it) }
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
                value = contents.primaryTotp,
                enabled = enabled,
                isError = isTotpError,
                onTotpChanged = { onEvent(LoginContentEvent.OnTotpChange(it)) },
                onFocus = { onFocusChange(LoginField.PrimaryTotp, it) }
            )
        }
    }
}
