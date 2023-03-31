package proton.android.pass.featureitemdetail.impl.login

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.pass.domain.ItemId
import proton.pass.domain.ItemType
import proton.pass.domain.ShareId

sealed interface LoginDetailUiState {

    @Stable
    object NotInitialised : LoginDetailUiState

    @Stable
    object Error : LoginDetailUiState

    @Stable
    data class Success(
        val shareId: ShareId,
        val itemId: ItemId,
        val itemType: ItemType,
        val title: String,
        val username: String,
        val password: PasswordState,
        val websites: ImmutableList<String>,
        val packageInfoSet: ImmutableSet<PackageInfoUi>,
        val note: String,
        val totpUiState: TotpUiState?,
        val state: Int,
        val isLoading: Boolean,
        val isItemSentToTrash: Boolean
    ) : LoginDetailUiState
}

@Stable
data class TotpUiState(
    val code: String,
    val remainingSeconds: Int,
    val totalSeconds: Int
)
