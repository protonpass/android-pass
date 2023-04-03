package proton.android.pass.featureitemdetail.impl.login

import androidx.compose.runtime.Stable
import proton.android.pass.commonuimodels.api.ItemUiModel

sealed interface LoginDetailUiState {

    @Stable
    object NotInitialised : LoginDetailUiState

    @Stable
    object Error : LoginDetailUiState

    @Stable
    data class Success(
        val itemUiModel: ItemUiModel,
        val passwordState: PasswordState,
        val totpUiState: TotpUiState?,
        val isLoading: Boolean,
        val isItemSentToTrash: Boolean,
        val isPermanentlyDeleted: Boolean
    ) : LoginDetailUiState
}

@Stable
data class TotpUiState(
    val code: String,
    val remainingSeconds: Int,
    val totalSeconds: Int
)
