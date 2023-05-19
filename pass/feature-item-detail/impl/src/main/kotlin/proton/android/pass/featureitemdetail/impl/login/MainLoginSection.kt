package proton.android.pass.featureitemdetail.impl.login

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.container.RoundedCornersColumn
import proton.android.pass.featureitemdetail.impl.login.totp.TotpRow

@Composable
fun MainLoginSection(
    modifier: Modifier = Modifier,
    username: String,
    passwordState: PasswordState,
    totpUiState: TotpUiState?,
    showViewAlias: Boolean,
    onUsernameClick: () -> Unit,
    onGoToAliasClick: () -> Unit,
    onTogglePasswordClick: () -> Unit,
    onCopyPasswordClick: () -> Unit,
    onCopyTotpClick: (String) -> Unit,
    onUpgradeClick: () -> Unit
) {
    RoundedCornersColumn(
        modifier = modifier.fillMaxWidth()
    ) {
        LoginUsernameRow(
            username = username,
            showViewAlias = showViewAlias,
            onUsernameClick = onUsernameClick,
            onGoToAliasClick = onGoToAliasClick
        )
        Divider(color = PassTheme.colors.inputBorderNorm)
        LoginPasswordRow(
            password = passwordState,
            onTogglePasswordClick = onTogglePasswordClick,
            onCopyPasswordClick = onCopyPasswordClick
        )
        if (totpUiState != null) {
            Divider(color = PassTheme.colors.inputBorderNorm)
            TotpRow(
                state = totpUiState,
                onCopyTotpClick = onCopyTotpClick,
                onUpgradeClick = onUpgradeClick
            )
        }
    }
}


class ThemedLoginPasswordRowPreviewProvider :
    ThemePairPreviewProvider<MainLoginSectionParams>(MainLoginSectionParamsPreviewProvider())

@Preview
@Composable
fun MainLoginSectionPreview(
    @PreviewParameter(ThemedLoginPasswordRowPreviewProvider::class) input: Pair<Boolean, MainLoginSectionParams>
) {
    PassTheme(isDark = input.first) {
        Surface {
            MainLoginSection(
                username = input.second.username,
                passwordState = input.second.passwordState,
                totpUiState = input.second.totpUiState,
                showViewAlias = input.second.showViewAlias,
                onUsernameClick = {},
                onTogglePasswordClick = {},
                onCopyPasswordClick = {},
                onCopyTotpClick = {},
                onGoToAliasClick = {},
                onUpgradeClick = {}
            )
        }
    }
}
