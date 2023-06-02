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
import proton.pass.domain.HiddenState

@Composable
fun MainLoginSection(
    modifier: Modifier = Modifier,
    username: String,
    passwordState: HiddenState,
    totpUiState: TotpUiState?,
    showViewAlias: Boolean,
    onEvent: (LoginDetailEvent) -> Unit
) {
    RoundedCornersColumn(modifier = modifier.fillMaxWidth()) {
        LoginUsernameRow(
            username = username,
            showViewAlias = showViewAlias,
            onUsernameClick = {
                onEvent(LoginDetailEvent.OnUsernameClick)
            },
            onGoToAliasClick = {
                onEvent(LoginDetailEvent.OnGoToAliasClick)
            }
        )
        if (passwordState !is HiddenState.Empty) {
            Divider(color = PassTheme.colors.inputBorderNorm)
            LoginPasswordRow(
                passwordHiddenState = passwordState,
                onTogglePasswordClick = {
                    onEvent(LoginDetailEvent.OnTogglePasswordClick)
                },
                onCopyPasswordClick = {
                    onEvent(LoginDetailEvent.OnCopyPasswordClick)
                }
            )
        }
        if (totpUiState != null) {
            Divider(color = PassTheme.colors.inputBorderNorm)
            TotpRow(
                state = totpUiState,
                onCopyTotpClick = {
                    onEvent(LoginDetailEvent.OnCopyTotpClick(it))
                },
                onUpgradeClick = {
                    onEvent(LoginDetailEvent.OnUpgradeClick)
                }
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
                onEvent = {}
            )
        }
    }
}
