package me.proton.android.pass.ui.create.alias

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.pass.domain.AliasMailbox
import me.proton.core.pass.domain.AliasOptions
import me.proton.core.pass.domain.AliasSuffix
import me.proton.core.pass.domain.Item
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ItemType
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.domain.repositories.AliasRepository
import me.proton.core.pass.domain.repositories.ItemRepository

@HiltViewModel
class UpdateAliasViewModel @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val itemRepository: ItemRepository,
    private val accountManager: AccountManager,
    private val aliasRepository: AliasRepository,
) : BaseAliasViewModel(accountManager) {

    private var _item: Item? = null

    fun onStart(shareId: ShareId, itemId: ItemId) = viewModelScope.launch {
        if (_item != null) return@launch

        viewState.value = viewState.value.copy(state = State.Loading)
        withUserId { userId ->
            val retrievedItem = itemRepository.getById(userId, shareId, itemId)
            _item = retrievedItem

            val mailboxes = aliasRepository.getAliasMailboxes(userId, shareId, itemId)

            val alias = retrievedItem.itemType as ItemType.Alias
            val email = alias.aliasEmail

            // TODO: Improve prefix/suffix detection
            val parts = email.split(".")
            val suffix = "${parts[parts.size - 2]}.${parts[parts.size - 1]}"
            var prefix = ""
            for (idx in 0..parts.size - 3) {
                if (idx > 0) prefix += "."
                prefix += parts[idx]
            }

            viewState.value = viewState.value.copy(
                state = State.Idle,
                modelState = viewState.value.modelState.copy(
                    title = retrievedItem.title.decrypt(cryptoContext.keyStoreCrypto),
                    note = retrievedItem.note.decrypt(cryptoContext.keyStoreCrypto),
                    alias = prefix,
                    aliasOptions = AliasOptions(emptyList(), emptyList()),
                    selectedSuffix = AliasSuffix(suffix, suffix, false, ""),
                    selectedMailbox = AliasMailbox(1, mailboxes.first()),
                    aliasToBeCreated = email
                )
            )
        }
    }

    fun updateAlias() = viewModelScope.launch {
        // TODO: Implement
    }
}
