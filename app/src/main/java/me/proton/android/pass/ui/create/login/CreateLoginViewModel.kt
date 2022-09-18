package me.proton.android.pass.ui.create.login

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.proton.android.pass.ui.shared.uievents.IsLoadingState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.domain.usecases.CreateItem
import javax.inject.Inject

@HiltViewModel
class CreateLoginViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val createItem: CreateItem
) : BaseLoginViewModel() {

    fun createItem(shareId: ShareId) = viewModelScope.launch {
        val loginItem = loginItemState.value
        val loginItemValidationErrors = loginItem.validate()
        if (loginItemValidationErrors.isNotEmpty()) {
            loginItemValidationErrorsState.value = loginItemValidationErrors
        } else {
            isLoadingState.value = IsLoadingState.Loading
            accountManager.getPrimaryUserId()
                .first { userId -> userId != null }
                ?.let { userId ->
                    val itemContents = loginItem.toItemContents()
                    val createdItem = createItem(userId, shareId, itemContents)
                    isLoadingState.value = IsLoadingState.NotLoading
                    isItemSavedState.value = ItemSavedState.Success(createdItem.id)
                }
        }
    }
}
