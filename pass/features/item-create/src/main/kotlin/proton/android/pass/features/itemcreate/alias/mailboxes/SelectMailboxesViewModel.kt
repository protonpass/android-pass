/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.features.itemcreate.alias.mailboxes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.features.itemcreate.alias.SelectedAliasMailboxUiModel

class SelectMailboxesViewModel : ViewModel() {

    private val canUpgradeState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val mailboxesState: MutableStateFlow<List<SelectedAliasMailboxUiModel>> =
        MutableStateFlow(emptyList())

    internal val uiState: StateFlow<SelectMailboxesUiState> = combine(
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

    internal fun setMailboxes(mailboxes: List<SelectedAliasMailboxUiModel>) {
        mailboxesState.update { mailboxes }
    }

    internal fun setCanUpgrade(canUpgrade: Boolean) {
        canUpgradeState.update { canUpgrade }
    }

    internal fun onMailboxChanged(newMailbox: SelectedAliasMailboxUiModel) = mailboxesState.value
        .map { mailbox ->
            if (mailbox.model.id == newMailbox.model.id) {
                mailbox.copy(selected = !newMailbox.selected)
            } else {
                mailbox
            }
        }
        .let { mailboxes ->
            mailboxesState.update { mailboxes }
        }

}
