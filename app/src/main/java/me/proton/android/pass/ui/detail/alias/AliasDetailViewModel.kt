package me.proton.android.pass.ui.detail.alias

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.pass.domain.Item
import me.proton.core.pass.domain.ItemType
import me.proton.core.pass.domain.repositories.AliasRepository

@HiltViewModel
class AliasDetailViewModel @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val aliasRepository: AliasRepository,
    private val accountManager: AccountManager
) : ViewModel() {

    val initialViewState = ViewState.Loading
    val viewState: MutableStateFlow<ViewState> = MutableStateFlow(initialViewState)

    private val itemFlow: MutableStateFlow<Item?> = MutableStateFlow(null)

    fun setItem(item: Item) = viewModelScope.launch {
        if (itemFlow.value != null) return@launch
        itemFlow.value = item

        accountManager.getPrimaryUserId().first { userId -> userId != null }?.let { userId ->
            val mailboxes = aliasRepository.getAliasMailboxes(userId, item.shareId, item.id)
            val alias = item.itemType as ItemType.Alias
            viewState.value = ViewState.Data(
                AliasUiModel(
                    title = item.title.decrypt(cryptoContext.keyStoreCrypto),
                    alias = alias.aliasEmail,
                    mailboxes = mailboxes,
                    note = item.note.decrypt(cryptoContext.keyStoreCrypto)
                )
            )
        }
    }

    sealed class ViewState {
        object Loading : ViewState()
        data class Error(val message: String) : ViewState()
        data class Data(val model: AliasUiModel) : ViewState()
    }

    data class AliasUiModel(
        val title: String,
        val alias: String,
        val mailboxes: List<String>,
        val note: String
    )
}
