package me.proton.android.pass.ui.create.alias

import androidx.compose.runtime.Immutable
import me.proton.android.pass.ui.shared.uievents.IsLoadingState
import me.proton.android.pass.ui.shared.uievents.ItemSavedState

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
