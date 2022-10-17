package me.proton.core.pass.presentation.create.note

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.pass.common.api.Result
import me.proton.core.pass.domain.Item
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.Share
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
        accountManager.getPrimaryUserId().first { userId -> userId != null }?.let { userId ->
            when (val result: Result<Item> = itemRepository.getById(userId, shareId, itemId)) {
                is Result.Success -> {
                    _item = result.data

                    isLoadingState.value = IsLoadingState.NotLoading
                    noteItemState.value = NoteItem(
                        title = result.data.title.decrypt(cryptoContext.keyStoreCrypto),
                        note = result.data.note.decrypt(cryptoContext.keyStoreCrypto)
                    )
                }
                else -> {
                    // no-op
                }
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
                when (val shareResult = getShare.invoke(userId, shareId)) {
                    is Result.Success -> {
                        val share: Share? = shareResult.data
                        requireNotNull(share)
                        val itemContents = noteItem.toItemContents()
                        val updatedItemResult =
                            itemRepository.updateItem(userId, share, _item!!, itemContents)
                        when (updatedItemResult) {
                            is Result.Success -> {
                                isLoadingState.value = IsLoadingState.NotLoading
                                isItemSavedState.value =
                                    ItemSavedState.Success(updatedItemResult.data.id)
                            }
                            else -> {
                                // no-op
                            }
                        }
                    }
                    else -> {
                        // no-op
                    }
                }
            }
    }
}
