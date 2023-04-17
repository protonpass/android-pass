package proton.android.pass.featureitemcreate.impl.alias

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.AliasOptionalNavArgId
import proton.android.pass.navigation.api.CommonNavArgId
import proton.pass.domain.ShareId
import proton.pass.domain.VaultWithItemCount

abstract class BaseAliasViewModel(
    observeVaults: ObserveVaultsWithItemCount,
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

                is LoadingResult.Success -> shares.data
            }
        }
        .distinctUntilChanged()

    protected val sharesWrapperState = combine(
        navShareIdState,
        selectedShareIdState,
        observeAllVaultsFlow
    ) { navShareId, selectedShareId, allShares ->
        val selectedShare = allShares
            .firstOrNull { it.vault.shareId == selectedShareId.value() }
            ?: allShares.firstOrNull { it.vault.shareId == navShareId.value() }
            ?: allShares.first()
        SharesWrapper(allShares, selectedShare)
    }

    protected data class SharesWrapper(
        val vaultList: List<VaultWithItemCount>,
        val currentVault: VaultWithItemCount
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
    protected val selectedMailboxListState: MutableStateFlow<List<Int>> =
        MutableStateFlow(emptyList())
    private val hasUserEditedContentFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val aliasItemWrapperState = combine(
        aliasItemState,
        selectedMailboxListState,
        aliasItemValidationErrorsState,
    ) { aliasItem, selectedMailboxList, aliasItemValidationErrors ->
        val mailboxes = aliasItem.mailboxes
            .map { mailbox ->
                mailbox.copy(
                    selected = selectedMailboxList.contains(mailbox.model.id)
                )
            }
            .toMutableList()
        AliasItemWrapper(
            aliasItem = aliasItem.copy(
                mailboxes = mailboxes,
                mailboxTitle = getMailboxTitle(mailboxes)
            ),
            aliasItemValidationErrors = aliasItemValidationErrors
        )
    }

    private data class AliasItemWrapper(
        val aliasItem: AliasItem,
        val aliasItemValidationErrors: Set<AliasItemValidationErrors>
    )

    private val eventWrapperState = combine(
        isAliasSavedState,
        isAliasDraftSavedState,
        isApplyButtonEnabledState,
        mutableCloseScreenEventFlow,
        ::EventWrapper
    )

    private data class EventWrapper(
        val isAliasSaved: AliasSavedState,
        val isAliasDraftSaved: AliasDraftSavedState,
        val isApplyButtonEnabled: IsButtonEnabled,
        val closeScreenEvent: CloseScreenEvent
    )

    val baseAliasUiState: StateFlow<CreateUpdateAliasUiState> = combine(
        sharesWrapperState,
        aliasItemWrapperState,
        isLoadingState,
        eventWrapperState,
        hasUserEditedContentFlow
    ) { shareWrapper, aliasItemWrapper, isLoading, eventWrapper, hasUserEditedContent ->
        CreateUpdateAliasUiState(
            vaultList = shareWrapper.vaultList,
            selectedVault = shareWrapper.currentVault,
            aliasItem = aliasItemWrapper.aliasItem,
            isDraft = isDraft,
            errorList = aliasItemWrapper.aliasItemValidationErrors,
            isLoadingState = isLoading,
            isAliasSavedState = eventWrapper.isAliasSaved,
            isAliasDraftSavedState = eventWrapper.isAliasDraftSaved,
            isApplyButtonEnabled = eventWrapper.isApplyButtonEnabled,
            showVaultSelector = shareWrapper.vaultList.size > 1,
            closeScreenEvent = eventWrapper.closeScreenEvent,
            hasUserEditedContent = hasUserEditedContent
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CreateUpdateAliasUiState.Initial
        )

    abstract fun onTitleChange(value: String)

    open fun onNoteChange(value: String) {
        onUserEditedContent()
        aliasItemState.update { it.copy(note = value) }
    }

    open fun onMailboxesChanged(mailboxes: List<SelectedAliasMailboxUiModel>) {
        onUserEditedContent()
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
        onUserEditedContent()
        isLoadingState.update { IsLoadingState.Loading }
        selectedShareIdState.update { shareId.toOption() }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    fun setDraftStatus(status: Boolean) {
        isDraft = status
    }

    protected fun onUserEditedContent() {
        if (hasUserEditedContentFlow.value) return
        hasUserEditedContentFlow.update { true }
    }

    companion object {
        private const val TAG = "BaseAliasViewModel"
    }
}
