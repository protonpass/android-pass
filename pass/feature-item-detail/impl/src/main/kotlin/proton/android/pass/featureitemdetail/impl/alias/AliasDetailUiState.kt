package proton.android.pass.featureitemdetail.impl.alias

import androidx.compose.runtime.Stable
import proton.pass.domain.ItemId
import proton.pass.domain.ItemType
import proton.pass.domain.ShareId

sealed interface AliasDetailUiState {

    @Stable
    object NotInitialised : AliasDetailUiState

    @Stable
    object Error : AliasDetailUiState

    @Stable
    data class Success(
        val shareId: ShareId,
        val itemId: ItemId,
        val itemType: ItemType,
        val state: Int,
        val isLoading: Boolean,
        val isLoadingMailboxes: Boolean,
        val isItemSentToTrash: Boolean,
        val model: AliasUiModel?,
    ) : AliasDetailUiState
}
