package me.proton.android.pass.ui.create.alias

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.android.pass.ui.shared.uievents.IsLoadingState
import me.proton.android.pass.ui.shared.uievents.ItemSavedState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import me.proton.core.pass.domain.AliasMailbox
import me.proton.core.pass.domain.AliasSuffix

abstract class BaseAliasViewModel(
    private val accountManager: AccountManager
) : ViewModel() {

    protected val aliasItemState: MutableStateFlow<AliasItem> = MutableStateFlow(AliasItem.Empty)
    protected val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)
    protected val isItemSavedState: MutableStateFlow<ItemSavedState> =
        MutableStateFlow(ItemSavedState.Unknown)
    protected val aliasItemValidationErrorsState: MutableStateFlow<Set<AliasItemValidationErrors>> =
        MutableStateFlow(emptySet())

    val aliasUiState: StateFlow<CreateUpdateAliasUiState> = combine(
        aliasItemState,
        isLoadingState,
        isItemSavedState,
        aliasItemValidationErrorsState
    ) { aliasItem, isLoading, isItemSaved, aliasItemValidationErrors ->
        CreateUpdateAliasUiState(
            aliasItem = aliasItem,
            errorList = aliasItemValidationErrors,
            isLoadingState = isLoading,
            isItemSaved = isItemSaved
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CreateUpdateAliasUiState.Initial
        )

    fun onTitleChange(value: String) = viewModelScope.launch {
        aliasItemState.value = aliasItemState.value.copy(title = value)
        aliasItemValidationErrorsState.value = aliasItemValidationErrorsState.value.toMutableSet()
            .apply { remove(AliasItemValidationErrors.BlankTitle) }
    }

    fun onAliasChange(value: String) = viewModelScope.launch {
        if (value.contains(" ") || value.contains("\n")) return@launch
        aliasItemState.value = aliasItemState.value.copy(
            alias = value,
            aliasToBeCreated = getAliasToBeCreated(
                alias = value,
                suffix = aliasItemState.value.selectedSuffix
            )
        )
        aliasItemValidationErrorsState.value = aliasItemValidationErrorsState.value.toMutableSet()
            .apply { remove(AliasItemValidationErrors.BlankAlias) }
    }

    fun onNoteChange(value: String) = viewModelScope.launch {
        aliasItemState.value = aliasItemState.value.copy(note = value)
    }

    fun onSuffixChange(suffix: AliasSuffix) = viewModelScope.launch {
        aliasItemState.value = aliasItemState.value.copy(
            selectedSuffix = suffix,
            aliasToBeCreated = getAliasToBeCreated(
                alias = aliasItemState.value.alias,
                suffix = suffix
            )
        )
    }

    fun onMailboxChange(mailbox: AliasMailbox) = viewModelScope.launch {
        aliasItemState.value = aliasItemState.value.copy(selectedMailbox = mailbox)
    }

    protected suspend fun withUserId(block: suspend (UserId) -> Unit) {
        val userId = accountManager.getPrimaryUserId().first { userId -> userId != null }
        userId?.let { block(it) }
    }

    private fun getAliasToBeCreated(alias: String, suffix: AliasSuffix?): String? {
        if (suffix != null && alias.isNotBlank()) {
            return "$alias${suffix.suffix}"
        }
        return null
    }
}
