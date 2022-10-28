package me.proton.android.pass.ui.detail.alias

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.pass.common.api.Result
import me.proton.pass.domain.AliasMailbox
import me.proton.pass.domain.Item
import me.proton.pass.domain.ItemType
import me.proton.pass.domain.repositories.AliasRepository
import javax.inject.Inject

@HiltViewModel
class AliasDetailViewModel @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val aliasRepository: AliasRepository,
    private val accountManager: AccountManager
) : ViewModel() {

    private val _viewState: MutableStateFlow<ViewState> = MutableStateFlow(ViewState.Loading)
    val viewState: StateFlow<ViewState> = _viewState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = ViewState.Loading
        )

    private val itemFlow: MutableStateFlow<Item?> = MutableStateFlow(null)

    fun setItem(item: Item) = viewModelScope.launch {
        if (itemFlow.value != null) return@launch
        itemFlow.value = item

        accountManager.getPrimaryUserId()
            .first { userId -> userId != null }
            ?.let { userId ->
                when (
                    val result =
                        aliasRepository.getAliasDetails(userId, item.shareId, item.id)
                ) {
                    is Result.Success -> {
                        val alias = item.itemType as ItemType.Alias
                        _viewState.value = ViewState.Data(
                            AliasUiModel(
                                title = item.title.decrypt(cryptoContext.keyStoreCrypto),
                                alias = alias.aliasEmail,
                                mailboxes = result.data.mailboxes,
                                note = item.note.decrypt(cryptoContext.keyStoreCrypto)
                            )
                        )
                    }
                    else -> {
                        // no-op
                    }
                }
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
        val mailboxes: List<AliasMailbox>,
        val note: String
    )
}
