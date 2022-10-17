package me.proton.core.pass.presentation.create.note

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
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
import me.proton.core.pass.domain.usecases.GetShareById
import me.proton.core.pass.presentation.uievents.IsLoadingState
import me.proton.core.pass.presentation.uievents.ItemSavedState
import javax.inject.Inject

@HiltViewModel
class UpdateNoteViewModel @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val accountManager: AccountManager,
    private val itemRepository: ItemRepository,
    private val getShare: GetShareById
) : BaseNoteViewModel() {

    private var _item: Item? = null

    fun setItem(shareId: ShareId, itemId: ItemId) = viewModelScope.launch {
        if (_item != null) return@launch
        isLoadingState.value = IsLoadingState.Loading
        accountManager.getPrimaryUserId()
            .first { userId -> userId != null }
            ?.let { userId ->
                itemRepository.getById(userId, shareId, itemId)
                    .onSuccess { item ->
                        _item = item
                        isLoadingState.value = IsLoadingState.NotLoading
                        noteItemState.value = NoteItem(
                            title = item.title.decrypt(cryptoContext.keyStoreCrypto),
                            note = item.note.decrypt(cryptoContext.keyStoreCrypto)
                        )
                    }
                    .onError {
                        val defaultMessage = "Get by id error"
                        PassLogger.i(TAG, it ?: Exception(defaultMessage), defaultMessage)
                    }
            }
    }

    fun updateItem(shareId: ShareId) = viewModelScope.launch {
        requireNotNull(_item)
        isLoadingState.value = IsLoadingState.Loading
        val noteItem = noteItemState.value
        accountManager.getPrimaryUserId()
            .first { userId -> userId != null }
            ?.let { userId ->
                getShare(userId, shareId)
                    .onSuccess { share ->
                        requireNotNull(share)
                        val itemContents = noteItem.toItemContents()
                        itemRepository.updateItem(userId, share, _item!!, itemContents)
                            .onSuccess { item ->
                                isLoadingState.value = IsLoadingState.NotLoading
                                isItemSavedState.value = ItemSavedState.Success(item.id)
                            }
                            .onError {
                                val defaultMessage = "Update item error"
                                PassLogger.i(TAG, it ?: Exception(defaultMessage), defaultMessage)
                            }
                    }
                    .onError {
                        val defaultMessage = "Get share error"
                        PassLogger.i(TAG, it ?: Exception(defaultMessage), defaultMessage)
                    }
            }
    }

    companion object {
        private const val TAG = "UpdateNoteViewModel"
    }
}
