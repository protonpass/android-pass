package proton.android.pass.featureitemcreate.impl.alias

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.navigation.api.AliasOptionalNavArgId

abstract class BaseAliasViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val title: Option<String> = savedStateHandle
        .get<String>(AliasOptionalNavArgId.Title.key)
        .toOption()
    protected var isDraft: Boolean = false

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

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    val baseAliasUiState: StateFlow<BaseAliasUiState> = combine(
        aliasItemWrapperState,
        isLoadingState,
        eventWrapperState,
        hasUserEditedContentFlow
    ) { aliasItemWrapper, isLoading, eventWrapper, hasUserEditedContent ->
        BaseAliasUiState(
            aliasItem = aliasItemWrapper.aliasItem,
            isDraft = isDraft,
            errorList = aliasItemWrapper.aliasItemValidationErrors,
            isLoadingState = isLoading,
            isAliasSavedState = eventWrapper.isAliasSaved,
            isAliasDraftSavedState = eventWrapper.isAliasDraftSaved,
            isApplyButtonEnabled = eventWrapper.isApplyButtonEnabled,
            closeScreenEvent = eventWrapper.closeScreenEvent,
            hasUserEditedContent = hasUserEditedContent,
            hasReachedAliasLimit = false,
            canUpgrade = false
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BaseAliasUiState.Initial
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

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    fun setDraftStatus(status: Boolean) {
        isDraft = status
    }

    protected fun onUserEditedContent() {
        if (hasUserEditedContentFlow.value) return
        hasUserEditedContentFlow.update { true }
    }
}
