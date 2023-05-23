package proton.android.pass.featureitemdetail.impl.login

import androidx.compose.runtime.Stable
import proton.android.pass.common.api.Option
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import proton.pass.domain.Vault

sealed interface LoginDetailUiState {

    @Stable
    object NotInitialised : LoginDetailUiState

    @Stable
    object Error : LoginDetailUiState

    @Stable
    data class Success(
        val itemUiModel: ItemUiModel,
        val vault: Vault?,
        val passwordState: PasswordState,
        val totpUiState: TotpUiState?,
        val linkedAlias: Option<LinkedAliasItem>,
        val isLoading: Boolean,
        val isItemSentToTrash: Boolean,
        val isPermanentlyDeleted: Boolean,
        val isRestoredFromTrash: Boolean,
        val canMigrate: Boolean,
    ) : LoginDetailUiState
}

sealed interface TotpUiState {
    object Hidden : TotpUiState

    @Stable
    data class Visible(
        val code: String,
        val remainingSeconds: Int,
        val totalSeconds: Int
    ) : TotpUiState
}

@Stable
data class LinkedAliasItem(
    val shareId: ShareId,
    val itemId: ItemId
)
