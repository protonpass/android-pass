package proton.android.pass.featurecreateitem.impl.login

import androidx.compose.runtime.Immutable
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.pass.domain.ShareId
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.featurecreateitem.impl.IsSentToTrashState
import proton.android.pass.featurecreateitem.impl.ItemSavedState

@Immutable
data class CreateUpdateLoginUiState(
    val shareId: Option<ShareId>,
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
            shareId = None,
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
