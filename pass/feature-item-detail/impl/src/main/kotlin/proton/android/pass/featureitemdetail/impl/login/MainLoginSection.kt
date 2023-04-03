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

@Composable
fun MainLoginSection(
    modifier: Modifier = Modifier,
    username: String,
    passwordState: PasswordState,
    totpUiState: TotpUiState?,
    onUsernameClick: () -> Unit,
    onTogglePasswordClick: () -> Unit,
    onCopyPasswordClick: () -> Unit,
    onCopyTotpClick: (String) -> Unit,
) {
    RoundedCornersColumn(
        modifier = modifier.fillMaxWidth()
    ) {
        LoginUsernameRow(
            username = username,
            onUsernameClick = onUsernameClick
        )
        Divider()
        LoginPasswordRow(
            password = passwordState,
            onTogglePasswordClick = onTogglePasswordClick,
            onCopyPasswordClick = onCopyPasswordClick
        )
        if (totpUiState != null) {
            Divider()
            TotpRow(state = totpUiState) { onCopyTotpClick(it) }
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
                onUsernameClick = {},
                onTogglePasswordClick = {},
                onCopyPasswordClick = {},
                onCopyTotpClick = {}
            )
        }
    }
}
