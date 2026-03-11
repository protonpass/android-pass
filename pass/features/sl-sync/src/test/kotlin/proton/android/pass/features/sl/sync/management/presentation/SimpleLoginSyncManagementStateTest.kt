/*
 * Copyright (c) 2026 Proton AG
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

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import proton.android.pass.common.api.None
import proton.android.pass.common.api.some
import proton.android.pass.domain.ShareFlags
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole
import proton.android.pass.domain.Vault
import proton.android.pass.domain.VaultId
import proton.android.pass.domain.hasUsableSimpleLoginVaults
import proton.android.pass.domain.simplelogin.SimpleLoginAliasDomain
import proton.android.pass.domain.simplelogin.SimpleLoginAliasMailbox
import proton.android.pass.domain.simplelogin.SimpleLoginAliasSettings
import me.proton.core.domain.entity.UserId
import java.util.Date

class SimpleLoginSyncManagementStateTest {

    @Test
    fun `treats viewer-only vaults as no usable vaults`() {
        val viewerVault = Vault(
            userId = UserId("user-id"),
            shareId = ShareId("viewer-share-id"),
            vaultId = VaultId("viewer-vault-id"),
            name = "Viewer vault",
            isOwned = false,
            role = ShareRole.Read,
            createTime = Date(),
            shareFlags = ShareFlags(0)
        )

        assertThat(listOf(viewerVault).hasUsableSimpleLoginVaults()).isFalse()
    }

    @Test
    fun `treats writable shared vaults as usable vaults`() {
        val writableVault = Vault(
            userId = UserId("user-id"),
            shareId = ShareId("writable-share-id"),
            vaultId = VaultId("writable-vault-id"),
            name = "Writable vault",
            isOwned = false,
            role = ShareRole.Write,
            createTime = Date(),
            shareFlags = ShareFlags(0)
        )

        assertThat(listOf(writableVault).hasUsableSimpleLoginVaults()).isTrue()
    }

    @Test
    fun `keeps mailboxes visible and mailbox management enabled when no vaults`() {
        val mailbox = SimpleLoginAliasMailbox(
            id = 1L,
            email = "alias@example.com",
            pendingEmail = null,
            isDefault = true,
            isVerified = true,
            aliasCount = 3
        )

        val state = SimpleLoginSyncManagementState(
            isUpdating = false,
            event = SimpleLoginSyncManagementEvent.Idle,
            modelOption = SimpleLoginSyncManagementModel(
                aliasDomains = listOf(
                    SimpleLoginAliasDomain(
                        domain = "example.com",
                        isDefault = true,
                        isCustom = false,
                        isPremium = false,
                        isVerified = true
                    )
                ),
                aliasMailboxes = listOf(mailbox),
                aliasSettings = SimpleLoginAliasSettings(
                    defaultDomain = "example.com",
                    defaultMailboxId = mailbox.id
                ),
                syncStatusOption = None
            ).some(),
            hasVaults = false
        )

        assertThat(state.isNoVaults).isTrue()
        assertThat(state.isLoading).isFalse()
        assertThat(state.aliasMailboxes).containsExactly(mailbox)
        assertThat(state.canManageMailboxAliases).isTrue()
        assertThat(state.canSelectPremiumDomains).isTrue()
        assertThat(state.canManageAliases).isFalse()
    }
}
