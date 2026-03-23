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

package proton.android.pass.features.home.drawer.presentation

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.data.api.ItemCountSummary
import proton.android.pass.data.api.usecases.UpgradeInfo
import proton.android.pass.data.fakes.usecases.FakeCanCreateFolder
import proton.android.pass.data.fakes.usecases.FakeCanCreateVault
import proton.android.pass.data.fakes.usecases.FakeCanOrganiseVaults
import proton.android.pass.data.fakes.usecases.FakeObserveItemCount
import proton.android.pass.data.fakes.usecases.FakeObserveUpgradeInfo
import proton.android.pass.data.fakes.usecases.FakeObserveVaultsWithItemCount
import proton.android.pass.data.fakes.usecases.folders.FakeObserveFolders
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.Plan
import proton.android.pass.domain.PlanLimit
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.preferences.FakeFeatureFlagsPreferenceRepository
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.searchoptions.fakes.FakeHomeSearchOptionsRepository
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.TestConstants
import proton.android.pass.test.domain.FolderTestFactory
import proton.android.pass.test.domain.VaultTestFactory

internal class HomeDrawerViewModelTest {

    @get:Rule
    internal val dispatcherRule = MainDispatcherRule()

    private lateinit var observeVaultsWithItemCount: FakeObserveVaultsWithItemCount
    private lateinit var observeItemCount: FakeObserveItemCount
    private lateinit var canCreateFolder: FakeCanCreateFolder
    private lateinit var canCreateVault: FakeCanCreateVault
    private lateinit var canOrganiseVaults: FakeCanOrganiseVaults
    private lateinit var observeUpgradeInfo: FakeObserveUpgradeInfo
    private lateinit var featureFlags: FakeFeatureFlagsPreferenceRepository
    private lateinit var homeSearchOptionsRepository: FakeHomeSearchOptionsRepository
    private lateinit var observeFolders: FakeObserveFolders

    private lateinit var viewModel: HomeDrawerViewModel

    @Before
    internal fun setup() {
        observeVaultsWithItemCount = FakeObserveVaultsWithItemCount()
        observeItemCount = FakeObserveItemCount()
        canCreateFolder = FakeCanCreateFolder()
        canCreateVault = FakeCanCreateVault()
        canOrganiseVaults = FakeCanOrganiseVaults()
        observeUpgradeInfo = FakeObserveUpgradeInfo()
        featureFlags = FakeFeatureFlagsPreferenceRepository()
        homeSearchOptionsRepository = FakeHomeSearchOptionsRepository()
        observeFolders = FakeObserveFolders()

        canCreateFolder.sendValue(true)
        canCreateVault.sendValue(true)
        canOrganiseVaults.sendValue(true)
        observeUpgradeInfo.setResult(FakeObserveUpgradeInfo.DEFAULT)
        observeItemCount.sendResult(Result.success(ItemCountSummary.Initial))

        viewModel = HomeDrawerViewModel(
            canCreateFolder = canCreateFolder,
            canCreateVault = canCreateVault,
            canOrganiseVaults = canOrganiseVaults,
            observeVaultsWithItemCount = observeVaultsWithItemCount,
            observeItemCount = observeItemCount,
            homeSearchOptionsRepository = homeSearchOptionsRepository,
            observeUpgradeInfo = observeUpgradeInfo,
            featureFlagsPreferencesRepository = featureFlags,
            observeFolders = observeFolders
        )
    }

    @Test
    internal fun `emits folders in state when PASS_FOLDERS is enabled`() = runTest {
        val userId = UserId("user-1")
        val shareId = ShareId("share-1")
        val vault = VaultTestFactory.create(userId = userId, shareId = shareId, name = "Main Vault")
        val folder = FolderTestFactory.create(
            userId = userId,
            shareId = shareId,
            vaultId = vault.vaultId,
            folderId = FolderId("folder-1"),
            folderKey = "key",
            name = "Root"
        )

        featureFlags.set(FeatureFlag.PASS_FOLDERS, true)
        observeFolders.sendResult(userId, shareId, Result.success(listOf(folder)))
        observeVaultsWithItemCount.sendResult(
            Result.success(
                listOf(
                    VaultWithItemCount(
                        vault = vault,
                        activeItemCount = 2,
                        trashedItemCount = 0
                    )
                )
            )
        )

        viewModel.stateFlow.test {
            val state = awaitNextMatching {
                it.foldersEnabled && it.vaultFolders[shareId]?.firstOrNull()?.name == "Root"
            }

            assertThat(state.vaultFolders[shareId]).hasSize(1)
            assertThat(state.vaultFolders[shareId]?.first()?.name).isEqualTo("Root")
        }
    }

