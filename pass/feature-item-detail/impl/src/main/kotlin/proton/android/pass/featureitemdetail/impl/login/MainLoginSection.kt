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
    state: LoginDetailUiState,
    onUsernameClick: () -> Unit,
    onTogglePasswordClick: () -> Unit,
    onCopyPasswordClick: () -> Unit,
    onCopyTotpClick: (String) -> Unit
) {
    RoundedCornersColumn(
        modifier = modifier.fillMaxWidth()
    ) {
        LoginUsernameRow(
            username = state.username,
            onUsernameClick = onUsernameClick
        )
        Divider()
        LoginPasswordRow(
            password = state.password,
            onTogglePasswordClick = onTogglePasswordClick,
            onCopyPasswordClick = onCopyPasswordClick
        )
        if (state.totpUiState != null) {
            Divider()
            TotpRow(state = state.totpUiState) { onCopyTotpClick(it) }
        }
    }
}


class ThemedLoginPasswordRowPreviewProvider :
    ThemePairPreviewProvider<LoginDetailUiState>(LoginDetailUiStatePreviewProvider())

@Preview
@Composable
fun MainLoginSectionPreview(
    @PreviewParameter(ThemedLoginPasswordRowPreviewProvider::class) input: Pair<Boolean, LoginDetailUiState>
) {
    PassTheme(isDark = input.first) {
        Surface {
            MainLoginSection(
                state = input.second,
                onUsernameClick = {},
                onTogglePasswordClick = {},
                onCopyPasswordClick = {},
                onCopyTotpClick = {}
            )
        }
    }
}
