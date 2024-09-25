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

package proton.android.pass.features.sl.sync.management.presentation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.domain.Vault
import proton.android.pass.domain.simplelogin.SimpleLoginAliasDomain
import proton.android.pass.domain.simplelogin.SimpleLoginAliasMailbox

@Stable
internal data class SimpleLoginSyncManagementState(
    internal val isUpdating: Boolean,
    internal val event: SimpleLoginSyncManagementEvent,
    private val modelOption: Option<SimpleLoginSyncManagementModel>,
    private val selectedDomainOption: Option<String?>
) {

    internal val defaultDomain: String? = when (modelOption) {
        None -> null
        is Some -> modelOption.value.defaultDomain
    }

    internal val defaultVault: Vault? = when (modelOption) {
        None -> null
        is Some -> modelOption.value.defaultVault
    }

    private val aliasDomains: List<SimpleLoginAliasDomain> = when (modelOption) {
        None -> emptyList()
        is Some -> modelOption.value.aliasDomains
    }

    internal val aliasDomainOptions: ImmutableList<String?> = mutableStateListOf<String?>().apply {
        add(null) // This is to support the "Not selected" option
        addAll(aliasDomains.map { it.domain })
    }.toPersistentList()

    internal val canSelectDomain: Boolean = aliasDomains.size > 1

    internal val aliasMailboxes: ImmutableList<SimpleLoginAliasMailbox> = when (modelOption) {
        None -> persistentListOf()
        is Some -> modelOption.value.aliasMailboxes.toPersistentList()
    }

    internal val selectedAliasDomain: String? = when (selectedDomainOption) {
        None -> defaultDomain
        is Some -> selectedDomainOption.value
    }

    internal val isSyncEnabled: Boolean = when (modelOption) {
        None -> false
        is Some -> modelOption.value.isSyncEnabled
    }

    internal val pendingAliasesCount: Int = when (modelOption) {
        None -> 0
        is Some -> modelOption.value.pendingAliasesCount
    }

    internal val hasPendingAliases: Boolean = pendingAliasesCount > 0

    internal val isLoading: Boolean = when (modelOption) {
        None -> true
        is Some -> false
    }

    internal val canUpdateDomain: Boolean = defaultDomain != selectedAliasDomain

    // The -1 is required since we added a null item to the beginning of the list to support the "Not selected" option
    internal fun getAliasDomain(position: Int): SimpleLoginAliasDomain? = aliasDomains
        .getOrNull(position.minus(1))

    internal companion object {

        internal val Initial: SimpleLoginSyncManagementState = SimpleLoginSyncManagementState(
            isUpdating = false,
            event = SimpleLoginSyncManagementEvent.Idle,
            modelOption = None,
            selectedDomainOption = None
        )

    }

}
