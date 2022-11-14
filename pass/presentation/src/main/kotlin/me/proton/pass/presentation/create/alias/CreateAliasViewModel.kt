package me.proton.pass.presentation.create.alias

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
import me.proton.pass.common.api.Result
import me.proton.pass.common.api.Some
import me.proton.pass.common.api.asResult
import me.proton.pass.common.api.onError
import me.proton.pass.common.api.onSuccess
import me.proton.pass.domain.AliasMailbox
import me.proton.pass.domain.AliasOptions
import me.proton.pass.domain.AliasSuffix
import me.proton.pass.domain.ShareId
import me.proton.pass.domain.entity.NewAlias
import me.proton.pass.domain.errors.CannotCreateMoreAliasesError
import me.proton.pass.domain.repositories.AliasRepository
import me.proton.pass.domain.usecases.CreateAlias
import me.proton.pass.presentation.create.alias.AliasSnackbarMessage.InitError
import me.proton.pass.presentation.create.alias.AliasSnackbarMessage.ItemCreationError
import me.proton.pass.presentation.uievents.AliasSavedState
import me.proton.pass.presentation.uievents.IsButtonEnabled
import me.proton.pass.presentation.uievents.IsLoadingState
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
                    .asResult()
                    .collect { onAliasOptions(it) }
            } else {
                PassLogger.i(TAG, "Empty User Id")
                snackbarMessageRepository.emitSnackbarMessage(InitError)
                mutableCloseScreenEventFlow.update { CloseScreenEvent.Close }
            }
            isLoadingState.update { IsLoadingState.NotLoading }
        }
    }

    fun setInitialState(state: InitialCreateAliasUiState) = viewModelScope.launch {
        aliasItemState.update {
            it.copy(
                title = state.title ?: "",
                alias = state.alias()

            )
        }
    }

    fun createAlias(shareId: ShareId) = viewModelScope.launch(coroutineExceptionHandler) {
        val aliasItem = aliasItemState.value
        if (aliasItem.selectedSuffix == null) return@launch

        val mailboxes = aliasItem.mailboxes.filter { it.selected }.map { it.model }
        val aliasItemValidationErrors = aliasItem.validate()
        if (aliasItemValidationErrors.isNotEmpty()) {
            aliasItemValidationErrorsState.update { aliasItemValidationErrors }
            return@launch
        }

        isLoadingState.update { IsLoadingState.Loading }
        performCreateAlias(shareId, aliasItem, aliasItem.selectedSuffix, mailboxes)
        isLoadingState.update { IsLoadingState.NotLoading }
    }

    private suspend fun performCreateAlias(
        shareId: ShareId,
        aliasItem: AliasItem,
        aliasSuffix: AliasSuffix,
        mailboxes: List<AliasMailbox>
    ) {
        val userId = accountManager.getPrimaryUserId().first { userId -> userId != null }
        if (userId != null) {
            createAlias(
                userId = userId,
                shareId = shareId,
                newAlias = NewAlias(
                    title = aliasItem.title,
                    note = aliasItem.note,
                    prefix = aliasItem.alias,
                    suffix = aliasSuffix,
                    mailboxes = mailboxes
                )
            )
                .onSuccess { item ->
                    val generatedAlias =
                        getAliasToBeCreated(aliasItem.alias, aliasSuffix) ?: ""
                    isAliasSavedState.update { AliasSavedState.Success(item.id, generatedAlias) }
                }
                .onError { onCreateAliasError(it) }
        } else {
            PassLogger.i(TAG, "Empty User Id")
            snackbarMessageRepository.emitSnackbarMessage(ItemCreationError)
        }
    }

    private suspend fun onAliasOptions(result: Result<AliasOptions>) {
        result
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
                isApplyButtonEnabledState.update { IsButtonEnabled.Enabled }
            }
            .onError {
                val defaultMessage = "Could not get alias options"
                PassLogger.i(TAG, it ?: Exception(defaultMessage), defaultMessage)
                snackbarMessageRepository.emitSnackbarMessage(InitError)
                mutableCloseScreenEventFlow.update { CloseScreenEvent.Close }
            }
    }

    private suspend fun onCreateAliasError(cause: Throwable?) {
        if (cause is CannotCreateMoreAliasesError) {
            snackbarMessageRepository.emitSnackbarMessage(AliasSnackbarMessage.CannotCreateMoreAliasesError)
        } else {
            val defaultMessage = "Create alias error"
            PassLogger.i(TAG, cause ?: Exception(defaultMessage), defaultMessage)
            snackbarMessageRepository.emitSnackbarMessage(ItemCreationError)
        }
    }

    companion object {
        private const val TAG = "CreateAliasViewModel"
    }
}
