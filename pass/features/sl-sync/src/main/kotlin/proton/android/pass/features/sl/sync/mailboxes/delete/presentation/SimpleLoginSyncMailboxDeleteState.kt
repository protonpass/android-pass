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
import proton.android.pass.domain.simplelogin.SimpleLoginAliasMailbox

@Stable
internal data class SimpleLoginSyncMailboxDeleteState(
    internal val transferAliasMailboxes: List<SimpleLoginAliasMailbox>,
    internal val isTransferAliasesEnabled: Boolean,
    private val aliasMailboxOption: Option<SimpleLoginAliasMailbox>,
    private val selectedAliasMailboxOption: Option<SimpleLoginAliasMailbox>
) {

    internal val aliasMailboxEmail: String = when (aliasMailboxOption) {
        None -> ""
        is Some -> aliasMailboxOption.value.email
    }

    internal val transferAliasMailbox: String = when (selectedAliasMailboxOption) {
        None -> transferAliasMailboxes.firstOrNull { it.isDefault }?.email.orEmpty()
        is Some -> selectedAliasMailboxOption.value.email
    }

    internal val hasAliasTransferMailboxes: Boolean = transferAliasMailboxes.size > 1

    internal companion object {

        internal val Initial = SimpleLoginSyncMailboxDeleteState(
            isTransferAliasesEnabled = true,
            transferAliasMailboxes = emptyList(),
            aliasMailboxOption = None,
            selectedAliasMailboxOption = None
        )

    }

}
