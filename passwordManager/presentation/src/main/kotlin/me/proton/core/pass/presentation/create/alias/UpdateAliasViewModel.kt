package me.proton.core.pass.presentation.create.alias

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.android.pass.log.PassLogger
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.pass.common.api.Option
import me.proton.core.pass.common.api.Some
import me.proton.core.pass.common.api.onError
import me.proton.core.pass.common.api.onSuccess
import me.proton.core.pass.domain.AliasOptions
import me.proton.core.pass.domain.AliasSuffix
import me.proton.core.pass.domain.Item
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ItemType
import me.proton.core.pass.domain.repositories.AliasRepository
import me.proton.core.pass.domain.repositories.ItemRepository
import me.proton.core.pass.presentation.create.alias.AliasSnackbarMessage.InitError
import me.proton.core.pass.presentation.uievents.IsLoadingState
import javax.inject.Inject

@HiltViewModel
class UpdateAliasViewModel @Inject constructor(
    accountManager: AccountManager,
    private val cryptoContext: CryptoContext,
    private val itemRepository: ItemRepository,
    private val aliasRepository: AliasRepository,
    savedStateHandle: SavedStateHandle
) : BaseAliasViewModel(savedStateHandle) {

    private val itemId: Option<ItemId> =
        Option.fromNullable(savedStateHandle.get<String>("itemId")?.let { ItemId(it) })

    private var _item: Item? = null

    init {
        viewModelScope.launch {
            if (_item != null) return@launch
            isLoadingState.update { IsLoadingState.Loading }
            val userId = accountManager.getPrimaryUserId()
                .first { userId -> userId != null }
            if (userId != null && shareId is Some && itemId is Some) {
                itemRepository.getById(userId, shareId.value, itemId.value)
                    .onSuccess { item ->
                        _item = item
                        aliasRepository.getAliasMailboxes(userId, shareId.value, itemId.value)
                            .onSuccess { mailboxes ->
                                val alias = item.itemType as ItemType.Alias
                                val email = alias.aliasEmail
                                val (prefix, suffix) = extractPrefixSuffix(email)
                                val mailboxesUiModel = mailboxes.map { mailbox ->
                                    AliasMailboxUiModel(
                                        model = mailbox,
                                        selected = true
                                    )
                                }
                                aliasItemState.update {
                                    it.copy(
                                        title = item.title.decrypt(cryptoContext.keyStoreCrypto),
                                        note = item.note.decrypt(cryptoContext.keyStoreCrypto),
                                        alias = prefix,
                                        aliasOptions = AliasOptions(emptyList(), emptyList()),
                                        selectedSuffix = AliasSuffix(suffix, suffix, false, ""),
                                        mailboxes = mailboxesUiModel,
                                        aliasToBeCreated = email
                                    )
                                }
                            }
                            .onError {
                                val message = "Error getting alias mailboxes"
                                PassLogger.i(TAG, it ?: Exception(message), message)
                                mutableSnackbarMessage.tryEmit(InitError)
                            }
                    }
                    .onError {
                        val message = "Error getting item by id"
                        PassLogger.i(TAG, it ?: Exception(message), message)
                        mutableSnackbarMessage.tryEmit(InitError)
                    }
            } else {
                PassLogger.i(TAG, "Empty user/share/item Id")
                mutableSnackbarMessage.tryEmit(InitError)
            }
        }
        isLoadingState.update { IsLoadingState.NotLoading }
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

    companion object {
        const val TAG = "UpdateAliasViewModel"
    }
}
