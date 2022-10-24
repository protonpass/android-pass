package me.proton.pass.presentation.create.login

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.android.pass.notifications.api.SnackbarMessageRepository
import me.proton.pass.common.api.None
import me.proton.pass.common.api.Option
import me.proton.pass.common.api.Result
import me.proton.pass.common.api.Some
import me.proton.pass.domain.ShareId
import me.proton.pass.domain.usecases.ObserveActiveShare
import me.proton.pass.presentation.uievents.IsLoadingState
import me.proton.pass.presentation.uievents.ItemSavedState

abstract class BaseLoginViewModel(
    private val snackbarMessageRepository: SnackbarMessageRepository,
    observeActiveShare: ObserveActiveShare,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    protected val shareId: Option<ShareId> =
        Option.fromNullable(savedStateHandle.get<String>("shareId")?.let { ShareId(it) })

    private val activeShareIdState: StateFlow<Option<ShareId>> = MutableStateFlow(shareId)
        .flatMapLatest { option ->
            when (option) {
                None -> observeActiveShare()
                    .distinctUntilChanged()
                    .map { result ->
                        when (result) {
                            is Result.Error -> None
                            Result.Loading -> None
                            is Result.Success -> Option.fromNullable(result.data)
                        }
                    }
                is Some -> flowOf(option)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = shareId
        )
    protected val loginItemState: MutableStateFlow<LoginItem> = MutableStateFlow(LoginItem.Empty)
    protected val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)
    protected val isItemSavedState: MutableStateFlow<ItemSavedState> =
        MutableStateFlow(ItemSavedState.Unknown)
    protected val loginItemValidationErrorsState: MutableStateFlow<Set<LoginItemValidationErrors>> =
        MutableStateFlow(emptySet())

    private val loginItemWrapperState = combine(
        loginItemState,
        loginItemValidationErrorsState
    ) { loginItem, loginItemValidationErrors ->
        LoginItemWrapper(loginItem, loginItemValidationErrors)
    }

    private data class LoginItemWrapper(
        val loginItem: LoginItem,
        val loginItemValidationErrors: Set<LoginItemValidationErrors>
    )

    val loginUiState: StateFlow<CreateUpdateLoginUiState> = combine(
        activeShareIdState,
        loginItemWrapperState,
        isLoadingState,
        isItemSavedState
    ) { shareId, loginItemWrapper, isLoading, isItemSaved ->
        CreateUpdateLoginUiState(
            shareId = shareId,
            loginItem = loginItemWrapper.loginItem,
            validationErrors = loginItemWrapper.loginItemValidationErrors,
            isLoadingState = isLoading,
            isItemSaved = isItemSaved
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CreateUpdateLoginUiState.Initial
        )

    fun onTitleChange(value: String) {
        loginItemState.update { it.copy(title = value) }
        loginItemValidationErrorsState.update {
            it.toMutableSet().apply { remove(LoginItemValidationErrors.BlankTitle) }
        }
    }

    fun onUsernameChange(value: String) {
        loginItemState.update { it.copy(username = value) }
    }

    fun onPasswordChange(value: String) {
        loginItemState.update { it.copy(password = value) }
    }

    fun onWebsiteChange(value: String, index: Int) {
        loginItemState.update {
            it.copy(
                websiteAddresses = it.websiteAddresses.toMutableList()
                    .apply { this[index] = value }
            )
        }
    }

    fun onAddWebsite() {
        loginItemState.update {
            it.copy(websiteAddresses = it.websiteAddresses.toMutableList().apply { add("") })
        }
    }

    fun onRemoveWebsite(index: Int) {
        loginItemState.update {
            it.copy(
                websiteAddresses = it.websiteAddresses.toMutableList().apply { removeAt(index) }
            )
        }
    }

    fun onNoteChange(value: String) {
        loginItemState.update { it.copy(note = value) }
    }

    fun onEmitSnackbarMessage(snackbarMessage: LoginSnackbarMessages) =
        viewModelScope.launch {
            snackbarMessageRepository.emitSnackbarMessage(snackbarMessage)
        }
}

