package me.proton.core.pass.presentation.create.login

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
import me.proton.core.pass.domain.ItemType
import me.proton.core.pass.domain.Share
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.domain.repositories.ItemRepository
import me.proton.core.pass.domain.usecases.GetShareById
import me.proton.core.pass.presentation.uievents.IsLoadingState
import me.proton.core.pass.presentation.uievents.ItemSavedState
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
        accountManager.getPrimaryUserId()
            .first { userId -> userId != null }
            ?.let { userId ->
                when (val itemResult = itemRepository.getById(userId, shareId, itemId)) {
                    is Result.Success -> {
                        val itemContents = itemResult.data.itemType as ItemType.Login
                        _item = itemResult.data

                        isLoadingState.value = IsLoadingState.NotLoading
                        loginItemState.value = LoginItem(
                            title = itemResult.data.title.decrypt(cryptoContext.keyStoreCrypto),
                            username = itemContents.username,
                            password = itemContents.password.decrypt(cryptoContext.keyStoreCrypto),
                            websiteAddresses = itemContents.websites.ifEmpty { listOf("") },
                            note = itemResult.data.note.decrypt(cryptoContext.keyStoreCrypto)
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
        val loginItem = loginItemState.value
        accountManager.getPrimaryUserId()
            .first { userId -> userId != null }
            ?.let { userId ->
                when (val shareResult = getShare.invoke(userId, shareId)) {
                    is Result.Success -> {
                        val share: Share? = shareResult.data
                        requireNotNull(share)
                        val itemContents = loginItem.toItemContents()
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
