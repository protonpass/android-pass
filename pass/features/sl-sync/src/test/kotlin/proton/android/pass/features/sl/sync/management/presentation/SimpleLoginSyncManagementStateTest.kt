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
import proton.android.pass.domain.simplelogin.SimpleLoginAliasDomain
import proton.android.pass.domain.simplelogin.SimpleLoginAliasMailbox
import proton.android.pass.domain.simplelogin.SimpleLoginAliasSettings

class SimpleLoginSyncManagementStateTest {

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
        assertThat(state.canManageAliases).isFalse()
    }
}
