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
import proton.android.pass.data.fakes.usecases.FakeGetUserPlan
import proton.android.pass.data.fakes.usecases.FakeObserveVaults
import proton.android.pass.data.fakes.usecases.simplelogin.FakeObserveSimpleLoginAliasDomains
import proton.android.pass.data.fakes.usecases.simplelogin.FakeObserveSimpleLoginAliasMailboxes
import proton.android.pass.data.fakes.usecases.simplelogin.FakeObserveSimpleLoginAliasSettings
import proton.android.pass.data.fakes.usecases.simplelogin.FakeObserveSimpleLoginSyncStatus
import proton.android.pass.domain.PlanType
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole
import proton.android.pass.domain.simplelogin.SimpleLoginSyncStatus
import proton.android.pass.domain.simplelogin.SimpleLoginAliasDomain
import proton.android.pass.domain.simplelogin.SimpleLoginAliasMailbox
import proton.android.pass.domain.simplelogin.SimpleLoginAliasSettings
import proton.android.pass.notifications.fakes.FakeSnackbarDispatcher
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.plans.PlanTestFactory
import proton.android.pass.test.domain.VaultTestFactory

class SimpleLoginSyncManagementViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `keeps mailboxes and domains visible when settings fail with only viewer vaults`() = runTest {
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
            observeSimpleLoginSyncStatus = FakeObserveSimpleLoginSyncStatus(),
            getUserPlan = FakeGetUserPlan().apply {
                setResult(Result.success(PlanTestFactory.create(planType = PAID_PLAN_TYPE)))
            },
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
        assertThat(state.canManageMailboxAliases).isTrue()

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
            getUserPlan = FakeGetUserPlan(),
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
        assertThat(state.canManageMailboxAliases).isFalse()

