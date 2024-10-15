/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.features.sl.sync.mailboxes.delete.presentation

import androidx.compose.runtime.Stable
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.domain.simplelogin.SimpleLoginAliasMailbox

@Stable
internal data class SimpleLoginSyncMailboxDeleteState(
    internal val transferAliasMailboxes: List<SimpleLoginAliasMailbox>,
    internal val isTransferAliasesEnabled: Boolean,
    internal val event: SimpleLoginSyncMailboxDeleteEvent,
    private val aliasMailboxOption: Option<SimpleLoginAliasMailbox>,
    private val selectedAliasMailboxOption: Option<SimpleLoginAliasMailbox>,
    private val isLoadingState: IsLoadingState
) {

    internal val aliasMailboxEmail: String = when (aliasMailboxOption) {
        None -> ""
        is Some -> aliasMailboxOption.value.email
    }

    internal val transferAliasMailboxId: Long? = when (selectedAliasMailboxOption) {
        None -> transferAliasMailboxes.firstOrNull { it.isDefault }?.id
        is Some -> selectedAliasMailboxOption.value.id
    }

    internal val transferAliasMailboxEmail: String = when (selectedAliasMailboxOption) {
        None -> transferAliasMailboxes.firstOrNull { it.isDefault }?.email.orEmpty()
        is Some -> selectedAliasMailboxOption.value.email
    }

    internal val hasAliasTransferMailboxes: Boolean = transferAliasMailboxes.size > 1

    internal val isLoading: Boolean = isLoadingState.value()

    internal companion object {

        internal val Initial = SimpleLoginSyncMailboxDeleteState(
            isTransferAliasesEnabled = true,
            transferAliasMailboxes = emptyList(),
            event = SimpleLoginSyncMailboxDeleteEvent.Idle,
            aliasMailboxOption = None,
            selectedAliasMailboxOption = None,
            isLoadingState = IsLoadingState.NotLoading
        )

    }

}
