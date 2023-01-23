package proton.android.pass.featurecreateitem.impl.login

import androidx.compose.runtime.Immutable
import proton.android.pass.commonuimodels.api.ShareUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.featurecreateitem.impl.IsSentToTrashState
import proton.android.pass.featurecreateitem.impl.ItemSavedState

@Immutable
data class CreateUpdateLoginUiState(
    val shareList: List<ShareUiModel>,
    val selectedShareId: ShareUiModel?,
    val loginItem: LoginItem,
    val validationErrors: Set<LoginItemValidationErrors>,
    val isLoadingState: IsLoadingState,
    val isItemSaved: ItemSavedState,
    val focusLastWebsite: Boolean,
    val canUpdateUsername: Boolean,
    val isItemSentToTrash: IsSentToTrashState
) {
    companion object {
        val Initial = CreateUpdateLoginUiState(
            shareList = emptyList(),
            selectedShareId = null,
            isLoadingState = IsLoadingState.NotLoading,
            loginItem = LoginItem.Empty,
            validationErrors = emptySet(),
            isItemSaved = ItemSavedState.Unknown,
            focusLastWebsite = false,
            canUpdateUsername = true,
            isItemSentToTrash = IsSentToTrashState.NotSent
        )
    }
}
