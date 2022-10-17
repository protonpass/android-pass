package me.proton.android.pass.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.pass.common.api.Result
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

    val initialState = State.Loading
    val state: MutableStateFlow<State> = MutableStateFlow(initialState)

    fun setContent(shareId: String, itemId: String) = viewModelScope.launch {
        accountManager.getPrimaryUserId()
            .first { userId -> userId != null }
            ?.let { userId ->
                when (
                    val itemResult: Result<Item> =
                        itemRepository.getById(userId, ShareId(shareId), ItemId(itemId))
                ) {
                    is Result.Error -> {
                        // no-op
                    }
                    Result.Loading -> {
                        // no-op
                    }
                    is Result.Success ->
                        state.value = State.Content(
                            FullItemUiModel(
                                name = itemResult.data.title.decrypt(cryptoContext.keyStoreCrypto),
                                item = itemResult.data
                            )
                        )
                }
            }
    }

    fun sendItemToTrash(item: Item?) = viewModelScope.launch {
        if (item == null) return@launch

        val userId = accountManager.getPrimaryUserId().first { userId -> userId != null }
        if (userId != null) {
            state.value = State.Loading
            trashItem.invoke(userId, item.shareId, item.id)
            state.value = State.ItemSentToTrash
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
}
