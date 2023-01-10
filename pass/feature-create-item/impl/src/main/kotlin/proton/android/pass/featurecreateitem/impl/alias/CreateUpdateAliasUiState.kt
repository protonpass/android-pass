package proton.android.pass.featurecreateitem.impl.alias

import androidx.compose.runtime.Immutable
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.pass.domain.ShareId

@Immutable
data class CreateUpdateAliasUiState(
    val shareId: ShareId?,
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
            shareId = null,
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
