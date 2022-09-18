package me.proton.android.pass.ui.create.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.android.pass.ui.shared.uievents.IsLoadingState
import me.proton.core.pass.domain.ItemId

abstract class BaseLoginViewModel : ViewModel() {

    internal val loginItemState: MutableStateFlow<LoginItem> = MutableStateFlow(LoginItem.Empty)
    internal val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)
    internal val isItemSavedState: MutableStateFlow<ItemSavedState> =
        MutableStateFlow(ItemSavedState.Unknown)
    internal val loginItemValidationErrorsState: MutableStateFlow<List<LoginItemValidationErrors>> =
        MutableStateFlow(emptyList())

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

sealed interface ItemSavedState {
    object Unknown : ItemSavedState
    data class Success(val itemId: ItemId) : ItemSavedState
}
