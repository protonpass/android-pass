package proton.android.pass.featureitemdetail.impl.login

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

@Suppress("MagicNumber")
class MainLoginSectionParamsPreviewProvider : PreviewParameterProvider<MainLoginSectionParams> {
    override val values: Sequence<MainLoginSectionParams>
        get() = sequenceOf(
            MainLoginSectionParams(
                username = "MyUsername",
                passwordState = PasswordState.Concealed("encrypted"),
                totpUiState = null,
                showViewAlias = false
            ),
            MainLoginSectionParams(
                username = "MyUsername",
                passwordState = PasswordState.Concealed("encrypted"),
                totpUiState = null,
                showViewAlias = true
            ),
            MainLoginSectionParams(
                username = "MyUsername",
                passwordState = PasswordState.Revealed("encrypted", "clearText"),
                totpUiState = null,
                showViewAlias = false
            ),
            MainLoginSectionParams(
                username = "MyUsername",
                passwordState = PasswordState.Revealed("encrypted", "clearText"),
                totpUiState = TotpUiState.Visible("123456", 12, 20),
                showViewAlias = false
            ),
            MainLoginSectionParams(
                username = "MyUsername",
                passwordState = PasswordState.Revealed("encrypted", "clearText"),
                totpUiState = TotpUiState.Hidden,
                showViewAlias = false
            )
        )
}

data class MainLoginSectionParams(
    val username: String,
    val passwordState: PasswordState,
    val totpUiState: TotpUiState?,
    val showViewAlias: Boolean
)
