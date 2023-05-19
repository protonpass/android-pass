package proton.android.pass.featureitemcreate.impl.alias

import androidx.compose.runtime.Immutable
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.featureitemcreate.impl.login.ShareUiState
import proton.pass.domain.ShareId

@Immutable
data class BaseAliasUiState(
    val aliasItem: AliasItem,
    val isDraft: Boolean,
    val errorList: Set<AliasItemValidationErrors>,
    val isLoadingState: IsLoadingState,
    val isAliasSavedState: AliasSavedState,
    val isAliasDraftSavedState: AliasDraftSavedState,
    val isApplyButtonEnabled: IsButtonEnabled,
    val closeScreenEvent: CloseScreenEvent,
    val hasUserEditedContent: Boolean,
    val hasReachedAliasLimit: Boolean,
    val canUpgrade: Boolean
) {
    companion object {
        val Initial = BaseAliasUiState(
            aliasItem = AliasItem.Empty,
            isDraft = false,
            errorList = emptySet(),
            isLoadingState = IsLoadingState.Loading,
            isAliasSavedState = AliasSavedState.Unknown,
            isAliasDraftSavedState = AliasDraftSavedState.Unknown,
            isApplyButtonEnabled = IsButtonEnabled.Disabled,
            closeScreenEvent = CloseScreenEvent.NotClose,
            hasUserEditedContent = false,
            hasReachedAliasLimit = false,
            canUpgrade = false
        )
    }
}

@Immutable
data class CreateAliasUiState(
    val shareUiState: ShareUiState,
    val baseAliasUiState: BaseAliasUiState
) {
    companion object {
        val Initial = CreateAliasUiState(
            shareUiState = ShareUiState.NotInitialised,
            baseAliasUiState = BaseAliasUiState.Initial
        )
    }
}

@Immutable
data class UpdateAliasUiState(
    val selectedShareId: ShareId?,
    val baseAliasUiState: BaseAliasUiState
) {
    companion object {
        val Initial = UpdateAliasUiState(
            selectedShareId = null,
            baseAliasUiState = BaseAliasUiState.Initial
        )
    }
}
