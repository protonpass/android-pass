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

package proton.android.pass.features.sl.sync.mailboxes.options.presentation

import androidx.compose.runtime.Stable
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.domain.simplelogin.SimpleLoginAliasMailbox

@Stable
internal data class SimpleLoginSyncMailboxOptionsState(
    internal val event: SimpleLoginSyncMailboxOptionsEvent,
    internal val action: SimpleLoginSyncMailboxOptionsAction,
    private val aliasMailboxOption: Option<SimpleLoginAliasMailbox>
) {

    private val isDefault = when (aliasMailboxOption) {
        None -> false
        is Some -> aliasMailboxOption.value.isDefault
    }

    private val isVerified = when (aliasMailboxOption) {
        None -> false
        is Some -> aliasMailboxOption.value.isVerified
    }

    private val hasPendingEmailChange = when (aliasMailboxOption) {
        None -> false
        is Some -> !aliasMailboxOption.value.pendingEmail.isNullOrBlank()
    }

    private val hasAliases = when (aliasMailboxOption) {
        None -> false
        is Some -> aliasMailboxOption.value.aliasCount > 0
    }

    internal val canSetAsDefault: Boolean = !isDefault && isVerified

    internal val canVerify: Boolean = !isVerified || hasPendingEmailChange

    internal val canDelete: Boolean = !isDefault

    internal val canTransferAliases: Boolean = isVerified && hasAliases

    internal val canChangeMailbox: Boolean = !hasPendingEmailChange

    internal val canCancelMailboxChange: Boolean = hasPendingEmailChange

    internal companion object {

        internal val Initial = SimpleLoginSyncMailboxOptionsState(
            aliasMailboxOption = None,
            event = SimpleLoginSyncMailboxOptionsEvent.Idle,
            action = SimpleLoginSyncMailboxOptionsAction.None
        )

    }

}
