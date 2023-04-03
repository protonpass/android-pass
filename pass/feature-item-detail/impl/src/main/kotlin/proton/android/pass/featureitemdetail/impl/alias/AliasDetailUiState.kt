package proton.android.pass.featureitemdetail.impl.alias

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.PersistentList
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.pass.domain.AliasMailbox

sealed interface AliasDetailUiState {

    @Stable
    object NotInitialised : AliasDetailUiState

    @Stable
    object Error : AliasDetailUiState

    @Stable
    data class Success(
        val itemUiModel: ItemUiModel,
        val mailboxes: PersistentList<AliasMailbox>,
        val isLoading: Boolean,
        val isLoadingMailboxes: Boolean,
        val isItemSentToTrash: Boolean,
    ) : AliasDetailUiState
}
