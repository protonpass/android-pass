package me.proton.android.pass.ui.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.android.pass.extension.toUiModel
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.domain.entity.UserId
import me.proton.core.pass.common.api.Result
import me.proton.core.pass.domain.repositories.ItemRepository
import me.proton.core.pass.domain.usecases.ObserveTrashedItems
import me.proton.core.pass.presentation.components.model.ItemUiModel
import javax.inject.Inject

@HiltViewModel
class TrashScreenViewModel @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val accountManager: AccountManager,
    observeTrashedItems: ObserveTrashedItems,
    private val itemRepository: ItemRepository
) : ViewModel() {

    val uiState: StateFlow<TrashUiState> = observeTrashedItems()
        .map { result ->
            when (result) {
                is Result.Success -> {
                    TrashUiState.Content(result.data.map { it.toUiModel(cryptoContext) })
                }
                is Result.Error -> TrashUiState.Error(
                    result.exception?.message ?: "Observe trash items errored"
                )
                Result.Loading -> TrashUiState.Loading
            }
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

    private suspend fun withUserId(block: suspend (UserId) -> Unit) {
        val userId = accountManager.getPrimaryUserId().first { userId -> userId != null }
        userId?.let { block(it) }
    }
}
