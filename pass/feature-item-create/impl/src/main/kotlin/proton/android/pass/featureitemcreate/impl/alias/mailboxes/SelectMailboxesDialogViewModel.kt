package proton.android.pass.featureitemcreate.impl.alias.mailboxes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.featureitemcreate.impl.alias.SelectedAliasMailboxUiModel

class SelectMailboxesDialogViewModel : ViewModel() {

    private val canUpgradeState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val mailboxesState: MutableStateFlow<List<SelectedAliasMailboxUiModel>> =
        MutableStateFlow(emptyList())

    val uiState: StateFlow<SelectMailboxesUiState> = combine(
        mailboxesState,
        canUpgradeState
    ) { mailboxes, canUpgrade ->
        val canApply = mailboxes.any { it.selected }
        SelectMailboxesUiState(
            mailboxes = mailboxes,
            canApply = IsButtonEnabled.from(canApply),
            canUpgrade = canUpgrade
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SelectMailboxesUiState.Initial
    )

    fun setMailboxes(mailboxes: List<SelectedAliasMailboxUiModel>) {
        mailboxesState.update { mailboxes }
    }

    fun setCanUpgrade(canUpgrade: Boolean) {
        canUpgradeState.update { canUpgrade }
    }

    fun onMailboxChanged(mailbox: SelectedAliasMailboxUiModel) = viewModelScope.launch {
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
