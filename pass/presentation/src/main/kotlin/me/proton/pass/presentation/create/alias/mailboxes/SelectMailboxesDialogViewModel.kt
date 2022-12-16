package me.proton.pass.presentation.create.alias.mailboxes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.pass.presentation.create.alias.AliasMailboxUiModel
import me.proton.pass.presentation.uievents.IsButtonEnabled

class SelectMailboxesDialogViewModel : ViewModel() {

    private val mailboxesState: MutableStateFlow<List<AliasMailboxUiModel>> =
        MutableStateFlow(emptyList())

    val uiState: StateFlow<SelectMailboxesUiState> = mailboxesState.map { mailboxes ->
        val canApply = mailboxes.any { it.selected }
        SelectMailboxesUiState(
            mailboxes = mailboxes,
            canApply = IsButtonEnabled.from(canApply)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SelectMailboxesUiState.Initial
    )

    fun setMailboxes(mailboxes: List<AliasMailboxUiModel>) {
        mailboxesState.update { mailboxes }
    }

    fun onMailboxChanged(mailbox: AliasMailboxUiModel) = viewModelScope.launch {
        val mailboxes = mailboxesState.value.map {
            if (it.model.id == mailbox.model.id) {
                it.copy(selected = !mailbox.selected)
            } else {
                it
            }
        }
        mailboxesState.update { mailboxes }
    }

}
