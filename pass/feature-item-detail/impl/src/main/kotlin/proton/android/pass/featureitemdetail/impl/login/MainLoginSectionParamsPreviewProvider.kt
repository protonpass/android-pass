package proton.android.pass.featureitemdetail.impl.login

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.pass.domain.HiddenState

@Suppress("MagicNumber")
class MainLoginSectionParamsPreviewProvider : PreviewParameterProvider<MainLoginSectionParams> {
    override val values: Sequence<MainLoginSectionParams>
        get() = sequenceOf(
            MainLoginSectionParams(
                username = "MyUsername",
                passwordState = HiddenState.Concealed("encrypted"),
                totpUiState = null,
                showViewAlias = false
            ),
            MainLoginSectionParams(
                username = "MyUsername",
                passwordState = HiddenState.Concealed("encrypted"),
                totpUiState = null,
                showViewAlias = true
            ),
            MainLoginSectionParams(
                username = "MyUsername",
                passwordState = HiddenState.Revealed("encrypted", "clearText"),
                totpUiState = null,
                showViewAlias = false
            ),
            MainLoginSectionParams(
                username = "MyUsername",
                passwordState = HiddenState.Revealed("encrypted", "clearText"),
                totpUiState = TotpUiState.Visible("123456", 12, 20),
                showViewAlias = false
            ),
            MainLoginSectionParams(
                username = "MyUsername",
                passwordState = HiddenState.Revealed("encrypted", "clearText"),
                totpUiState = TotpUiState.Limited,
                showViewAlias = false
            ),

            // Hidden sections
            MainLoginSectionParams(
                username = "",
                passwordState = HiddenState.Concealed("encrypted"),
                totpUiState = TotpUiState.Visible("123456", 12, 20),
                showViewAlias = false
            ),
            MainLoginSectionParams(
                username = "MyUsername",
                passwordState = HiddenState.Empty(""),
                totpUiState = TotpUiState.Visible("123456", 12, 20),
                showViewAlias = false
            ),
            MainLoginSectionParams(
                username = "MyUsername",
                passwordState = HiddenState.Concealed("encrypted"),
                totpUiState = TotpUiState.Hidden,
                showViewAlias = false
            ),
            MainLoginSectionParams(
                username = "",
                passwordState = HiddenState.Empty(""),
                totpUiState = TotpUiState.Visible("123456", 12, 20),
                showViewAlias = false
            ),
            MainLoginSectionParams(
                username = "",
                passwordState = HiddenState.Concealed("encrypted"),
                totpUiState = TotpUiState.Hidden,
                showViewAlias = false
            ),
            MainLoginSectionParams(
                username = "MyUsername",
                passwordState = HiddenState.Empty(""),
                totpUiState = TotpUiState.Hidden,
                showViewAlias = false
            ),
        )
}

data class MainLoginSectionParams(
    val username: String,
    val passwordState: HiddenState,
    val totpUiState: TotpUiState?,
    val showViewAlias: Boolean
)
