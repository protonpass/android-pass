package proton.android.pass.featureitemdetail.impl.login

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.collections.immutable.persistentListOf

@Suppress("MagicNumber")
class LoginDetailUiStatePreviewProvider : PreviewParameterProvider<LoginDetailUiState> {
    override val values: Sequence<LoginDetailUiState>
        get() = sequenceOf(
            LoginDetailUiState(
                title = "",
                username = "MyUsername",
                password = PasswordState.Concealed("encrypted"),
                websites = persistentListOf(),
                packageNames = persistentListOf(),
                note = "",
                totpUiState = null
            ),
            LoginDetailUiState(
                title = "",
                username = "MyUsername",
                password = PasswordState.Revealed("encrypted", "clearText"),
                websites = persistentListOf(),
                packageNames = persistentListOf(),
                note = "",
                totpUiState = null
            ),
            LoginDetailUiState(
                title = "",
                username = "MyUsername",
                password = PasswordState.Revealed("encrypted", "clearText"),
                websites = persistentListOf(),
                packageNames = persistentListOf(),
                note = "",
                totpUiState = TotpUiState("123456", 12, 20)
            )
        )
}
