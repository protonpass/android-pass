package me.proton.core.pass.presentation.create.alias

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.android.pass.log.PassLogger
import me.proton.android.pass.notifications.api.SnackbarMessageRepository
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.pass.common.api.Some
import me.proton.core.pass.common.api.onError
import me.proton.core.pass.common.api.onSuccess
import me.proton.core.pass.domain.AliasOptions
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.domain.entity.NewAlias
import me.proton.core.pass.domain.repositories.AliasRepository
import me.proton.core.pass.domain.usecases.CreateAlias
import me.proton.core.pass.presentation.create.alias.AliasSnackbarMessage.InitError
import me.proton.core.pass.presentation.create.alias.AliasSnackbarMessage.ItemCreationError
import me.proton.core.pass.presentation.uievents.IsLoadingState
import me.proton.core.pass.presentation.uievents.ItemSavedState
import javax.inject.Inject

@HiltViewModel
class CreateAliasViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val aliasRepository: AliasRepository,
    private val createAlias: CreateAlias,
    private val snackbarMessageRepository: SnackbarMessageRepository,
    savedStateHandle: SavedStateHandle
) : BaseAliasViewModel(snackbarMessageRepository, savedStateHandle) {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PassLogger.e(TAG, throwable)
    }

    private var _aliasOptions: AliasOptions? = null

    private val mutableCloseScreenEventFlow: MutableStateFlow<CloseScreenEvent> =
        MutableStateFlow(CloseScreenEvent.NotClose)
    val closeScreenEventFlow: StateFlow<CloseScreenEvent> = mutableCloseScreenEventFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = CloseScreenEvent.NotClose
        )

    init {
        viewModelScope.launch(coroutineExceptionHandler) {
            if (_aliasOptions != null) return@launch
            isLoadingState.update { IsLoadingState.Loading }
            val userId = accountManager.getPrimaryUserId()
                .first { userId -> userId != null }
            if (userId != null && shareId is Some) {
                aliasRepository.getAliasOptions(userId, shareId.value)
                    .onSuccess { aliasOptions ->
                        _aliasOptions = aliasOptions

                        val mailboxes = aliasOptions.mailboxes.mapIndexed { idx, model ->
                            AliasMailboxUiModel(model = model, selected = idx == 0)
                        }
                        val mailboxTitle = mailboxes.first { it.selected }.model.email

                        aliasItemState.update {
                            it.copy(
                                aliasOptions = aliasOptions,
                                selectedSuffix = aliasOptions.suffixes.first(),
                                mailboxes = mailboxes,
                                mailboxTitle = mailboxTitle,
                                isMailboxListApplicable = true
                            )
                        }
                    }
                    .onError {
                        val defaultMessage = "Could not get alias options"
                        PassLogger.i(TAG, it ?: Exception(defaultMessage), defaultMessage)
                        snackbarMessageRepository.emitSnackbarMessage(InitError)
                        mutableCloseScreenEventFlow.update { CloseScreenEvent.Close }
                    }
            } else {
                PassLogger.i(TAG, "Empty User Id")
                snackbarMessageRepository.emitSnackbarMessage(InitError)
                mutableCloseScreenEventFlow.update { CloseScreenEvent.Close }
            }
            isLoadingState.update { IsLoadingState.NotLoading }
        }
    }

    fun createAlias(shareId: ShareId) = viewModelScope.launch(coroutineExceptionHandler) {
        val aliasItem = aliasItemState.value
        if (aliasItem.selectedSuffix == null) return@launch

        val mailboxes = aliasItem.mailboxes.filter { it.selected }.map { it.model }
        val aliasItemValidationErrors = aliasItem.validate()
        if (aliasItemValidationErrors.isNotEmpty()) {
            aliasItemValidationErrorsState.update { aliasItemValidationErrors }
        } else {
            isLoadingState.update { IsLoadingState.Loading }
            val userId = accountManager.getPrimaryUserId()
                .first { userId -> userId != null }
            if (userId != null) {
                createAlias(
                    userId = userId,
                    shareId = shareId,
                    newAlias = NewAlias(
                        title = aliasItem.title,
                        note = aliasItem.note,
                        prefix = aliasItem.alias,
                        suffix = aliasItem.selectedSuffix,
                        mailboxes = mailboxes
                    )
                )
                    .onSuccess { item ->
                        isItemSavedState.update { ItemSavedState.Success(item.id) }
                    }
                    .onError {
                        val defaultMessage = "Create alias error"
                        PassLogger.i(TAG, it ?: Exception(defaultMessage), defaultMessage)
                        snackbarMessageRepository.emitSnackbarMessage(ItemCreationError)
                    }
            } else {
                PassLogger.i(TAG, "Empty User Id")
                snackbarMessageRepository.emitSnackbarMessage(ItemCreationError)
            }
            isLoadingState.update { IsLoadingState.NotLoading }
        }
    }

    companion object {
        private const val TAG = "CreateAliasViewModel"
    }
}
