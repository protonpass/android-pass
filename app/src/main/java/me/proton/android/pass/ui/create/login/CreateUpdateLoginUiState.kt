package me.proton.android.pass.ui.create.login

import androidx.compose.runtime.Immutable
import me.proton.android.pass.ui.shared.uievents.IsLoadingState

@Immutable
data class CreateUpdateLoginUiState(
    val loginItem: LoginItem,
    val errorList: List<LoginItemValidationErrors>,
    val isLoadingState: IsLoadingState,
    val isItemSaved: ItemSavedState
) {
    companion object {
        val Initial = CreateUpdateLoginUiState(
            isLoadingState = IsLoadingState.NotLoading,
            loginItem = LoginItem.Empty,
            errorList = emptyList(),
            isItemSaved = ItemSavedState.Unknown
        )
    }
}
