package proton.android.pass.featureitemdetail.impl.login

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

@Suppress("MagicNumber")
class LoginDetailUiStatePreviewProvider : PreviewParameterProvider<LoginDetailUiState.Success> {
    override val values: Sequence<LoginDetailUiState.Success>
        get() = sequenceOf(
            LoginDetailUiState.Success(
                title = "",
                username = "MyUsername",
                password = PasswordState.Concealed("encrypted"),
                websites = persistentListOf(),
                packageInfoSet = persistentSetOf(),
                note = "",
                totpUiState = null,
                isLoading = false,
                isItemSentToTrash = false,
                shareId = ShareId(id = ""),
                itemId = ItemId(id = ""),
                itemType = proton.pass.domain.ItemType.Password,
                state = 0
            ),
            LoginDetailUiState.Success(
                title = "",
                username = "MyUsername",
                password = PasswordState.Revealed("encrypted", "clearText"),
                websites = persistentListOf(),
                packageInfoSet = persistentSetOf(),
                note = "",
                totpUiState = null,
                isLoading = false,
                isItemSentToTrash = false,
                shareId = ShareId(id = ""),
                itemId = ItemId(id = ""),
                itemType = proton.pass.domain.ItemType.Password,
                state = 0
            ),
            LoginDetailUiState.Success(
                title = "",
                username = "MyUsername",
                password = PasswordState.Revealed("encrypted", "clearText"),
                websites = persistentListOf(),
                packageInfoSet = persistentSetOf(),
                note = "",
                totpUiState = TotpUiState("123456", 12, 20),
                isLoading = false,
                isItemSentToTrash = false,
                shareId = ShareId(id = ""),
                itemId = ItemId(id = ""),
                itemType = proton.pass.domain.ItemType.Password,
                state = 0
            )
        )
}
