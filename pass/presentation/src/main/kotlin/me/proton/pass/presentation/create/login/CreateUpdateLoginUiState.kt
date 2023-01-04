package me.proton.pass.presentation.create.login

import androidx.compose.runtime.Immutable
import me.proton.pass.common.api.None
import me.proton.pass.common.api.Option
import me.proton.pass.domain.ShareId
import me.proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import me.proton.pass.presentation.create.IsSentToTrashState
import me.proton.pass.presentation.create.ItemSavedState

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
