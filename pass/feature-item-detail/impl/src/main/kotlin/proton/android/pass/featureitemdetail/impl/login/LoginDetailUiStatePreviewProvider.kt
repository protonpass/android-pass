package proton.android.pass.featureitemdetail.impl.login

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf

@Suppress("MagicNumber")
class LoginDetailUiStatePreviewProvider : PreviewParameterProvider<LoginDetailUiState> {
    override val values: Sequence<LoginDetailUiState>
        get() = sequenceOf(
            LoginDetailUiState(
                title = "",
                username = "MyUsername",
                password = PasswordState.Concealed("encrypted"),
                websites = persistentListOf(),
                packageInfoSet = persistentSetOf(),
                note = "",
                totpUiState = null,
                isLoading = false,
                isItemSentToTrash = false
            ),
            LoginDetailUiState(
                title = "",
                username = "MyUsername",
                password = PasswordState.Revealed("encrypted", "clearText"),
                websites = persistentListOf(),
                packageInfoSet = persistentSetOf(),
                note = "",
                totpUiState = null,
                isLoading = false,
                isItemSentToTrash = false
            ),
            LoginDetailUiState(
                title = "",
                username = "MyUsername",
                password = PasswordState.Revealed("encrypted", "clearText"),
                websites = persistentListOf(),
                packageInfoSet = persistentSetOf(),
                note = "",
                totpUiState = TotpUiState("123456", 12, 20),
                isLoading = false,
                isItemSentToTrash = false
            )
        )
}
