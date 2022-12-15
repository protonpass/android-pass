package me.proton.pass.presentation.create.alias

import androidx.compose.runtime.Immutable
import me.proton.pass.common.api.None
import me.proton.pass.common.api.Option
import me.proton.pass.domain.ShareId
import me.proton.pass.presentation.uievents.AliasDraftSavedState
import me.proton.pass.presentation.uievents.AliasSavedState
import me.proton.pass.presentation.uievents.IsButtonEnabled
import me.proton.pass.presentation.uievents.IsLoadingState

@Immutable
data class CreateUpdateAliasUiState(
    val shareId: Option<ShareId>,
    val aliasItem: AliasItem,
    val isDraft: Boolean,
    val errorList: Set<AliasItemValidationErrors>,
    val isLoadingState: IsLoadingState,
    val isAliasSavedState: AliasSavedState,
    val isAliasDraftSavedState: AliasDraftSavedState,
    val isApplyButtonEnabled: IsButtonEnabled
) {
    companion object {
        val Initial = CreateUpdateAliasUiState(
            shareId = None,
            aliasItem = AliasItem.Empty,
            isDraft = false,
            errorList = emptySet(),
            isLoadingState = IsLoadingState.Loading,
            isAliasSavedState = AliasSavedState.Unknown,
            isAliasDraftSavedState = AliasDraftSavedState.Unknown,
            isApplyButtonEnabled = IsButtonEnabled.Disabled
        )
    }
}
