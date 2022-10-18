package me.proton.core.pass.presentation.create.login

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.android.pass.log.PassLogger
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.pass.common.api.None
import me.proton.core.pass.common.api.Some
import me.proton.core.pass.common.api.onError
import me.proton.core.pass.common.api.onSuccess
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.domain.usecases.CreateItem
import me.proton.core.pass.domain.usecases.ObserveActiveShare
import me.proton.core.pass.presentation.create.login.LoginSnackbarMessages.ItemCreationError
import me.proton.core.pass.presentation.uievents.IsLoadingState
import me.proton.core.pass.presentation.uievents.ItemSavedState
import javax.inject.Inject

@HiltViewModel
class CreateLoginViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val createItem: CreateItem,
    observeActiveShare: ObserveActiveShare,
    savedStateHandle: SavedStateHandle
) : BaseLoginViewModel(observeActiveShare, savedStateHandle) {

    fun setInitialContents(initialContents: InitialCreateLoginContents) {
        val currentValue = loginItemState.value
        val websites = currentValue.websiteAddresses.toMutableList()

        if (initialContents.url != null) {
            // Check if we are in the initial state, and if so, clear the list
            if (websites.size == 1 && websites.first().isEmpty()) {
                websites.clear()
                websites.add(initialContents.url)
            } else if (!websites.contains(initialContents.url)) {
                websites.add(initialContents.url)
            }
        }

        loginItemState.update {
            it.copy(
                title = initialContents.title ?: currentValue.title,
                username = initialContents.username ?: currentValue.username,
                password = initialContents.password ?: currentValue.password,
                websiteAddresses = websites
            )
        }
    }

    fun createItem() = viewModelScope.launch {
        when (val shareId = loginUiState.value.shareId) {
            None -> mutableSnackbarMessage.tryEmit(ItemCreationError)
            is Some -> createItem(shareId.value)
        }
    }

    fun createItem(shareId: ShareId) = viewModelScope.launch {
        val loginItem = loginItemState.value
        val loginItemValidationErrors = loginItem.validate()
        if (loginItemValidationErrors.isNotEmpty()) {
            loginItemValidationErrorsState.update { loginItemValidationErrors }
        } else {
            isLoadingState.update { IsLoadingState.Loading }
            val userId = accountManager.getPrimaryUserId()
                .firstOrNull { userId -> userId != null }
            if (userId != null) {
                createItem(userId, shareId, loginItem.toItemContents())
                    .onSuccess { item ->
                        isItemSavedState.update { ItemSavedState.Success(item.id) }
                    }
                    .onError {
                        val defaultMessage = "Could not create item"
                        PassLogger.i(TAG, it ?: Exception(defaultMessage), defaultMessage)
                        mutableSnackbarMessage.tryEmit(ItemCreationError)
                    }
            } else {
                mutableSnackbarMessage.tryEmit(ItemCreationError)
            }
            isLoadingState.update { IsLoadingState.NotLoading }
        }
    }

    companion object {
        private const val TAG = "CreateLoginViewModel"
    }
}
