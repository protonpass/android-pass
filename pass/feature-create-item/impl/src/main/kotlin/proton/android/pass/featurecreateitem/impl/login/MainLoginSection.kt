package proton.android.pass.featurecreateitem.impl.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.composecomponents.impl.container.roundedContainer

@Composable
fun MainLoginSection(
    modifier: Modifier = Modifier,
    loginItem: LoginItem,
    showCreateAliasButton: Boolean,
    canUpdateUsername: Boolean,
    isEditAllowed: Boolean,
    onUsernameChange: (String) -> Unit,
    onCreateAliasClick: () -> Unit,
    onAliasOptionsClick: () -> Unit,
    onPasswordChange: (String) -> Unit,
    onGeneratePasswordClick: () -> Unit,
    onAddTotpClick: () -> Unit,
    onDeleteTotpClick: () -> Unit
) {
    Column(
        modifier = modifier.roundedContainer(ProtonTheme.colors.separatorNorm)
    ) {
        UsernameInput(
            value = loginItem.username,
            showCreateAliasButton = showCreateAliasButton,
            canUpdateUsername = canUpdateUsername,
            isEditAllowed = isEditAllowed,
            onChange = onUsernameChange,
            onGenerateAliasClick = onCreateAliasClick,
            onAliasOptionsClick = onAliasOptionsClick
        )
        Divider()
        PasswordInput(
            value = loginItem.password,
            isEditAllowed = isEditAllowed,
            onChange = onPasswordChange,
            onGeneratePasswordClick = onGeneratePasswordClick
        )
        Divider()
        TotpInput(
            modifier = Modifier.fillMaxWidth(),
            value = loginItem.primaryTotp,
            enabled = isEditAllowed,
            onAddTotpClick = onAddTotpClick,
            onDeleteTotpClick = onDeleteTotpClick
        )
    }
}
