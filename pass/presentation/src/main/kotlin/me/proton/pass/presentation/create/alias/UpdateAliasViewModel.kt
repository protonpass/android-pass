package me.proton.pass.presentation.create.alias

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.android.pass.log.PassLogger
import me.proton.android.pass.notifications.api.SnackbarMessageRepository
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.None
import me.proton.pass.common.api.Option
import me.proton.pass.common.api.Some
import me.proton.pass.common.api.onError
import me.proton.pass.common.api.onSuccess
import me.proton.pass.domain.AliasDetails
import me.proton.pass.domain.AliasOptions
import me.proton.pass.domain.AliasSuffix
import me.proton.pass.domain.Item
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ItemType
import me.proton.pass.domain.ShareId
import me.proton.pass.domain.repositories.AliasRepository
import me.proton.pass.domain.repositories.ItemRepository
import me.proton.pass.domain.usecases.UpdateAlias
import me.proton.pass.domain.usecases.UpdateAliasContent
import me.proton.pass.domain.usecases.UpdateAliasItemContent
import me.proton.pass.presentation.create.alias.AliasSnackbarMessage.InitError
import me.proton.pass.presentation.uievents.IsButtonEnabled
import me.proton.pass.presentation.uievents.IsLoadingState
import me.proton.pass.presentation.uievents.ItemSavedState
import javax.inject.Inject

@HiltViewModel
class UpdateAliasViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val cryptoContext: CryptoContext,
    private val itemRepository: ItemRepository,
    private val aliasRepository: AliasRepository,
    private val snackbarMessageRepository: SnackbarMessageRepository,
    private val updateAliasUseCase: UpdateAlias,
    savedStateHandle: SavedStateHandle
) : BaseAliasViewModel(snackbarMessageRepository, savedStateHandle) {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PassLogger.e(TAG, throwable)
    }

    private val itemId: Option<ItemId> =
        Option.fromNullable(savedStateHandle.get<String>("itemId")?.let { ItemId(it) })

    private var _item: Item? = null

    private var itemDataChanged = false
    private var mailboxesChanged = false

    init {
        viewModelScope.launch(coroutineExceptionHandler) {
            isApplyButtonEnabledState.update { IsButtonEnabled.Disabled }
            setupInitialState()
        }
    }

    override fun onMailboxChange(mailbox: AliasMailboxUiModel) {
        super.onMailboxChange(mailbox)
        isApplyButtonEnabledState.update { IsButtonEnabled.Enabled }
        mailboxesChanged = true
    }

    override fun onNoteChange(value: String) {
        super.onNoteChange(value)
        isApplyButtonEnabledState.update { IsButtonEnabled.Enabled }
        itemDataChanged = true
    }

    override fun onTitleChange(value: String) {
        super.onTitleChange(value)
        isApplyButtonEnabledState.update { IsButtonEnabled.Enabled }
        itemDataChanged = true
    }

    private suspend fun setupInitialState() {
        if (_item != null) return
        isLoadingState.update { IsLoadingState.Loading }

        val userId = accountManager.getPrimaryUserId().first { userId -> userId != null }
        if (userId != null && shareId is Some && itemId is Some) {
            fetchInitialData(userId, shareId.value, itemId.value)
        } else {
            showError("Empty user/share/item Id", InitError)
        }
        isLoadingState.update { IsLoadingState.NotLoading }
    }

    private suspend fun fetchInitialData(userId: UserId, shareId: ShareId, itemId: ItemId) {
        val itemResult = itemRepository.getById(userId, shareId, itemId)
        itemResult
            .onSuccess { item ->
                _item = item
                aliasRepository.getAliasDetails(userId, shareId, itemId)
                    .onSuccess { details -> onAliasDetails(item, details) }
                    .onError {
                        showError("Error getting alias mailboxes", InitError, it)
                    }
            }
            .onError { showError("Error getting item by id", InitError, it) }
    }

    private fun onAliasDetails(item: Item, details: AliasDetails) {
        val alias = item.itemType as ItemType.Alias
        val email = alias.aliasEmail
        val (prefix, suffix) = extractPrefixSuffix(email)

        val mailboxes = details.availableMailboxes.map { mailbox ->
            AliasMailboxUiModel(
                model = mailbox,
                selected = details.mailboxes.any { it.id == mailbox.id }
            )
        }

        aliasItemState.update {
            it.copy(
                title = item.title.decrypt(cryptoContext.keyStoreCrypto),
                note = item.note.decrypt(cryptoContext.keyStoreCrypto),
                alias = prefix,
                aliasOptions = AliasOptions(emptyList(), details.mailboxes),
                selectedSuffix = AliasSuffix(suffix, suffix, false, ""),
                mailboxes = mailboxes,
                aliasToBeCreated = email,
                mailboxTitle = getMailboxTitle(mailboxes),
                isMailboxListApplicable = true // By default it should be applicable
            )
        }
    }

    private suspend fun showError(
        message: String,
        snackbarMessage: AliasSnackbarMessage,
        it: Throwable? = null
    ) {
        PassLogger.i(TAG, it ?: Exception(message), message)
        snackbarMessageRepository.emitSnackbarMessage(snackbarMessage)
    }

    fun updateAlias() = viewModelScope.launch(coroutineExceptionHandler) {
        val canUpdate = canUpdateAlias()
        if (!canUpdate) return@launch

        val body = createUpdateAliasBody()
        isLoadingState.update { IsLoadingState.Loading }

        val userId = accountManager.getPrimaryUserId().first { userId -> userId != null }
        if (userId != null) {
            updateAliasUseCase(
                userId = userId,
                item = _item!!,
                content = body
            )
                .onSuccess { item ->
                    isItemSavedState.update { ItemSavedState.Success(item.id) }
                }
                .onError {
                    val defaultMessage = "Update alias error"
                    PassLogger.i(TAG, it ?: Exception(defaultMessage), defaultMessage)
                    snackbarMessageRepository.emitSnackbarMessage(AliasSnackbarMessage.AliasUpdated)
                }
        } else {
            PassLogger.i(TAG, "Empty User Id")
            snackbarMessageRepository.emitSnackbarMessage(AliasSnackbarMessage.ItemCreationError)
        }
        isLoadingState.update { IsLoadingState.NotLoading }
    }

    private fun canUpdateAlias(): Boolean {
        if (!itemDataChanged && !mailboxesChanged) return false

        val aliasItem = aliasItemState.value
        val aliasItemValidationErrors = aliasItem.validate()
        if (aliasItemValidationErrors.isNotEmpty()) {
            aliasItemValidationErrorsState.update { aliasItemValidationErrors }
            return false
        }
        return true
    }

    private fun createUpdateAliasBody(): UpdateAliasContent {
        val mailboxes = if (mailboxesChanged) {
            val selectedMailboxes = aliasItemState.value
                .mailboxes
                .filter { it.selected }
                .map { it.model }
            Some(selectedMailboxes)
        } else None

        val itemData = if (itemDataChanged) {
            val aliasItem = aliasItemState.value
            Some(
                UpdateAliasItemContent(
                    title = aliasItem.title,
                    note = aliasItem.note
                )
            )
        } else None

        val body = UpdateAliasContent(
            mailboxes = mailboxes,
            itemData = itemData
        )
        return body
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
