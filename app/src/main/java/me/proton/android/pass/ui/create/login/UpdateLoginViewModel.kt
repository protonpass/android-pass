package me.proton.android.pass.ui.create.login

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.proton.android.pass.ui.shared.uievents.IsLoadingState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.pass.domain.Item
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ItemType
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.domain.repositories.ItemRepository
import me.proton.core.pass.domain.usecases.GetShareById
import javax.inject.Inject

@HiltViewModel
class UpdateLoginViewModel @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val accountManager: AccountManager,
    private val itemRepository: ItemRepository,
    private val getShare: GetShareById
) : BaseLoginViewModel() {

    private var _item: Item? = null

    fun setItem(shareId: ShareId, itemId: ItemId) = viewModelScope.launch {
        if (_item != null) return@launch

        isLoadingState.value = IsLoadingState.Loading
        accountManager.getPrimaryUserId().first { userId -> userId != null }?.let { userId ->
            val retrievedItem = itemRepository.getById(userId, shareId, itemId)
            val itemContents = retrievedItem.itemType as ItemType.Login
            _item = retrievedItem

            isLoadingState.value = IsLoadingState.NotLoading
            loginItemState.value = LoginItem(
                title = retrievedItem.title.decrypt(cryptoContext.keyStoreCrypto),
                username = itemContents.username,
                password = itemContents.password.decrypt(cryptoContext.keyStoreCrypto),
                websiteAddresses = itemContents.websites.ifEmpty { listOf("") },
                note = retrievedItem.note.decrypt(cryptoContext.keyStoreCrypto)
            )
        }
    }

    fun updateItem(shareId: ShareId) = viewModelScope.launch {
        requireNotNull(_item)
        isLoadingState.value = IsLoadingState.Loading
        val loginItem = loginItemState.value
        accountManager.getPrimaryUserId().first { userId -> userId != null }?.let { userId ->
            val share = getShare.invoke(userId, shareId)
            requireNotNull(share)
            val itemContents = loginItem.toItemContents()
            val updatedItem = itemRepository.updateItem(userId, share, _item!!, itemContents)
            isLoadingState.value = IsLoadingState.NotLoading
            isItemSavedState.value = ItemSavedState.Success(updatedItem.id)
        }
    }
}
