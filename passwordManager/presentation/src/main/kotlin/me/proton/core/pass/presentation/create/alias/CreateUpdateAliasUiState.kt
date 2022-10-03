package me.proton.core.pass.presentation.create.alias

import androidx.compose.runtime.Immutable
import me.proton.core.pass.presentation.uievents.IsLoadingState
import me.proton.core.pass.presentation.uievents.ItemSavedState

@Immutable
data class CreateUpdateAliasUiState(
    val aliasItem: AliasItem,
    val errorList: Set<AliasItemValidationErrors>,
    val isLoadingState: IsLoadingState,
    val isItemSaved: ItemSavedState
) {
    companion object {
        val Initial = CreateUpdateAliasUiState(
            isLoadingState = IsLoadingState.NotLoading,
            aliasItem = AliasItem.Empty,
            errorList = emptySet(),
            isItemSaved = ItemSavedState.Unknown
        )
    }
}
