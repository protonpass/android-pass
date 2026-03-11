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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import proton.android.pass.data.api.usecases.simplelogin.ObserveSimpleLoginAliasSettings
import proton.android.pass.data.api.usecases.simplelogin.ObserveSimpleLoginSyncStatus
import proton.android.pass.data.fakes.usecases.FakeObserveVaults
import proton.android.pass.data.fakes.usecases.simplelogin.FakeObserveSimpleLoginAliasDomains
import proton.android.pass.data.fakes.usecases.simplelogin.FakeObserveSimpleLoginAliasMailboxes
import proton.android.pass.data.fakes.usecases.simplelogin.FakeObserveSimpleLoginAliasSettings
import proton.android.pass.data.fakes.usecases.simplelogin.FakeObserveSimpleLoginSyncStatus
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole
import proton.android.pass.domain.simplelogin.SimpleLoginAliasDomain
import proton.android.pass.domain.simplelogin.SimpleLoginAliasMailbox
import proton.android.pass.domain.simplelogin.SimpleLoginAliasSettings
import proton.android.pass.notifications.fakes.FakeSnackbarDispatcher
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.VaultTestFactory

class SimpleLoginSyncManagementViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `keeps mailboxes and domains visible when settings and sync status fail with only viewer vaults`() = runTest {
        val mailbox = SimpleLoginAliasMailbox(
            id = 1L,
            email = "alias@example.com",
            pendingEmail = null,
            isDefault = true,
            isVerified = true,
            aliasCount = 2
        )
        val observeVaults = FakeObserveVaults().apply {
            sendResult(
                Result.success(
                    listOf(
                        VaultTestFactory.create(
                            shareId = ShareId("viewer-share-id"),
                            isOwned = false,
                            role = ShareRole.Read
                        )
                    )
                )
            )
        }
        val observeAliasDomains = FakeObserveSimpleLoginAliasDomains().apply {
            sendDomains(
                listOf(
                    SimpleLoginAliasDomain(
                        domain = "example.com",
                        isDefault = true,
                        isCustom = false,
                        isPremium = false,
                        isVerified = true
                    ),
                    SimpleLoginAliasDomain(
                        domain = "example.net",
                        isDefault = false,
                        isCustom = false,
                        isPremium = false,
                        isVerified = true
                    )
                )
            )
        }
        val observeAliasMailboxes = FakeObserveSimpleLoginAliasMailboxes().apply {
            sendMailboxes(listOf(mailbox))
        }
        val viewModel = SimpleLoginSyncManagementViewModel(
            observeVaults = observeVaults,
            observeSimpleLoginAliasDomains = observeAliasDomains,
            observeSimpleLoginAliasMailboxes = observeAliasMailboxes,
            observeSimpleLoginAliasSettings = failingAliasSettingsFlow(),
            observeSimpleLoginSyncStatus = failingSyncStatusFlow(),
            snackbarDispatcher = FakeSnackbarDispatcher()
        )
        val collectJob = backgroundScope.launch {
            viewModel.state.collect { }
        }

        advanceUntilIdle()

        val state = viewModel.state.value
        assertThat(state.isNoVaults).isTrue()
        assertThat(state.isLoading).isFalse()
        assertThat(state.aliasMailboxes).containsExactly(mailbox)
        assertThat(state.canSelectDomain).isTrue()
        assertThat(state.defaultDomain).isEqualTo("example.com")

        collectJob.cancel()
    }

    @Test
    fun `does not stay loading when no usable vaults and alias detail flows have no initial value`() = runTest {
        val observeVaults = FakeObserveVaults().apply {
            sendResult(
                Result.success(
                    listOf(
                        VaultTestFactory.create(
                            shareId = ShareId("viewer-share-id"),
                            isOwned = false,
                            role = ShareRole.Read
                        )
                    )
                )
            )
        }
        val viewModel = SimpleLoginSyncManagementViewModel(
            observeVaults = observeVaults,
            observeSimpleLoginAliasDomains = FakeObserveSimpleLoginAliasDomains(),
            observeSimpleLoginAliasMailboxes = FakeObserveSimpleLoginAliasMailboxes(),
            observeSimpleLoginAliasSettings = FakeObserveSimpleLoginAliasSettings().apply {
                sendSettings(
                    SimpleLoginAliasSettings(
                        defaultDomain = null,
                        defaultMailboxId = 0
                    )
                )
            },
            observeSimpleLoginSyncStatus = FakeObserveSimpleLoginSyncStatus(),
            snackbarDispatcher = FakeSnackbarDispatcher()
        )
        val collectJob = backgroundScope.launch {
            viewModel.state.collect { }
        }

        advanceUntilIdle()

        val state = viewModel.state.value
        assertThat(state.isNoVaults).isTrue()
        assertThat(state.isLoading).isFalse()
        assertThat(state.aliasMailboxes).isEmpty()
        assertThat(state.defaultDomain).isNull()

        collectJob.cancel()
    }

    private fun failingAliasSettingsFlow(): ObserveSimpleLoginAliasSettings = object : ObserveSimpleLoginAliasSettings {
        override fun invoke(): Flow<proton.android.pass.domain.simplelogin.SimpleLoginAliasSettings> =
            flow { throw IllegalStateException("settings unavailable") }
    }

    private fun failingSyncStatusFlow(): ObserveSimpleLoginSyncStatus = object : ObserveSimpleLoginSyncStatus {
        override fun invoke(): Flow<proton.android.pass.domain.simplelogin.SimpleLoginSyncStatus> =
            flow { throw IllegalStateException("sync status unavailable") }
    }
}
