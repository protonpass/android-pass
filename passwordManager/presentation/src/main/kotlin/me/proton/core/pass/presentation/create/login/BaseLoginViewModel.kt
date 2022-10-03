package me.proton.core.pass.presentation.create.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.core.pass.presentation.uievents.IsLoadingState
import me.proton.core.pass.presentation.uievents.ItemSavedState

abstract class BaseLoginViewModel : ViewModel() {

    protected val loginItemState: MutableStateFlow<LoginItem> = MutableStateFlow(LoginItem.Empty)
    protected val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)
    protected val isItemSavedState: MutableStateFlow<ItemSavedState> =
        MutableStateFlow(ItemSavedState.Unknown)
    protected val loginItemValidationErrorsState: MutableStateFlow<Set<LoginItemValidationErrors>> =
        MutableStateFlow(emptySet())

    val loginUiState: StateFlow<CreateUpdateLoginUiState> = combine(
        loginItemState,
        isLoadingState,
        isItemSavedState,
        loginItemValidationErrorsState
    ) { loginItem, isLoading, isItemSaved, loginItemValidationErrors ->
        CreateUpdateLoginUiState(
            loginItem = loginItem,
            errorList = loginItemValidationErrors,
            isLoadingState = isLoading,
            isItemSaved = isItemSaved
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CreateUpdateLoginUiState.Initial
        )

    fun onTitleChange(value: String) = viewModelScope.launch {
        loginItemState.value = loginItemState.value.copy(title = value)
        loginItemValidationErrorsState.value = loginItemValidationErrorsState.value.toMutableSet()
            .apply { remove(LoginItemValidationErrors.BlankTitle) }
    }

    fun onUsernameChange(value: String) = viewModelScope.launch {
        loginItemState.value = loginItemState.value.copy(username = value)
    }

    fun onPasswordChange(value: String) = viewModelScope.launch {
        loginItemState.value = loginItemState.value.copy(password = value)
    }

    fun onWebsiteChange(value: String, index: Int) = viewModelScope.launch {
        val addresses = loginItemState.value.websiteAddresses.toMutableList()
        addresses[index] = value
        loginItemState.value = loginItemState.value.copy(websiteAddresses = addresses)
    }

    fun onAddWebsite() = viewModelScope.launch {
        val addresses = loginItemState.value.websiteAddresses.toMutableList()
        addresses.add("")
        loginItemState.value = loginItemState.value.copy(websiteAddresses = addresses)
    }

    fun onRemoveWebsite(index: Int) = viewModelScope.launch {
        val addresses = loginItemState.value.websiteAddresses.toMutableList()
        addresses.removeAt(index)
        loginItemState.value = loginItemState.value.copy(websiteAddresses = addresses)
    }

    fun onNoteChange(value: String) = viewModelScope.launch {
        loginItemState.value = loginItemState.value.copy(note = value)
    }
}

