package me.proton.pass.presentation.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.android.pass.commonuimodels.api.ItemUiModel
import me.proton.android.pass.crypto.api.context.EncryptionContextProvider
import me.proton.android.pass.data.api.repositories.ItemRepository
import me.proton.android.pass.data.api.usecases.ObserveTrashedItems
import me.proton.android.pass.data.api.usecases.RefreshContent
import me.proton.android.pass.log.api.PassLogger
import me.proton.android.pass.notifications.api.SnackbarMessageRepository
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.common.api.onError
import me.proton.pass.commonui.api.toUiModel
import me.proton.pass.presentation.trash.TrashSnackbarMessage.ClearTrashError
import me.proton.pass.presentation.trash.TrashSnackbarMessage.DeleteItemError
import me.proton.pass.presentation.trash.TrashSnackbarMessage.ObserveItemsError
import me.proton.pass.presentation.trash.TrashSnackbarMessage.RefreshError
import me.proton.pass.presentation.trash.TrashSnackbarMessage.RestoreItemsError
import me.proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import me.proton.android.pass.composecomponents.impl.uievents.IsRefreshingState
import javax.inject.Inject

@HiltViewModel
class TrashScreenViewModel @Inject constructor(
    private val accountManager: AccountManager,
    observeTrashedItems: ObserveTrashedItems,
    private val itemRepository: ItemRepository,
    private val refreshContent: RefreshContent,
    private val snackbarMessageRepository: SnackbarMessageRepository,
    private val encryptionContextProvider: EncryptionContextProvider
) : ViewModel() {

    private val isLoading: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)
    private val isRefreshing: MutableStateFlow<IsRefreshingState> =
        MutableStateFlow(IsRefreshingState.NotRefreshing)

    val uiState: StateFlow<TrashUiState> = combine(
        observeTrashedItems(),
        isRefreshing,
        isLoading
    ) { itemsResult, refreshing, loading ->

        val isLoading =
            IsLoadingState.from(itemsResult is Result.Loading || loading is IsLoadingState.Loading)
        val items = when (itemsResult) {
            Result.Loading -> emptyList()
            is Result.Error -> {
                val defaultMessage = "Observe trash items error"
                PassLogger.i(
                    TAG,
                    itemsResult.exception ?: Exception(defaultMessage),
                    defaultMessage
                )
                snackbarMessageRepository.emitSnackbarMessage(ObserveItemsError)
                emptyList()
            }
            is Result.Success -> {
                encryptionContextProvider.withEncryptionContext {
                    itemsResult.data.map { it.toUiModel(this@withEncryptionContext) }
                }
            }
        }

        TrashUiState(
            isLoading = isLoading,
            isRefreshing = refreshing,
            items = items.toImmutableList()
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TrashUiState.Loading
        )

    fun restoreItem(item: ItemUiModel) = viewModelScope.launch {
        withUserId {
            itemRepository.untrashItem(it, item.shareId, item.id)
        }
    }

    fun deleteItem(item: ItemUiModel) = viewModelScope.launch {
        withUserId {
            itemRepository.deleteItem(it, item.shareId, item.id)
                .onError {
                    val message = "Error deleting item"
                    PassLogger.i(TAG, it ?: Exception(message), message)
                    snackbarMessageRepository.emitSnackbarMessage(DeleteItemError)
                }
        }
    }

    fun clearTrash() = viewModelScope.launch {
        withUserId {
            isLoading.update { IsLoadingState.Loading }
            itemRepository.clearTrash(it)
                .onError {
                    val message = "Error clearing trash"
                    PassLogger.i(TAG, it ?: Exception(message), message)
                    snackbarMessageRepository.emitSnackbarMessage(ClearTrashError)
                }
            isLoading.update { IsLoadingState.NotLoading }
        }
    }

    fun restoreItems() = viewModelScope.launch {
        withUserId {
            isLoading.update { IsLoadingState.Loading }
            itemRepository.restoreItems(it)
                .onError {
                    val message = "Error restoring items"
                    PassLogger.i(TAG, it ?: Exception(message), message)
                    snackbarMessageRepository.emitSnackbarMessage(RestoreItemsError)
                }
            isLoading.update { IsLoadingState.NotLoading }
        }
    }

    fun onRefresh() = viewModelScope.launch {
        withUserId {
            isRefreshing.update { IsRefreshingState.Refreshing }
            refreshContent(it)
                .onError {
                    val message = "Error in refresh"
                    PassLogger.i(TAG, it ?: Exception(message), message)
                    snackbarMessageRepository.emitSnackbarMessage(RefreshError)
                }
            isRefreshing.update { IsRefreshingState.NotRefreshing }
        }
    }

    private suspend fun withUserId(block: suspend (UserId) -> Unit) {
        val userId = accountManager.getPrimaryUserId().first { userId -> userId != null }
        userId?.let { block(it) }
    }

    companion object {
        private const val TAG = "TrashScreenViewModel"
    }
}