        collectJob.cancel()
    }

    @Test
    fun `allows mailbox management for paid users with a usable vault`() = runTest {
        val observeVaults = FakeObserveVaults().apply {
            sendResult(
                Result.success(
                    listOf(
                        VaultTestFactory.create(
                            shareId = ShareId("owned-share-id"),
                            isOwned = true,
                            role = ShareRole.Admin
                        )
                    )
                )
            )
        }
        val viewModel = SimpleLoginSyncManagementViewModel(
            observeVaults = observeVaults,
            observeSimpleLoginAliasDomains = FakeObserveSimpleLoginAliasDomains(),
            observeSimpleLoginAliasMailboxes = FakeObserveSimpleLoginAliasMailboxes(),
            observeSimpleLoginAliasSettings = FakeObserveSimpleLoginAliasSettings(),
            observeSimpleLoginSyncStatus = FakeObserveSimpleLoginSyncStatus(),
            getUserPlan = FakeGetUserPlan().apply {
                setResult(Result.success(PlanTestFactory.create(planType = PAID_PLAN_TYPE)))
            },
            snackbarDispatcher = FakeSnackbarDispatcher()
        )
        val collectJob = backgroundScope.launch {
            viewModel.state.collect { }
        }

        advanceUntilIdle()

        val state = viewModel.state.value
        assertThat(state.isNoVaults).isFalse()
        assertThat(state.canManageMailboxAliases).isTrue()

        collectJob.cancel()
    }

    @Test
    fun `falls back to live vault when sync status default vault was deleted`() = runTest {
        val staleVault = VaultTestFactory.create(
            shareId = ShareId("deleted-share-id"),
            name = "Deleted vault",
            isOwned = true,
            role = ShareRole.Admin
        )
        val recreatedVault = VaultTestFactory.create(
            shareId = ShareId("new-share-id"),
            name = "New vault",
            isOwned = true,
            role = ShareRole.Admin
        )
        val observeVaults = FakeObserveVaults().apply {
            sendResult(Result.success(listOf(recreatedVault)))
        }
        val observeSyncStatus = FakeObserveSimpleLoginSyncStatus().apply {
            updateSyncStatus(
                SimpleLoginSyncStatus(
                    isSyncEnabled = true,
                    isPreferenceEnabled = true,
                    pendingAliasCount = 0,
                    defaultVault = staleVault,
                    canManageAliases = true
                )
            )
        }
        val viewModel = SimpleLoginSyncManagementViewModel(
            observeVaults = observeVaults,
            observeSimpleLoginAliasDomains = FakeObserveSimpleLoginAliasDomains(),
            observeSimpleLoginAliasMailboxes = FakeObserveSimpleLoginAliasMailboxes(),
            observeSimpleLoginAliasSettings = FakeObserveSimpleLoginAliasSettings(),
            observeSimpleLoginSyncStatus = observeSyncStatus,
            getUserPlan = FakeGetUserPlan(),
            snackbarDispatcher = FakeSnackbarDispatcher()
        )
        val collectJob = backgroundScope.launch {
            viewModel.state.collect { }
        }

        advanceUntilIdle()

        val state = viewModel.state.value
        assertThat(state.isNoVaults).isFalse()
        assertThat(state.defaultVault?.shareId).isEqualTo(recreatedVault.shareId)
        assertThat(state.defaultVault?.name).isEqualTo("New vault")

        collectJob.cancel()
    }

    @Test
    fun `forces sync status refresh when usable vaults are available and when they change`() = runTest {
        val initialVault = VaultTestFactory.create(
            shareId = ShareId("old-share-id"),
            isOwned = true,
            role = ShareRole.Admin
        )
        val recreatedVault = VaultTestFactory.create(
            shareId = ShareId("new-share-id"),
            isOwned = true,
            role = ShareRole.Admin
        )
        val observeVaults = FakeObserveVaults().apply {
            sendResult(Result.success(listOf(initialVault)))
        }
        val observeSyncStatus = FakeObserveSimpleLoginSyncStatus().apply {
            updateSyncStatus(
                SimpleLoginSyncStatus(
                    isSyncEnabled = true,
                    isPreferenceEnabled = true,
                    pendingAliasCount = 0,
                    defaultVault = initialVault,
                    canManageAliases = true
                )
            )
        }
        val viewModel = SimpleLoginSyncManagementViewModel(
            observeVaults = observeVaults,
            observeSimpleLoginAliasDomains = FakeObserveSimpleLoginAliasDomains(),
            observeSimpleLoginAliasMailboxes = FakeObserveSimpleLoginAliasMailboxes(),
            observeSimpleLoginAliasSettings = FakeObserveSimpleLoginAliasSettings(),
            observeSimpleLoginSyncStatus = observeSyncStatus,
            getUserPlan = FakeGetUserPlan(),
            snackbarDispatcher = FakeSnackbarDispatcher()
        )
        val collectJob = backgroundScope.launch {
            viewModel.state.collect { }
        }

        advanceUntilIdle()
        observeVaults.sendResult(Result.success(listOf(recreatedVault)))
        advanceUntilIdle()

        assertThat(observeSyncStatus.forceRefreshValues).containsExactly(true, true).inOrder()

        collectJob.cancel()
    }

    @Test
    fun `does not show a selected domain when none is selected`() = runTest {
        val observeAliasDomains = FakeObserveSimpleLoginAliasDomains().apply {
            sendDomains(
                listOf(
                    SimpleLoginAliasDomain(
                        domain = "example.com",
                        isDefault = false,
                        isCustom = false,
                        isPremium = false,
                        isVerified = true
                    ),
                    SimpleLoginAliasDomain(
                        domain = "example.net",
                        isDefault = false,
                        isCustom = false,
                        isPremium = true,
                        isVerified = true
                    )
                )
            )
        }
        val viewModel = SimpleLoginSyncManagementViewModel(
            observeVaults = FakeObserveVaults().apply {
                sendResult(
                    Result.success(
                        listOf(
                            VaultTestFactory.create(
                                shareId = ShareId("owned-share-id"),
                                isOwned = true,
                                role = ShareRole.Admin
                            )
                        )
                    )
                )
            },
            observeSimpleLoginAliasDomains = observeAliasDomains,
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
            getUserPlan = FakeGetUserPlan(),
            snackbarDispatcher = FakeSnackbarDispatcher()
        )
        val collectJob = backgroundScope.launch {
            viewModel.state.collect { }
        }

        advanceUntilIdle()

        val state = viewModel.state.value
        assertThat(state.defaultDomain).isNull()
        assertThat(state.canSelectDomain).isTrue()

        collectJob.cancel()
    }

    private fun failingAliasSettingsFlow(): ObserveSimpleLoginAliasSettings = object : ObserveSimpleLoginAliasSettings {
        override fun invoke(): Flow<SimpleLoginAliasSettings> =
            flow { throw IllegalStateException("settings unavailable") }
    }

    private companion object {
        private val PAID_PLAN_TYPE = PlanType.Paid.Plus("plus", "Plus")
    }
}
