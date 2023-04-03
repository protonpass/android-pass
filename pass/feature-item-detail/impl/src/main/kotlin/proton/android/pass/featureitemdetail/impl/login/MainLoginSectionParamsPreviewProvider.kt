package proton.android.pass.featureitemdetail.impl.login

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

@Suppress("MagicNumber")
class MainLoginSectionParamsPreviewProvider : PreviewParameterProvider<MainLoginSectionParams> {
    override val values: Sequence<MainLoginSectionParams>
        get() = sequenceOf(
            MainLoginSectionParams(
                username = "MyUsername",
                passwordState = PasswordState.Concealed("encrypted"),
                totpUiState = null
            ),
            MainLoginSectionParams(
                username = "MyUsername",
                passwordState = PasswordState.Revealed("encrypted", "clearText"),
                totpUiState = null
            ),
            MainLoginSectionParams(
                username = "MyUsername",
                passwordState = PasswordState.Revealed("encrypted", "clearText"),
                totpUiState = TotpUiState("123456", 12, 20),
            )
        )
}

data class MainLoginSectionParams(
    val username: String,
    val passwordState: PasswordState,
    val totpUiState: TotpUiState?
)
