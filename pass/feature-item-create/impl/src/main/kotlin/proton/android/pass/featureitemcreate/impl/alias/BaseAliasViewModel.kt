package proton.android.pass.featureitemcreate.impl.alias

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.toOption
import proton.android.pass.commonuimodels.api.ShareUiModel
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.ObserveAliasOptions
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.android.pass.featureitemcreate.impl.alias.AliasSnackbarMessage.CannotRetrieveAliasOptions
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.AliasOptionalNavArgId
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarMessageRepository
import proton.pass.domain.ShareId

abstract class BaseAliasViewModel(
    snackbarMessageRepository: SnackbarMessageRepository,
    observeAliasOptions: ObserveAliasOptions,
    observeVaults: ObserveVaults,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    protected val navShareId = savedStateHandle.get<String>(CommonNavArgId.ShareId.key)
        .toOption()
        .map { ShareId(it) }
    private val navShareIdState = MutableStateFlow(navShareId)
    private val selectedShareIdState: MutableStateFlow<Option<ShareId>> = MutableStateFlow(None)

    private val title: Option<String> = savedStateHandle
        .get<String>(AliasOptionalNavArgId.Title.key)
        .toOption()
    protected var isDraft: Boolean = false

    private val observeAllVaultsFlow = observeVaults()
        .map { shares ->
            when (shares) {
                LoadingResult.Loading -> emptyList()
                is LoadingResult.Error -> {
                    PassLogger.e(TAG, shares.exception, "Cannot retrieve all shares")
                    emptyList()
                }
                is LoadingResult.Success -> shares.data.map {
                    ShareUiModel(
                        it.shareId,
                        it.name,
                        it.color,
                        it.icon
                    )
                }
            }
        }
        .distinctUntilChanged()

    private val sharesWrapperState = combine(
        navShareIdState,
        selectedShareIdState,
        observeAllVaultsFlow
    ) { navShareId, selectedShareId, allShares ->
        val selectedShare = allShares
            .firstOrNull { it.id == selectedShareId.value() }
            ?: allShares.firstOrNull { it.id == navShareId.value() }
            ?: allShares.first()
        SharesWrapper(allShares, selectedShare)
    }

    protected data class SharesWrapper(
        val shareList: List<ShareUiModel>,
        val currentShare: ShareUiModel
    )

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    val aliasItemState: MutableStateFlow<AliasItem> = MutableStateFlow(
        AliasItem(
            title = title.value() ?: "",
            prefix = AliasUtils.formatAlias(title.value() ?: "")
        )
    )
    protected val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.Loading)
    protected val isAliasSavedState: MutableStateFlow<AliasSavedState> =
        MutableStateFlow(AliasSavedState.Unknown)
    protected val isAliasDraftSavedState: MutableStateFlow<AliasDraftSavedState> =
        MutableStateFlow(AliasDraftSavedState.Unknown)
    protected val aliasItemValidationErrorsState: MutableStateFlow<Set<AliasItemValidationErrors>> =
        MutableStateFlow(emptySet())
    protected val isApplyButtonEnabledState: MutableStateFlow<IsButtonEnabled> =
        MutableStateFlow(IsButtonEnabled.Disabled)
    protected val mutableCloseScreenEventFlow: MutableStateFlow<CloseScreenEvent> =
        MutableStateFlow(CloseScreenEvent.NotClose)
    private val selectedMailboxListState: MutableStateFlow<List<Int>> =
        MutableStateFlow(emptyList())
    private val selectedSuffixState: MutableStateFlow<Option<AliasSuffixUiModel>> =
        MutableStateFlow(None)
    private val aliasOptionsState: Flow<LoadingResult<AliasOptionsUiModel>> = sharesWrapperState
        .flatMapLatest { observeAliasOptions(it.currentShare.id) }
        .map(::AliasOptionsUiModel)
        .asLoadingResult()
        .onEach {
            when (it) {
                is LoadingResult.Error -> {
                    isLoadingState.update { IsLoadingState.NotLoading }
                    snackbarMessageRepository.emitSnackbarMessage(CannotRetrieveAliasOptions)
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

    private val aliasItemWrapperState = combine(
        aliasItemState,
        aliasOptionsState,
        selectedMailboxListState,
        selectedSuffixState,
        aliasItemValidationErrorsState
    ) { aliasItem, aliasOptionsResult, selectedMailboxes, selectedSuffix, aliasItemValidationErrors ->
        val aliasItemWithOptions =
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

                val suffix =
                    if (selectedSuffix is Some && aliasOptions.suffixes.contains(selectedSuffix.value)) {
                        selectedSuffix.value
                    } else {
                        aliasOptions.suffixes.first()
                            .also { selectedSuffixState.update { it } }
                    }
                val aliasToBeCreated = if (aliasItem.prefix.isNotBlank()) {
                    getAliasToBeCreated(aliasItem.prefix, suffix)
                } else {
                    null
                }
                aliasItem.copy(
                    aliasOptions = aliasOptions,
                    selectedSuffix = suffix,
                    mailboxes = mailboxes,
                    mailboxTitle = mailboxTitle,
                    aliasToBeCreated = aliasToBeCreated
                )
            } else {
                aliasItem
            }
        AliasItemWrapper(aliasItemWithOptions, aliasItemValidationErrors)
    }

    private data class AliasItemWrapper(
        val aliasItem: AliasItem,
        val aliasItemValidationErrors: Set<AliasItemValidationErrors>
    )

    private val aliasSavedEventWrapperState = combine(
        isAliasSavedState,
        isAliasDraftSavedState
    ) { isAliasSaved, isAliasDraftSaved ->
        AliasSavedEventWrapper(isAliasSaved, isAliasDraftSaved)
    }

    private data class AliasSavedEventWrapper(
        val isAliasSaved: AliasSavedState,
        val isAliasDraftSaved: AliasDraftSavedState
    )

    val aliasUiState: StateFlow<CreateUpdateAliasUiState> = combine(
        sharesWrapperState,
        aliasItemWrapperState,
        isLoadingState,
        aliasSavedEventWrapperState,
        isApplyButtonEnabledState
    ) { shareWrapper, aliasItemWrapper, isLoading, isAliasSavedEvent, isButtonEnabled ->
        CreateUpdateAliasUiState(
            shareList = shareWrapper.shareList,
            selectedShareId = shareWrapper.currentShare,
            aliasItem = aliasItemWrapper.aliasItem,
            isDraft = isDraft,
            errorList = aliasItemWrapper.aliasItemValidationErrors,
            isLoadingState = isLoading,
            isAliasSavedState = isAliasSavedEvent.isAliasSaved,
            isAliasDraftSavedState = isAliasSavedEvent.isAliasDraftSaved,
            isApplyButtonEnabled = isButtonEnabled,
            showVaultSelector = shareWrapper.shareList.size > 1
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CreateUpdateAliasUiState.Initial
        )

    abstract fun onTitleChange(value: String)
    abstract fun onPrefixChange(value: String)

    open fun onNoteChange(value: String) {
        aliasItemState.update { it.copy(note = value) }
    }

    fun onSuffixChange(suffix: AliasSuffixUiModel) {
        selectedSuffixState.update { suffix.toOption() }
    }

    open fun onMailboxesChanged(mailboxes: List<SelectedAliasMailboxUiModel>) {
        val atLeastOneSelected = mailboxes.any { it.selected }
        if (!atLeastOneSelected) return
        selectedMailboxListState.update { mailboxes.filter { it.selected }.map { it.model.id } }
    }

    protected fun getMailboxTitle(mailboxes: List<SelectedAliasMailboxUiModel>): String {
        val allSelectedMailboxes = mailboxes.filter { it.selected }
        if (allSelectedMailboxes.isEmpty()) return ""
        val mailboxTitle = buildString {
            allSelectedMailboxes.forEachIndexed { idx, mailbox ->
                if (idx > 0) append(",\n")
                append(mailbox.model.email)
            }
        }

        return mailboxTitle
    }

    protected fun getAliasToBeCreated(alias: String, suffix: AliasSuffixUiModel?): String? {
        if (suffix != null && alias.isNotBlank()) {
            return "$alias${suffix.suffix}"
        }
        return null
    }

    fun changeVault(shareId: ShareId) = viewModelScope.launch {
        isLoadingState.update { IsLoadingState.Loading }
        selectedShareIdState.update { shareId.toOption() }
    }

    companion object {
        private const val TAG = "BaseAliasViewModel"
    }
}
