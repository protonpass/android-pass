package me.proton.android.pass.ui.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
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
import me.proton.core.pass.common.api.None
import me.proton.core.pass.common.api.Option
import me.proton.core.pass.common.api.Result
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

    private val isRefreshing: MutableStateFlow<IsRefreshingState> = MutableStateFlow(IsRefreshingState.NotRefreshing)

    val uiState: StateFlow<TrashUiState> = combine(
        observeTrashedItems(),
        isRefreshing
    ) { itemsResult, refreshing ->

        val isLoading = IsLoadingState.from(itemsResult is Result.Loading)

        val (items, errorMessage) = when (itemsResult) {
            Result.Loading -> emptyList<ItemUiModel>() to None
            is Result.Error -> {
                val defaultMessage = "Observe trash items error"
                PassLogger.i(TAG, itemsResult.exception ?: Exception(defaultMessage), defaultMessage)
                emptyList<ItemUiModel>() to Option.fromNullable(itemsResult.exception?.message)
            }
            is Result.Success -> {
                itemsResult.data.map { it.toUiModel(cryptoContext) } to None
            }
        }

        TrashUiState(
            isLoading = isLoading,
            isRefreshing = refreshing,
            items = items,
            errorMessage = errorMessage
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
        }
    }

    fun clearTrash() = viewModelScope.launch {
        withUserId {
            itemRepository.clearTrash(it)
        }
    }

    fun onRefresh() = viewModelScope.launch {
        withUserId {
            isRefreshing.update { IsRefreshingState.Refreshing }
            refreshContent(it)
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
