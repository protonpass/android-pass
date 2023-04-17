package proton.android.pass.featureitemcreate.impl.alias

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.onError
import proton.android.pass.common.api.onSuccess
import proton.android.pass.common.api.toOption
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.errors.CannotCreateMoreAliasesError
import proton.android.pass.data.api.repositories.DraftRepository
import proton.android.pass.data.api.usecases.CreateAlias
import proton.android.pass.data.api.usecases.ObserveAliasOptions
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.featureitemcreate.impl.ItemCreate
import proton.android.pass.featureitemcreate.impl.alias.AliasSnackbarMessage.AliasCreated
import proton.android.pass.featureitemcreate.impl.alias.AliasSnackbarMessage.ItemCreationError
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryManager
import proton.pass.domain.ShareId
import proton.pass.domain.entity.NewAlias
import javax.inject.Inject

@HiltViewModel
open class CreateAliasViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val createAlias: CreateAlias,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val telemetryManager: TelemetryManager,
    protected val draftRepository: DraftRepository,
    observeAliasOptions: ObserveAliasOptions,
    observeVaults: ObserveVaultsWithItemCount,
    savedStateHandle: SavedStateHandle
) : BaseAliasViewModel(observeVaults, savedStateHandle) {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PassLogger.e(TAG, throwable)
    }

    private val selectedSuffixState: MutableStateFlow<Option<AliasSuffixUiModel>> =
        MutableStateFlow(None)

    private val aliasOptionsState: Flow<LoadingResult<AliasOptionsUiModel>> = sharesWrapperState
        .flatMapLatest { observeAliasOptions(it.currentVault.vault.shareId) }
        .map(::AliasOptionsUiModel)
        .asLoadingResult()
        .onEach {
            when (it) {
                is LoadingResult.Error -> {
                    PassLogger.w(TAG, it.exception, "Error loading AliasOptions")
                    isLoadingState.update { IsLoadingState.NotLoading }
                    snackbarDispatcher(AliasSnackbarMessage.CannotRetrieveAliasOptions)
                    mutableCloseScreenEventFlow.update { CloseScreenEvent.Close }
                }

                LoadingResult.Loading -> isLoadingState.update { IsLoadingState.Loading }
                is LoadingResult.Success -> {
                    isLoadingState.update { IsLoadingState.NotLoading }
                    isApplyButtonEnabledState.update { IsButtonEnabled.Enabled }
                }
            }
        }
        .distinctUntilChanged()

    val createAliasUiState = combine(
        baseAliasUiState,
        aliasOptionsState,
        selectedMailboxListState,
        selectedSuffixState,
    ) { aliasUiState, aliasOptionsResult, selectedMailboxes, selectedSuffix ->
        if (aliasOptionsResult is LoadingResult.Success) {
            val aliasOptions = aliasOptionsResult.data

            val mailboxes = aliasOptions.mailboxes
                .map { model ->
                    SelectedAliasMailboxUiModel(
                        model = model,
                        selected = selectedMailboxes.contains(model.id)
                    )
                }
                .toMutableList()
            if (mailboxes.none { it.selected } && mailboxes.isNotEmpty()) {
                val mailbox = mailboxes.removeAt(0)
                mailboxes.add(0, mailbox.copy(selected = true))
                    .also { selectedMailboxListState.update { listOf(mailbox.model.id) } }
            }

            val mailboxTitle = getMailboxTitle(mailboxes)

            val suffix = if (
                selectedSuffix is Some &&
                aliasOptions.suffixes.contains(selectedSuffix.value)
            ) {
                selectedSuffix.value
            } else {
                aliasOptions.suffixes.first()
                    .also { selectedSuffixState.update { it } }
            }
            val aliasToBeCreated = if (aliasUiState.aliasItem.prefix.isNotBlank()) {
                getAliasToBeCreated(aliasUiState.aliasItem.prefix, suffix)
            } else {
                aliasUiState.aliasItem.aliasToBeCreated
            }

            aliasUiState.copy(
                aliasItem = aliasUiState.aliasItem.copy(
                    aliasOptions = aliasOptions,
                    selectedSuffix = suffix,
                    mailboxes = mailboxes,
                    mailboxTitle = mailboxTitle,
                    aliasToBeCreated = aliasToBeCreated
                )
            )

        } else {
            aliasUiState
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CreateUpdateAliasUiState.Initial
        )

    protected var titlePrefixInSync = true

    override fun onTitleChange(value: String) {
        onUserEditedContent()
        aliasItemState.update { aliasItem ->
            val prefix = if (titlePrefixInSync) {
                AliasUtils.formatAlias(value)
            } else {
                aliasItem.prefix
            }.take(AliasItem.MAX_PREFIX_LENGTH)
            aliasItem.copy(
                title = value,
                prefix = prefix,
                aliasToBeCreated = getAliasToBeCreated(
                    alias = prefix,
                    suffix = selectedSuffixState.value.value()
                )
            )
        }
        aliasItemValidationErrorsState.update {
            it.toMutableSet()
                .apply { remove(AliasItemValidationErrors.BlankTitle) }
        }
    }

    fun onPrefixChange(value: String) {
        if (value.contains(" ") || value.contains("\n")) return
        onUserEditedContent()
        val prefix = value.take(AliasItem.MAX_PREFIX_LENGTH)
        aliasItemState.update {
            it.copy(
                prefix = prefix,
                aliasToBeCreated = getAliasToBeCreated(
                    alias = prefix,
                    suffix = aliasItemState.value.selectedSuffix
                )
            )
        }
        aliasItemValidationErrorsState.update {
            it.toMutableSet()
                .apply {
                    remove(AliasItemValidationErrors.BlankPrefix)
                    remove(AliasItemValidationErrors.InvalidAliasContent)
                }
        }
        titlePrefixInSync = false
    }

    fun onSuffixChange(suffix: AliasSuffixUiModel) {
        onUserEditedContent()
        selectedSuffixState.update { suffix.toOption() }
    }

    fun createAlias(shareId: ShareId) = viewModelScope.launch(coroutineExceptionHandler) {
        val aliasItem = createAliasUiState.value.aliasItem
        if (aliasItem.selectedSuffix == null) return@launch

        val mailboxes = aliasItem.mailboxes.filter { it.selected }.map { it.model }
        val aliasItemValidationErrors = aliasItem.validate(allowEmptyTitle = isDraft)
        if (aliasItemValidationErrors.isNotEmpty()) {
            aliasItemValidationErrorsState.update { aliasItemValidationErrors }
            return@launch
        }

        if (isDraft) {
            draftRepository.save(KEY_DRAFT_ALIAS, aliasItem)
            isAliasDraftSavedState.tryEmit(AliasDraftSavedState.Success(shareId, aliasItem))
        } else {
            isLoadingState.update { IsLoadingState.Loading }
            performCreateAlias(shareId, aliasItem, aliasItem.selectedSuffix, mailboxes)
            isLoadingState.update { IsLoadingState.NotLoading }
        }
    }

    private suspend fun performCreateAlias(
        shareId: ShareId,
        aliasItem: AliasItem,
        aliasSuffix: AliasSuffixUiModel,
        mailboxes: List<AliasMailboxUiModel>
    ) {
        val userId = accountManager.getPrimaryUserId().first { userId -> userId != null }
        if (userId != null) {
            createAlias(
                userId = userId,
                shareId = shareId,
                newAlias = NewAlias(
                    title = aliasItem.title,
                    note = aliasItem.note,
                    prefix = aliasItem.prefix,
                    suffix = aliasSuffix.toDomain(),
                    mailboxes = mailboxes.map(AliasMailboxUiModel::toDomain)
                )
            )
                .onSuccess { item ->
                    val generatedAlias =
                        getAliasToBeCreated(aliasItem.prefix, aliasSuffix) ?: ""
                    isAliasSavedState.update {
                        AliasSavedState.Success(
                            item.id,
                            generatedAlias
                        )
                    }
                    snackbarDispatcher(AliasCreated)
                    telemetryManager.sendEvent(ItemCreate(EventItemType.Alias))
                }
                .onError { onCreateAliasError(it) }
        } else {
            PassLogger.i(TAG, "Empty User Id")
            snackbarDispatcher(ItemCreationError)
        }
    }

    private suspend fun onCreateAliasError(cause: Throwable?) {
        if (cause is CannotCreateMoreAliasesError) {
            snackbarDispatcher(AliasSnackbarMessage.CannotCreateMoreAliasesError)
        } else {
            val defaultMessage = "Create alias error"
            PassLogger.e(TAG, cause ?: Exception(defaultMessage), defaultMessage)
            snackbarDispatcher(ItemCreationError)
        }
    }

    companion object {
        private const val TAG = "CreateAliasViewModel"
        const val KEY_DRAFT_ALIAS = "draft_alias"
    }
}
