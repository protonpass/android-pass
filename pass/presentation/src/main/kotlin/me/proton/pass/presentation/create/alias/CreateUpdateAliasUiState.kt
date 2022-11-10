package me.proton.pass.presentation.create.alias

import androidx.compose.runtime.Immutable
import me.proton.pass.common.api.None
import me.proton.pass.common.api.Option
import me.proton.pass.domain.ShareId
import me.proton.pass.presentation.uievents.AliasSavedState
import me.proton.pass.presentation.uievents.IsButtonEnabled
import me.proton.pass.presentation.uievents.IsLoadingState

@Immutable
data class CreateUpdateAliasUiState(
    val shareId: Option<ShareId>,
    val aliasItem: AliasItem,
    val errorList: Set<AliasItemValidationErrors>,
    val isLoadingState: IsLoadingState,
    val isAliasSavedState: AliasSavedState,
    val isApplyButtonEnabled: IsButtonEnabled
) {
    companion object {
        val Initial = CreateUpdateAliasUiState(
            shareId = None,
            isLoadingState = IsLoadingState.Loading,
            aliasItem = AliasItem.Empty,
            errorList = emptySet(),
            isAliasSavedState = AliasSavedState.Unknown,
            isApplyButtonEnabled = IsButtonEnabled.Disabled
        )
    }
}
