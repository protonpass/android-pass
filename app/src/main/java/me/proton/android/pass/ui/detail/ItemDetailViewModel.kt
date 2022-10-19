package me.proton.android.pass.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.android.pass.log.PassLogger
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.pass.common.api.onError
import me.proton.core.pass.common.api.onSuccess
import me.proton.core.pass.domain.Item
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.domain.repositories.ItemRepository
import me.proton.core.pass.domain.usecases.TrashItem
import javax.inject.Inject

@HiltViewModel
class ItemDetailViewModel @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val accountManager: AccountManager,
    private val itemRepository: ItemRepository,
    private val trashItem: TrashItem
) : ViewModel() {

    private val _state: MutableStateFlow<State> = MutableStateFlow(State.Loading)
    val state: StateFlow<State> = _state
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = State.Loading
        )

    fun setContent(shareId: String, itemId: String) = viewModelScope.launch {
        accountManager.getPrimaryUserId()
            .first { userId -> userId != null }
            ?.let { userId ->
                itemRepository.getById(userId, ShareId(shareId), ItemId(itemId))
                    .onSuccess { item ->
                        _state.value = State.Content(
                            FullItemUiModel(
                                name = item.title.decrypt(cryptoContext.keyStoreCrypto),
                                item = item
                            )
                        )
                    }
                    .onError {
                        val defaultMessage = "Get by id error"
                        PassLogger.i(TAG, it ?: Exception(defaultMessage), defaultMessage)
                    }
            }
    }

    fun sendItemToTrash(item: Item?) = viewModelScope.launch {
        if (item == null) return@launch

        val userId = accountManager.getPrimaryUserId().first { userId -> userId != null }
        if (userId != null) {
            _state.value = State.Loading
            trashItem.invoke(userId, item.shareId, item.id)
            _state.value = State.ItemSentToTrash
        }
    }

    sealed class State {
        object Loading : State()
        data class Content(val model: FullItemUiModel) : State()
        data class Error(val message: String) : State()
        object ItemSentToTrash : State()
    }

    data class FullItemUiModel(
        val name: String,
        val item: Item
    )

    companion object {
        private const val TAG = "ItemDetailViewModel"
    }
}
