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

package proton.android.pass.features.sl.sync.details.presentation

import androidx.compose.runtime.Stable
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.domain.Vault
import proton.android.pass.domain.simplelogin.SimpleLoginAliasDomain
import proton.android.pass.domain.simplelogin.SimpleLoginAliasMailbox

@Stable
internal data class SimpleLoginSyncDetailsState(
    internal val aliasDomains: List<SimpleLoginAliasDomain>,
    internal val aliasMailboxes: List<SimpleLoginAliasMailbox>,
    internal val defaultVaultOption: Option<Vault>,
    internal val pendingAliasesCountOption: Option<Int>,
    internal val isLoading: Boolean,
    internal val isUpdating: Boolean,
    internal val event: SimpleLoginSyncDetailsEvent,
    private val selectedDomainOption: Option<String>,
    private val selectedMailboxOption: Option<SimpleLoginAliasMailbox>
) {

    internal val defaultDomain: String = aliasDomains
        .firstOrNull { aliasDomain -> aliasDomain.isDefault }
        ?.domain
        .orEmpty()

    private val defaultMailbox: SimpleLoginAliasMailbox? =
        aliasMailboxes.firstOrNull { aliasMailbox ->
            aliasMailbox.isDefault
        }

    internal val defaultMailboxEmail: String = defaultMailbox?.email.orEmpty()

    internal val selectedAliasDomain: String = when (selectedDomainOption) {
        None -> defaultDomain
        is Some -> selectedDomainOption.value
    }

    internal val selectedAliasMailboxId: String = when (selectedMailboxOption) {
        None -> defaultMailbox?.id.orEmpty()
        is Some -> selectedMailboxOption.value.id
    }

    internal val selectedAliasMailboxEmail: String = when (selectedMailboxOption) {
        None -> defaultMailbox?.email.orEmpty()
        is Some -> selectedMailboxOption.value.email
    }

    internal val pendingAliasesCount: Int = when (pendingAliasesCountOption) {
        None -> 0
        is Some -> pendingAliasesCountOption.value
    }

    internal companion object {

        internal val Initial: SimpleLoginSyncDetailsState = SimpleLoginSyncDetailsState(
            aliasDomains = emptyList(),
            aliasMailboxes = emptyList(),
            defaultVaultOption = None,
            pendingAliasesCountOption = None,
            isLoading = true,
            isUpdating = false,
            event = SimpleLoginSyncDetailsEvent.Idle,
            selectedDomainOption = None,
            selectedMailboxOption = None
        )

    }

}
