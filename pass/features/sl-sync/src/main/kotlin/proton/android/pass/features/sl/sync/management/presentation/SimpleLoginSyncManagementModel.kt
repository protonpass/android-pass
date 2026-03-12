/*
 * Copyright (c) 2024-2026 Proton AG
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

import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.domain.Vault
import proton.android.pass.domain.canBeUsedForSimpleLogin
import proton.android.pass.domain.selectSimpleLoginFallbackVault
import proton.android.pass.domain.simplelogin.SimpleLoginAliasDomain
import proton.android.pass.domain.simplelogin.SimpleLoginAliasMailbox
import proton.android.pass.domain.simplelogin.SimpleLoginAliasSettings
import proton.android.pass.domain.simplelogin.SimpleLoginSyncStatus

internal data class SimpleLoginSyncManagementModel(
    private val vaults: List<Vault>,
    internal val aliasDomains: List<SimpleLoginAliasDomain>,
    internal val aliasMailboxes: List<SimpleLoginAliasMailbox>,
    private val aliasSettings: SimpleLoginAliasSettings,
    private val syncStatusOption: Option<SimpleLoginSyncStatus>
) {

    private val usableVaults: List<Vault> = vaults.filter(Vault::canBeUsedForSimpleLogin)

    internal val defaultDomain: String? = aliasSettings.defaultDomain
        ?: aliasDomains.firstOrNull { aliasDomain -> aliasDomain.isDefault }?.domain

    internal val defaultMailboxId: Long = aliasSettings.defaultMailboxId
        .takeIf { defaultMailboxId -> defaultMailboxId != 0L }
        ?: aliasMailboxes.firstOrNull { aliasMailbox -> aliasMailbox.isDefault }?.id
        ?: 0L

    internal val defaultVault: Vault? = when (syncStatusOption) {
        None -> usableVaults.selectSimpleLoginFallbackVault()
        is Some -> usableVaults.firstOrNull { vault ->
            vault.shareId == syncStatusOption.value.defaultVault.shareId
        } ?: usableVaults.selectSimpleLoginFallbackVault()
    }

    internal val pendingAliasesCount: Int = when (syncStatusOption) {
        None -> 0
        is Some -> syncStatusOption.value.pendingAliasCount
    }

    internal val isSyncEnabled: Boolean = when (syncStatusOption) {
        None -> false
        is Some -> syncStatusOption.value.isSyncEnabled
    }

    internal val canManageAliases: Boolean = when (syncStatusOption) {
        None -> false
        is Some -> syncStatusOption.value.canManageAliases
    }

    internal companion object {
        internal val DefaultAliasSettings = SimpleLoginAliasSettings(
            defaultDomain = null,
            defaultMailboxId = 0
        )
    }

}
