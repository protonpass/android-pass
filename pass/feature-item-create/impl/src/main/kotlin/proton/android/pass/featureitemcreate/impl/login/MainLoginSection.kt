package proton.android.pass.featureitemcreate.impl.login

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.composecomponents.impl.container.roundedContainer

@Composable
fun MainLoginSection(
    modifier: Modifier = Modifier,
    loginItem: LoginItem,
    canUpdateUsername: Boolean,
    isEditAllowed: Boolean,
    onUsernameChange: (String) -> Unit,
    onUsernameFocus: (Boolean) -> Unit,
    onAliasOptionsClick: () -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordFocus: (Boolean) -> Unit,
    onTotpChanged: (String) -> Unit,
    onTotpFocus: (Boolean) -> Unit
) {
    Column(
        modifier = modifier.roundedContainer(ProtonTheme.colors.separatorNorm)
    ) {
        UsernameInput(
            value = loginItem.username,
            canUpdateUsername = canUpdateUsername,
            isEditAllowed = isEditAllowed,
            onChange = onUsernameChange,
            onAliasOptionsClick = onAliasOptionsClick,
            onFocus = onUsernameFocus
        )
        Divider()
        PasswordInput(
            value = loginItem.password,
            isEditAllowed = isEditAllowed,
            onChange = onPasswordChange,
            onFocus = onPasswordFocus
        )
        Divider()
        TotpInput(
            value = loginItem.primaryTotp,
            enabled = isEditAllowed,
            onTotpChanged = onTotpChanged,
            onFocus = onTotpFocus
        )
    }
}
