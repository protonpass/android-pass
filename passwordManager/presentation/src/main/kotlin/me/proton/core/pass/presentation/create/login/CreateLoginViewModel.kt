package me.proton.core.pass.presentation.create.login

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.pass.common.api.Result
import me.proton.core.pass.domain.Item
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.domain.usecases.CreateItem
import me.proton.core.pass.domain.usecases.ObserveActiveShare
import me.proton.core.pass.presentation.uievents.IsLoadingState
import me.proton.core.pass.presentation.uievents.ItemSavedState
import javax.inject.Inject

@HiltViewModel
class CreateLoginViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val createItem: CreateItem,
    private val observeActiveShare: ObserveActiveShare
) : BaseLoginViewModel() {

    fun setInitialContents(initialContents: InitialCreateLoginContents) = viewModelScope.launch {
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


        val newState = loginItemState.value.copy(
            title = initialContents.title ?: currentValue.title,
            username = initialContents.username ?: currentValue.username,
            password = initialContents.password ?: currentValue.password,
            websiteAddresses = websites
        )

        loginItemState.value = newState
    }

    fun createItem() = viewModelScope.launch {
        when (val shareIdResult = observeActiveShare().firstOrNull()) {
            is Result.Success -> {
                shareIdResult.data?.let {
                    createItem(it)
                }
            }
            else -> {
                // no-op
            }
        }
    }

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
                    when (
                        val itemResult: Result<Item> =
                            createItem(userId, shareId, itemContents)
                    ) {
                        is Result.Success -> {
                            isLoadingState.value = IsLoadingState.NotLoading
                            isItemSavedState.value = ItemSavedState.Success(itemResult.data.id)
                        }
                        else -> {
                            // no-op
                        }
                    }
                }
        }
    }
}
