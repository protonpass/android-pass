package proton.android.pass.featureitemcreate.impl.alias

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.asResultWithoutLoading
import proton.android.pass.common.api.map
import proton.android.pass.common.api.onError
import proton.android.pass.common.api.onSuccess
import proton.android.pass.common.api.toOption
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.repositories.AliasRepository
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.usecases.ObserveAliasOptions
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.android.pass.data.api.usecases.UpdateAlias
import proton.android.pass.data.api.usecases.UpdateAliasContent
import proton.android.pass.data.api.usecases.UpdateAliasItemContent
import proton.android.pass.featureitemcreate.impl.alias.AliasSnackbarMessage.AliasUpdated
import proton.android.pass.featureitemcreate.impl.alias.AliasSnackbarMessage.InitError
import proton.android.pass.featureitemcreate.impl.alias.AliasSnackbarMessage.ItemCreationError
import proton.android.pass.featureitemcreate.impl.alias.AliasSnackbarMessage.ItemUpdateError
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarMessageRepository
import proton.pass.domain.AliasDetails
import proton.pass.domain.Item
import proton.pass.domain.ItemId
import proton.pass.domain.ItemType
import proton.pass.domain.ShareId
import javax.inject.Inject

@HiltViewModel
class UpdateAliasViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val itemRepository: ItemRepository,
    private val aliasRepository: AliasRepository,
    private val snackbarMessageRepository: SnackbarMessageRepository,
    private val updateAliasUseCase: UpdateAlias,
    private val encryptionContextProvider: EncryptionContextProvider,
    observeAliasOptions: ObserveAliasOptions,
    observeVaults: ObserveVaults,
    savedStateHandle: SavedStateHandle
) : BaseAliasViewModel(
    snackbarMessageRepository,
    observeAliasOptions,
    observeVaults,
    savedStateHandle
) {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PassLogger.e(TAG, throwable)
    }

    private val itemId: Option<ItemId> =
        savedStateHandle.get<String>(CommonNavArgId.ItemId.key)
            .toOption()
            .map { ItemId(it) }

    private var _item: Item? = null

    private var itemDataChanged = false
    private var mailboxesChanged = false

    init {
        viewModelScope.launch(coroutineExceptionHandler) {
            isApplyButtonEnabledState.update { IsButtonEnabled.Disabled }
            setupInitialState()
        }
    }

    override fun onMailboxesChanged(mailboxes: List<SelectedAliasMailboxUiModel>) {
        super.onMailboxesChanged(mailboxes)
        isApplyButtonEnabledState.update { IsButtonEnabled.Enabled }
        mailboxesChanged = true
    }

    override fun onNoteChange(value: String) {
        super.onNoteChange(value)
        isApplyButtonEnabledState.update { IsButtonEnabled.Enabled }
        itemDataChanged = true
    }

    override fun onTitleChange(value: String) {
        aliasItemState.update { it.copy(title = value) }
        aliasItemValidationErrorsState.update {
            it.toMutableSet()
                .apply { remove(AliasItemValidationErrors.BlankTitle) }
        }
        isApplyButtonEnabledState.update { IsButtonEnabled.Enabled }
        itemDataChanged = true
    }

    override fun onPrefixChange(value: String) {
        // no-op as alias cannot be changed from Update view
        // should never be called
        PassLogger.e(
            TAG,
            IllegalStateException("UpdateAliasViewModel.onAliasChange should never be called")
        )
    }

    private suspend fun setupInitialState() {
        if (_item != null) return
        isLoadingState.update { IsLoadingState.Loading }

        val userId = accountManager.getPrimaryUserId().first { userId -> userId != null }
        if (userId != null && navShareId is Some && itemId is Some) {
            fetchInitialData(userId, navShareId.value, itemId.value)
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
                    .asResultWithoutLoading()
                    .collect { onAliasDetails(it, item) }
            }
            .onError { showError("Error getting item by id", InitError, it) }
    }

    private suspend fun onAliasDetails(result: LoadingResult<AliasDetails>, item: Item) {
        result
            .map(::AliasDetailsUiModel)
            .onSuccess { details ->
                val alias = item.itemType as ItemType.Alias
                val email = alias.aliasEmail
                val (prefix, suffix) = AliasUtils.extractPrefixSuffix(email)

                val mailboxes = details.availableMailboxes.map { mailbox ->
                    SelectedAliasMailboxUiModel(
                        model = mailbox,
                        selected = details.mailboxes.any { it.id == mailbox.id }
                    )
                }

                aliasItemState.update {
                    encryptionContextProvider.withEncryptionContext {
                        it.copy(
                            title = decrypt(item.title),
                            note = decrypt(item.note),
                            prefix = prefix,
                            aliasOptions = AliasOptionsUiModel(emptyList(), details.mailboxes),
                            selectedSuffix = AliasSuffixUiModel(suffix, suffix, false, ""),
                            mailboxes = mailboxes,
                            aliasToBeCreated = email,
                            mailboxTitle = getMailboxTitle(mailboxes)
                        )
                    }
                }
            }
            .onError {
                showError("Error getting alias mailboxes", InitError, it)
            }
    }

    private suspend fun showError(
        message: String,
        snackbarMessage: AliasSnackbarMessage,
        it: Throwable? = null
    ) {
        PassLogger.e(TAG, it ?: Exception(message), message)
        snackbarMessageRepository.emitSnackbarMessage(snackbarMessage)
    }

    fun updateAlias() = viewModelScope.launch(coroutineExceptionHandler) {
        val canUpdate = canUpdateAlias()
        if (!canUpdate) {
            PassLogger.i(TAG, "Cannot update alias")
            return@launch
        }

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
                    PassLogger.i(TAG, "Alias successfully updated")
                    isAliasSavedState.update {
                        AliasSavedState.Success(
                            itemId = item.id,
                            alias = "" // we don't care about it as we are updating it
                        )
                    }
                    isLoadingState.update { IsLoadingState.NotLoading }
                    snackbarMessageRepository.emitSnackbarMessage(AliasUpdated)
                }
                .onError {
                    PassLogger.e(TAG, it, "Update alias error")
                    snackbarMessageRepository.emitSnackbarMessage(ItemUpdateError)
                    isLoadingState.update { IsLoadingState.NotLoading }
                }
        } else {
            PassLogger.i(TAG, "Empty User Id")
            snackbarMessageRepository.emitSnackbarMessage(ItemCreationError)
            isLoadingState.update { IsLoadingState.NotLoading }
        }
    }

    private fun canUpdateAlias(): Boolean {
        if (!itemDataChanged && !mailboxesChanged) {
            PassLogger.i(TAG, "Nor item nor mailboxes have changed")
            return false
        }

        val aliasItem = aliasItemState.value
        val aliasItemValidationErrors = aliasItem.validate(allowEmptyTitle = false)
        if (aliasItemValidationErrors.isNotEmpty()) {
            PassLogger.i(TAG, "alias item validation has failed: $aliasItemValidationErrors")
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
                .map(AliasMailboxUiModel::toDomain)
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

        return UpdateAliasContent(
            mailboxes = mailboxes,
            itemData = itemData
        )
    }

    companion object {
        const val TAG = "UpdateAliasViewModel"
    }
}