    @Test
    internal fun `does not re-subscribe folder flows when only vault counts change for same share`() = runTest {
        val userId = UserId("user-1")
        val shareId = ShareId("share-1")
        val vault = VaultTestFactory.create(userId = userId, shareId = shareId, name = "Main Vault")
        val folder = FolderTestFactory.create(
            userId = userId,
            shareId = shareId,
            vaultId = vault.vaultId,
            folderId = FolderId("folder-1"),
            folderKey = "key",
            name = "Root"
        )

        featureFlags.set(FeatureFlag.PASS_FOLDERS, true)
        observeFolders.sendResult(userId, shareId, Result.success(listOf(folder)))

        observeVaultsWithItemCount.sendResult(
            Result.success(
                listOf(
                    VaultWithItemCount(
                        vault = vault,
                        activeItemCount = 1,
                        trashedItemCount = 0
                    )
                )
            )
        )

        viewModel.stateFlow.test {
            awaitNextMatching { it.vaultShares.firstOrNull()?.activeItemCount == 1L && it.vaultFolders.isNotEmpty() }

            assertThat(observeFolders.invocationCount(userId, shareId)).isEqualTo(1)

            observeVaultsWithItemCount.sendResult(
                Result.success(
                    listOf(
                        VaultWithItemCount(
                            vault = vault,
                            activeItemCount = 9,
                            trashedItemCount = 0
                        )
                    )
                )
            )

            val updated = awaitNextMatching { it.vaultShares.firstOrNull()?.activeItemCount == 9L }

            assertThat(updated.vaultFolders[shareId]).hasSize(1)
            assertThat(observeFolders.invocationCount(userId, shareId)).isEqualTo(1)
        }
    }

    @Test
    internal fun `does not re-subscribe folder flows when vault order changes for same keys`() = runTest {
        val userId = UserId("user-1")
        val firstShareId = ShareId("share-1")
        val secondShareId = ShareId("share-2")
        val firstVault = VaultTestFactory.create(userId = userId, shareId = firstShareId, name = "same-name")
        val secondVault = VaultTestFactory.create(userId = userId, shareId = secondShareId, name = "same-name")

        featureFlags.set(FeatureFlag.PASS_FOLDERS, true)

        observeVaultsWithItemCount.sendResult(
            Result.success(
                listOf(
                    VaultWithItemCount(vault = firstVault, activeItemCount = 1, trashedItemCount = 0),
                    VaultWithItemCount(vault = secondVault, activeItemCount = 2, trashedItemCount = 0)
                )
            )
        )

        viewModel.stateFlow.test {
            awaitNextMatching { it.vaultShares.size == 2 }
            assertThat(observeFolders.invocationCount(userId, firstShareId)).isEqualTo(1)
            assertThat(observeFolders.invocationCount(userId, secondShareId)).isEqualTo(1)

            observeVaultsWithItemCount.sendResult(
                Result.success(
                    listOf(
                        VaultWithItemCount(vault = secondVault, activeItemCount = 3, trashedItemCount = 0),
                        VaultWithItemCount(vault = firstVault, activeItemCount = 4, trashedItemCount = 0)
                    )
                )
            )
            awaitNextMatching {
                it.vaultShares.firstOrNull()?.activeItemCount == 3L ||
                    it.vaultShares.firstOrNull()?.activeItemCount == 4L
            }

            assertThat(observeFolders.invocationCount(userId, firstShareId)).isEqualTo(1)
            assertThat(observeFolders.invocationCount(userId, secondShareId)).isEqualTo(1)
        }
    }

