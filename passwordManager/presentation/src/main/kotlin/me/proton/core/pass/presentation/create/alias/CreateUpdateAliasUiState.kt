package me.proton.core.pass.presentation.create.alias

import androidx.compose.runtime.Immutable
import me.proton.core.pass.common.api.None
import me.proton.core.pass.common.api.Option
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.presentation.uievents.IsLoadingState
import me.proton.core.pass.presentation.uievents.ItemSavedState

@Immutable
data class CreateUpdateAliasUiState(
    val shareId: Option<ShareId>,
    val aliasItem: AliasItem,
    val errorList: Set<AliasItemValidationErrors>,
    val isLoadingState: IsLoadingState,
    val isItemSaved: ItemSavedState
) {
    companion object {
        val Initial = CreateUpdateAliasUiState(
            shareId = None,
            isLoadingState = IsLoadingState.NotLoading,
            aliasItem = AliasItem.Empty,
            errorList = emptySet(),
            isItemSaved = ItemSavedState.Unknown
        )
    }
}
