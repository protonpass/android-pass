package me.proton.core.pass.presentation.create.alias

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.pass.common.api.Result
import me.proton.core.pass.domain.AliasOptions
import me.proton.core.pass.domain.AliasSuffix
import me.proton.core.pass.domain.Item
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ItemType
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.domain.repositories.AliasRepository
import me.proton.core.pass.domain.repositories.ItemRepository
import me.proton.core.pass.presentation.uievents.IsLoadingState
import javax.inject.Inject

@HiltViewModel
class UpdateAliasViewModel @Inject constructor(
    accountManager: AccountManager,
    private val cryptoContext: CryptoContext,
    private val itemRepository: ItemRepository,
    private val aliasRepository: AliasRepository
) : BaseAliasViewModel(accountManager) {

    private var _item: Item? = null

    fun onStart(shareId: ShareId, itemId: ItemId) = viewModelScope.launch {
        if (_item != null) return@launch

        isLoadingState.value = IsLoadingState.Loading
        withUserId { userId ->
            when (val itemResult = itemRepository.getById(userId, shareId, itemId)) {
                is Result.Success -> {
                    _item = itemResult.data
                    when (
                        val mailboxesResult =
                            aliasRepository.getAliasMailboxes(userId, shareId, itemId)
                    ) {
                        is Result.Success -> {
                            val alias = itemResult.data.itemType as ItemType.Alias
                            val email = alias.aliasEmail
                            val (prefix, suffix) = extractPrefixSuffix(email)
                            isLoadingState.value = IsLoadingState.NotLoading
                            aliasItemState.value = aliasItemState.value.copy(
                                title = itemResult.data.title.decrypt(cryptoContext.keyStoreCrypto),
                                note = itemResult.data.note.decrypt(cryptoContext.keyStoreCrypto),
                                alias = prefix,
                                aliasOptions = AliasOptions(emptyList(), emptyList()),
                                selectedSuffix = AliasSuffix(suffix, suffix, false, ""),
                                selectedMailbox = mailboxesResult.data.first(),
                                aliasToBeCreated = email
                            )
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

    fun updateAlias() = viewModelScope.launch {

    }

    @Suppress("MagicNumber")
    private fun extractPrefixSuffix(email: String): PrefixSuffix {
        val parts = email.split(".")
        val suffix = "${parts[parts.size - 2]}.${parts[parts.size - 1]}"
        var prefix = ""
        for (idx in 0..parts.size - 3) {
            if (idx > 0) prefix += "."
            prefix += parts[idx]
        }
        return PrefixSuffix(prefix, suffix)
    }

    internal data class PrefixSuffix(
        val prefix: String,
        val suffix: String
    )
}
