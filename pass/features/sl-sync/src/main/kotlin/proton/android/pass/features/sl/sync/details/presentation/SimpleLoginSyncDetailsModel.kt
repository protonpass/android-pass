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

import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.some
import proton.android.pass.domain.Vault
import proton.android.pass.domain.simplelogin.SimpleLoginAliasDomain
import proton.android.pass.domain.simplelogin.SimpleLoginAliasMailbox
import proton.android.pass.domain.simplelogin.SimpleLoginAliasSettings
import proton.android.pass.domain.simplelogin.SimpleLoginSyncStatus

internal data class SimpleLoginSyncDetailsModel(
    internal val aliasDomains: List<SimpleLoginAliasDomain>,
    internal val aliasMailboxes: List<SimpleLoginAliasMailbox>,
    private val aliasSettings: SimpleLoginAliasSettings,
    private val syncStatusOption: Option<SimpleLoginSyncStatus>
) {

    internal val defaultDomain: String? = aliasSettings.defaultDomain

    internal val defaultMailboxId: String = aliasSettings.defaultMailboxId

    internal val defaultVault: Option<Vault> = when (syncStatusOption) {
        None -> None
        is Some -> syncStatusOption.value.defaultVault.some()
    }

    internal val pendingAliasesCount: Int = when (syncStatusOption) {
        None -> 0
        is Some -> syncStatusOption.value.pendingAliasCount
    }

}
