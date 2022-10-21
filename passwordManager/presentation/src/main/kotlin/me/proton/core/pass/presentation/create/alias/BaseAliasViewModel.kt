package me.proton.core.pass.presentation.create.alias

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.android.pass.notifications.api.SnackbarMessageRepository
import me.proton.core.pass.common.api.Option
import me.proton.core.pass.domain.AliasSuffix
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.presentation.uievents.IsLoadingState
import me.proton.core.pass.presentation.uievents.ItemSavedState

abstract class BaseAliasViewModel(
    private val snackbarMessageRepository: SnackbarMessageRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    protected val shareId: Option<ShareId> =
        Option.fromNullable(savedStateHandle.get<String>("shareId")?.let { ShareId(it) })

    private val shareIdState: Flow<Option<ShareId>> = MutableStateFlow(shareId)

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    val aliasItemState: MutableStateFlow<AliasItem> = MutableStateFlow(AliasItem.Empty)
    protected val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)
    protected val isItemSavedState: MutableStateFlow<ItemSavedState> =
        MutableStateFlow(ItemSavedState.Unknown)
    protected val aliasItemValidationErrorsState: MutableStateFlow<Set<AliasItemValidationErrors>> =
        MutableStateFlow(emptySet())

    private val aliasItemWrapperState = combine(
        aliasItemState,
        aliasItemValidationErrorsState
    ) { aliasItem, aliasItemValidationErrors ->
        AliasItemWrapper(aliasItem, aliasItemValidationErrors)
    }

    private data class AliasItemWrapper(
        val aliasItem: AliasItem,
        val aliasItemValidationErrors: Set<AliasItemValidationErrors>
    )

    val aliasUiState: StateFlow<CreateUpdateAliasUiState> = combine(
        shareIdState,
        aliasItemWrapperState,
        isLoadingState,
        isItemSavedState
    ) { shareId, aliasItemWrapper, isLoading, isItemSaved ->
        CreateUpdateAliasUiState(
            shareId = shareId,
            aliasItem = aliasItemWrapper.aliasItem,
            errorList = aliasItemWrapper.aliasItemValidationErrors,
            isLoadingState = isLoading,
            isItemSaved = isItemSaved
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CreateUpdateAliasUiState.Initial
        )

    fun onTitleChange(value: String) {
        aliasItemState.update { it.copy(title = value) }
        aliasItemValidationErrorsState.update {
            it.toMutableSet()
                .apply { remove(AliasItemValidationErrors.BlankTitle) }
        }
    }

    fun onAliasChange(value: String) {
        if (value.contains(" ") || value.contains("\n")) return
        aliasItemState.update {
            it.copy(
                alias = value,
                aliasToBeCreated = getAliasToBeCreated(
                    alias = value,
                    suffix = aliasItemState.value.selectedSuffix
                )
            )
        }
        aliasItemValidationErrorsState.update {
            it.toMutableSet()
                .apply { remove(AliasItemValidationErrors.BlankAlias) }
        }
    }

    fun onNoteChange(value: String) {
        aliasItemState.update { it.copy(note = value) }
    }

    fun onSuffixChange(suffix: AliasSuffix) {
        aliasItemState.update {
            it.copy(
                selectedSuffix = suffix,
                aliasToBeCreated = getAliasToBeCreated(
                    alias = aliasItemState.value.alias,
                    suffix = suffix
                )
            )
        }
    }

    fun onMailboxChange(mailbox: AliasMailboxUiModel) {
        val mailboxes = aliasItemState.value.mailboxes.map {
            if (it.model.id == mailbox.model.id) {
                it.copy(selected = !mailbox.selected)
            } else {
                it
            }
        }

        val allSelectedMailboxes = mailboxes.filter { it.selected }
        var mailboxTitle = allSelectedMailboxes.firstOrNull()?.model?.email ?: ""
        if (allSelectedMailboxes.size > 1) {
            val howManyMore = allSelectedMailboxes.size - 1
            mailboxTitle += " ($howManyMore+)"
        }

        aliasItemState.update {
            it.copy(
                mailboxes = mailboxes,
                mailboxTitle = mailboxTitle,
                isMailboxListApplicable = allSelectedMailboxes.isNotEmpty()
            )
        }
    }

    fun onEmitSnackbarMessage(snackbarMessage: AliasSnackbarMessage) {
        viewModelScope.launch {
            snackbarMessageRepository.emitSnackbarMessage(snackbarMessage)
        }
    }

    private fun getAliasToBeCreated(alias: String, suffix: AliasSuffix?): String? {
        if (suffix != null && alias.isNotBlank()) {
            return "$alias${suffix.suffix}"
        }
        return null
    }
}
