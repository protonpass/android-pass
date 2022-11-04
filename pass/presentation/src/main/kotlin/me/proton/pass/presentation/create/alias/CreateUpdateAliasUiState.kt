package me.proton.pass.presentation.create.alias

import androidx.compose.runtime.Immutable
import me.proton.pass.common.api.None
import me.proton.pass.common.api.Option
import me.proton.pass.domain.ShareId
import me.proton.pass.presentation.uievents.IsButtonEnabled
import me.proton.pass.presentation.uievents.IsLoadingState
import me.proton.pass.presentation.uievents.ItemSavedState

@Immutable
data class CreateUpdateAliasUiState(
    val shareId: Option<ShareId>,
    val aliasItem: AliasItem,
    val errorList: Set<AliasItemValidationErrors>,
    val isLoadingState: IsLoadingState,
    val isItemSaved: ItemSavedState,
    val isApplyButtonEnabled: IsButtonEnabled
) {
    companion object {
        val Initial = CreateUpdateAliasUiState(
            shareId = None,
            isLoadingState = IsLoadingState.Loading,
            aliasItem = AliasItem.Empty,
            errorList = emptySet(),
            isItemSaved = ItemSavedState.Unknown,
            isApplyButtonEnabled = IsButtonEnabled.Disabled
        )
    }
}
