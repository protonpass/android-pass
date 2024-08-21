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
    internal val isUpdating: Boolean,
    internal val event: SimpleLoginSyncDetailsEvent,
    private val modelOption: Option<SimpleLoginSyncDetailsModel>,
    private val selectedDomainOption: Option<String>,
    private val selectedMailboxOption: Option<SimpleLoginAliasMailbox>
) {

    internal val defaultDomain: String = when (modelOption) {
        None -> ""
        is Some -> modelOption.value.defaultDomain
    }

    private val defaultMailbox: SimpleLoginAliasMailbox? = when (modelOption) {
        None -> null
        is Some -> modelOption.value
            .aliasMailboxes
            .firstOrNull { aliasMailbox -> aliasMailbox.id == modelOption.value.defaultMailboxId }
    }

    internal val defaultMailboxEmail: String = defaultMailbox?.email.orEmpty()

    internal val defaultVaultOption: Option<Vault> = when (modelOption) {
        None -> None
        is Some -> modelOption.value.defaultVault
    }

    internal val aliasDomains: List<SimpleLoginAliasDomain> = when (modelOption) {
        None -> emptyList()
        is Some -> modelOption.value.aliasDomains
    }

    internal val canSelectDomain: Boolean = aliasDomains.size > 1

    internal val aliasMailboxes: List<SimpleLoginAliasMailbox> = when (modelOption) {
        None -> emptyList()
        is Some -> modelOption.value.aliasMailboxes
    }

    internal val canSelectMailbox: Boolean = true

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

    internal val pendingAliasesCount: Int = when (modelOption) {
        None -> 0
        is Some -> modelOption.value.pendingAliasesCount
    }

    internal val isLoading: Boolean = when (modelOption) {
        None -> true
        is Some -> false
    }

    internal companion object {

        internal val Initial: SimpleLoginSyncDetailsState = SimpleLoginSyncDetailsState(
            isUpdating = false,
            event = SimpleLoginSyncDetailsEvent.Idle,
            modelOption = None,
            selectedDomainOption = None,
            selectedMailboxOption = None
        )

    }

}