    @Test
    internal fun `observes duplicated share key once`() = runTest {
        val userId = UserId("user-1")
        val shareId = ShareId("share-1")
        val vault = VaultTestFactory.create(userId = userId, shareId = shareId, name = "Main Vault")

        featureFlags.set(FeatureFlag.PASS_FOLDERS, true)

        observeVaultsWithItemCount.sendResult(
            Result.success(
                listOf(
                    VaultWithItemCount(vault = vault, activeItemCount = 1, trashedItemCount = 0),
                    VaultWithItemCount(vault = vault, activeItemCount = 2, trashedItemCount = 0)
                )
            )
        )

        viewModel.stateFlow.test {
            awaitNextMatching { it.vaultShares.size == 2 }
            assertThat(observeFolders.invocationCount(userId, shareId)).isEqualTo(1)
        }
    }

    // region folder button visibility

    @Test
    internal fun `folder button hidden when PASS_FOLDERS flag is disabled`() = runTest {
        featureFlags.set(FeatureFlag.PASS_FOLDERS, false)
        canCreateFolder.sendValue(true)
        emitSingleVault()

        viewModel.stateFlow.test {
            val state = awaitNextMatching { it.vaultShares.isNotEmpty() }
            assertThat(state.foldersEnabled).isFalse()
        }
    }

    @Test
    internal fun `folder button shown for paid user when flag is enabled`() = runTest {
        featureFlags.set(FeatureFlag.PASS_FOLDERS, true)
        canCreateFolder.sendValue(true)
        observeUpgradeInfo.setResult(upgradeInfoWithUpgrade(false))
        emitSingleVault()

        viewModel.stateFlow.test {
            val state = awaitNextMatching { it.foldersEnabled && it.vaultShares.isNotEmpty() }
            assertThat(state.canCreateFolder).isTrue()
            assertThat(state.needsToUpgrade).isFalse()
        }
    }

    @Test
    internal fun `folder upsell button shown for free user when flag is enabled and upgrade available`() = runTest {
        featureFlags.set(FeatureFlag.PASS_FOLDERS, true)
        canCreateFolder.sendValue(false)
        observeUpgradeInfo.setResult(upgradeInfoWithUpgrade(true))
        emitSingleVault()

        viewModel.stateFlow.test {
            val state = awaitNextMatching { it.foldersEnabled && it.vaultShares.isNotEmpty() }
            assertThat(state.canCreateFolder).isFalse()
            assertThat(state.needsToUpgrade).isTrue()
        }
    }

    @Test
    internal fun `folder button hidden when org restricts creation and no upgrade available`() = runTest {
        featureFlags.set(FeatureFlag.PASS_FOLDERS, true)
        canCreateFolder.sendValue(false)
        observeUpgradeInfo.setResult(upgradeInfoWithUpgrade(false))
        emitSingleVault()

        viewModel.stateFlow.test {
            val state = awaitNextMatching { it.vaultShares.isNotEmpty() }
            assertThat(state.foldersEnabled).isTrue()
            assertThat(state.canCreateFolder).isFalse()
            assertThat(state.needsToUpgrade).isFalse()
        }
    }

    // endregion

    private fun emitSingleVault(userId: UserId = UserId("user-1"), shareId: ShareId = ShareId("share-1")) {
        val vault = VaultTestFactory.create(userId = userId, shareId = shareId)
        observeVaultsWithItemCount.sendResult(
            Result.success(
                listOf(VaultWithItemCount(vault = vault, activeItemCount = 0, trashedItemCount = 0))
            )
        )
    }

    private fun upgradeInfoWithUpgrade(isUpgradeAvailable: Boolean): UpgradeInfo = UpgradeInfo(
        isUpgradeAvailable = isUpgradeAvailable,
        isSubscriptionAvailable = isUpgradeAvailable,
        plan = Plan(
            planType = TestConstants.FreePlanType,
            vaultLimit = PlanLimit.Limited(1),
            aliasLimit = PlanLimit.Limited(0),
            totpLimit = PlanLimit.Limited(0),
            updatedAt = 0,
            hideUpgrade = false
        ),
        totalVaults = 1,
        totalAlias = 0,
        totalTotp = 0
    )

    private suspend fun ReceiveTurbine<HomeDrawerState>.awaitNextMatching(
        predicate: (HomeDrawerState) -> Boolean
    ): HomeDrawerState {
        while (true) {
            val state = awaitItem()
            if (predicate(state)) return state
        }
    }

}
