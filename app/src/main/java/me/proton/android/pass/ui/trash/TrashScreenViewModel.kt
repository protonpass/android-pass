package me.proton.android.pass.ui.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.android.pass.extension.toUiModel
import me.proton.android.pass.log.PassLogger
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.domain.entity.UserId
import me.proton.core.pass.common.api.Result
import me.proton.core.pass.common.api.onError
import me.proton.core.pass.domain.repositories.ItemRepository
import me.proton.core.pass.domain.usecases.ObserveTrashedItems
import me.proton.core.pass.domain.usecases.RefreshContent
import me.proton.core.pass.presentation.components.model.ItemUiModel
import me.proton.core.pass.presentation.uievents.IsLoadingState
import me.proton.core.pass.presentation.uievents.IsRefreshingState
import javax.inject.Inject

@HiltViewModel
class TrashScreenViewModel @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val accountManager: AccountManager,
    observeTrashedItems: ObserveTrashedItems,
    private val itemRepository: ItemRepository,
    private val refreshContent: RefreshContent
) : ViewModel() {

    private val mutableSnackbarMessage: MutableSharedFlow<TrashSnackbarMessage> =
        MutableSharedFlow(extraBufferCapacity = 1)
    val snackbarMessage: SharedFlow<TrashSnackbarMessage> = mutableSnackbarMessage

    private val isLoading: MutableStateFlow<IsLoadingState> = MutableStateFlow(IsLoadingState.NotLoading)
    private val isRefreshing: MutableStateFlow<IsRefreshingState> = MutableStateFlow(IsRefreshingState.NotRefreshing)

    val uiState: StateFlow<TrashUiState> = combine(
        observeTrashedItems(),
        isRefreshing,
        isLoading
    ) { itemsResult, refreshing, loading ->

        val isLoading = IsLoadingState.from(itemsResult is Result.Loading || loading is IsLoadingState.Loading)
        val items = when (itemsResult) {
            Result.Loading -> emptyList()
            is Result.Error -> {
                val defaultMessage = "Observe trash items error"
                PassLogger.i(TAG, itemsResult.exception ?: Exception(defaultMessage), defaultMessage)
                mutableSnackbarMessage.tryEmit(TrashSnackbarMessage.ObserveItemsError)
                emptyList()
            }
            is Result.Success -> {
                itemsResult.data.map { it.toUiModel(cryptoContext) }
            }
        }

        TrashUiState(
            isLoading = isLoading,
            isRefreshing = refreshing,
            items = items
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
                    mutableSnackbarMessage.tryEmit(TrashSnackbarMessage.DeleteItemError)
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
                    mutableSnackbarMessage.tryEmit(TrashSnackbarMessage.ClearTrashError)
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
                    mutableSnackbarMessage.tryEmit(TrashSnackbarMessage.RefreshError)
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
