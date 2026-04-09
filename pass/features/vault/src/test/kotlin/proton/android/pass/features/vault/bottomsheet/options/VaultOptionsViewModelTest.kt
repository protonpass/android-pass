/*
 * Copyright (c) 2023-2026 Proton AG
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

package proton.android.pass.features.vault.bottomsheet.options

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.commonui.fakes.FakeSavedStateHandleProvider
import proton.android.pass.data.api.usecases.UpgradeInfo
import proton.android.pass.data.api.usecases.capabilities.VaultAccessData
import proton.android.pass.data.fakes.usecases.FakeCanCreateFolder
import proton.android.pass.data.fakes.usecases.FakeCanManageVaultAccess
import proton.android.pass.data.fakes.usecases.FakeCanMigrateVault
import proton.android.pass.data.fakes.usecases.FakeCanShareShare
import proton.android.pass.data.fakes.usecases.FakeObserveEncryptedItems
import proton.android.pass.data.fakes.usecases.FakeObserveUpgradeInfo
import proton.android.pass.data.fakes.usecases.FakeObserveVaults
import proton.android.pass.domain.GroupId
import proton.android.pass.domain.Plan
import proton.android.pass.domain.ShareRole
import proton.android.pass.domain.PlanLimit
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.fakes.FakeSnackbarDispatcher
import proton.android.pass.preferences.FakeFeatureFlagsPreferenceRepository
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.StringTestFactory
import proton.android.pass.test.TestConstants
import proton.android.pass.test.domain.VaultTestFactory

class VaultOptionsViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var instance: VaultOptionsViewModel

    private lateinit var snackbarDispatcher: FakeSnackbarDispatcher
    private lateinit var canMigrateVault: FakeCanMigrateVault
    private lateinit var canShareVault: FakeCanShareShare
    private lateinit var canManageVaultAccess: FakeCanManageVaultAccess
    private lateinit var canCreateFolder: FakeCanCreateFolder
    private lateinit var observeUpgradeInfo: FakeObserveUpgradeInfo
    private lateinit var observeVaults: FakeObserveVaults

    @Before
    fun setup() {
        snackbarDispatcher = FakeSnackbarDispatcher()
        canShareVault = FakeCanShareShare()
        canMigrateVault = FakeCanMigrateVault()
        canManageVaultAccess = FakeCanManageVaultAccess()
        canCreateFolder = FakeCanCreateFolder()
        observeUpgradeInfo = FakeObserveUpgradeInfo()
        observeVaults = FakeObserveVaults()
        setNavShareId(ShareId(SHARE_ID))
    }

    @Test
    fun `can edit vault if admin role`() = runTest {
        emitDefaultVault(role = ShareRole.Admin)
        instance.state.test {
            val item = awaitItem() as VaultOptionsUiState.Success
            assertThat(item.showEdit).isTrue()
        }
    }

    @Test
    fun `can edit vault if write role`() = runTest {
        emitDefaultVault(owned = false, role = ShareRole.Write)
        instance.state.test {
            val item = awaitItem() as VaultOptionsUiState.Success
            assertThat(item.showEdit).isTrue()
        }
    }

    @Test
    fun `cannot edit vault if read role`() = runTest {
        emitDefaultVault(owned = false, role = ShareRole.Read)
        instance.state.test {
            val item = awaitItem() as VaultOptionsUiState.Success
            assertThat(item.showEdit).isFalse()
        }
    }

    @Test
    fun `respects canMigrate value set to true`() = runTest {
        testCanMigrate(true)
    }

    @Test
    fun `respects canMigrate value set to false`() = runTest {
        testCanMigrate(false)
    }

    private suspend fun testCanMigrate(expected: Boolean) {
        emitDefaultVault()
        canMigrateVault.setResult(expected)
        instance.state.test {
            val item = awaitItem()
            assertThat(item).isInstanceOf(VaultOptionsUiState.Success::class.java)

            val casted = item as VaultOptionsUiState.Success
            assertThat(casted.showMigrate).isEqualTo(expected)
        }
    }

    @Test
    fun `does not show canManageVaultAccess when can manage but vault not shared`() = runTest {
        emitDefaultVault(shared = false)
        canManageVaultAccess.setResult(
            VaultAccessData(
                canManageAccess = true,
                canViewMembers = false
            )
        )
        instance.state.test {
            val item = awaitItem()
            assertThat(item).isInstanceOf(VaultOptionsUiState.Success::class.java)

            val casted = item as VaultOptionsUiState.Success
            assertThat(casted.showManageAccess).isFalse()
        }
    }

    @Test
    fun `shows canManageVaultAccess when can manage and vault shared`() = runTest {
        emitDefaultVault(shared = true)
        canManageVaultAccess.setResult(
            VaultAccessData(
                canManageAccess = true,
                canViewMembers = false
            )
        )
        instance.state.test {
            val item = awaitItem()
            assertThat(item).isInstanceOf(VaultOptionsUiState.Success::class.java)

            val casted = item as VaultOptionsUiState.Success
            assertThat(casted.showManageAccess).isTrue()
        }
    }

    @Test
    fun `does not show canManageVaultAccess when canopt manage and vault shared`() = runTest {
        emitDefaultVault(shared = true)
        canManageVaultAccess.setResult(
            VaultAccessData(
                canManageAccess = false,
                canViewMembers = false
            )
        )
        instance.state.test {
            val item = awaitItem()
            assertThat(item).isInstanceOf(VaultOptionsUiState.Success::class.java)

            val casted = item as VaultOptionsUiState.Success
            assertThat(casted.showManageAccess).isFalse()
        }
    }

    @Test
    fun `does not show canManageVaultAccess when cannot manage and vault not shared`() = runTest {
        emitDefaultVault(shared = false)
        canManageVaultAccess.setResult(
            VaultAccessData(
                canManageAccess = false,
                canViewMembers = false
            )
        )
        instance.state.test {
            val item = awaitItem()
            assertThat(item).isInstanceOf(VaultOptionsUiState.Success::class.java)

            val casted = item as VaultOptionsUiState.Success
            assertThat(casted.showManageAccess).isFalse()
        }
    }


    @Test
    fun `does not show canViewMembers when can view but vault not shared`() = runTest {
        emitDefaultVault(shared = false)
        canManageVaultAccess.setResult(
            VaultAccessData(
                canManageAccess = false,
                canViewMembers = true
            )
        )
        instance.state.test {
            val item = awaitItem()
            assertThat(item).isInstanceOf(VaultOptionsUiState.Success::class.java)

            val casted = item as VaultOptionsUiState.Success
            assertThat(casted.showViewMembers).isFalse()
        }
    }

    @Test
    fun `shows canViewMembers when can view and vault shared`() = runTest {
        emitDefaultVault(shared = true)
        canManageVaultAccess.setResult(
            VaultAccessData(
                canManageAccess = false,
                canViewMembers = true
            )
        )
        instance.state.test {
            val item = awaitItem()
            assertThat(item).isInstanceOf(VaultOptionsUiState.Success::class.java)

            val casted = item as VaultOptionsUiState.Success
            assertThat(casted.showViewMembers).isTrue()
        }
    }

    @Test
    fun `does not show canViewMembers when cannot view and vault shared`() = runTest {
        emitDefaultVault(shared = true)
        canManageVaultAccess.setResult(
            VaultAccessData(
                canManageAccess = false,
                canViewMembers = false
            )
        )
        instance.state.test {
            val item = awaitItem()
            assertThat(item).isInstanceOf(VaultOptionsUiState.Success::class.java)

            val casted = item as VaultOptionsUiState.Success
            assertThat(casted.showViewMembers).isFalse()
        }
    }

    @Test
    fun `does not show canViewMembers when cannot view and vault not shared`() = runTest {
        emitDefaultVault(shared = false)
        canManageVaultAccess.setResult(
            VaultAccessData(
                canManageAccess = false,
                canViewMembers = false
            )
        )
        instance.state.test {
            val item = awaitItem()
            assertThat(item).isInstanceOf(VaultOptionsUiState.Success::class.java)

            val casted = item as VaultOptionsUiState.Success
            assertThat(casted.showViewMembers).isFalse()
        }
    }

    @Test
    fun `cannot delete primary vault`() = runTest {
        emitDefaultVault()
        instance.state.test {
            val item = awaitItem()
            assertThat(item).isInstanceOf(VaultOptionsUiState.Success::class.java)

            val casted = item as VaultOptionsUiState.Success
            assertThat(casted.showDelete).isFalse()
        }
    }

    @Test
    fun `cannot leave owned vault`() = runTest {
        emitDefaultVault(owned = true)
        instance.state.test {
            val item = awaitItem()
            assertThat(item).isInstanceOf(VaultOptionsUiState.Success::class.java)

            val casted = item as VaultOptionsUiState.Success
            assertThat(casted.showLeave).isFalse()
        }
    }

    @Test
    fun `can leave non-owned regular vault`() = runTest {
        emitDefaultVault(owned = false)
        instance.state.test {
            val item = awaitItem()
            assertThat(item).isInstanceOf(VaultOptionsUiState.Success::class.java)

            val casted = item as VaultOptionsUiState.Success
            assertThat(casted.showLeave).isTrue()
        }
    }

    @Test
    fun `cannot leave owned group share vault`() = runTest {
        emitDefaultVault(owned = true, groupId = GroupId("group-1"))
        instance.state.test {
            val item = awaitItem()
            assertThat(item).isInstanceOf(VaultOptionsUiState.Success::class.java)

            val casted = item as VaultOptionsUiState.Success
            assertThat(casted.showLeave).isFalse()
        }
    }

    @Test
    fun `cannot leave non-owned group share vault`() = runTest {
        emitDefaultVault(owned = false, groupId = GroupId("group-1"))
        instance.state.test {
            val item = awaitItem()
            assertThat(item).isInstanceOf(VaultOptionsUiState.Success::class.java)

            val casted = item as VaultOptionsUiState.Success
            assertThat(casted.showLeave).isFalse()
        }
    }

    // RemovePrimaryVault tests
    @Test
    fun `if RemovePrimaryVault enabled cannot remove last owned vault if primary`() = runTest {
        val ownedVault = vaultWith(owned = true)
        val vaults = listOf(
            vaultWith(owned = false),
            ownedVault,
            vaultWith(owned = false)
        )
        setNavShareId(ownedVault.shareId)
        observeVaults.sendResult(Result.success(vaults))

        instance.state.test {
            val item = awaitItem()
            assertThat(item).isInstanceOf(VaultOptionsUiState.Success::class.java)

            val casted = item as VaultOptionsUiState.Success
            assertThat(casted.showDelete).isFalse()
        }
    }

    @Test
    fun `if RemovePrimaryVault enabled cannot remove last owned vault if not primary`() = runTest {
        val ownedVault = vaultWith(owned = true)
        val vaults = listOf(
            vaultWith(owned = false),
            ownedVault,
            vaultWith(owned = false)
        )
        setNavShareId(ownedVault.shareId)
        observeVaults.sendResult(Result.success(vaults))

        instance.state.test {
            val item = awaitItem()
            assertThat(item).isInstanceOf(VaultOptionsUiState.Success::class.java)

            val casted = item as VaultOptionsUiState.Success
            assertThat(casted.showDelete).isFalse()
        }
    }

    private fun emitDefaultVault(
        owned: Boolean = true,
        shared: Boolean = true,
        groupId: GroupId? = null,
        role: ShareRole = ShareRole.Admin
    ): Vault {
        val defaultVault = VaultTestFactory.create(
            shareId = ShareId(SHARE_ID),
            isOwned = owned,
            groupId = groupId,
            shared = shared,
            members = if (shared) 2 else 1,
            role = role
        )

        observeVaults.sendResult(Result.success(listOf(defaultVault)))
        return defaultVault
    }

    private fun vaultWith(owned: Boolean): Vault =
        VaultTestFactory.create(shareId = ShareId("ShareId-${StringTestFactory.randomString()}"), isOwned = owned)

    // region canAddFolder / canAddFolderNeedsUpgrade

    @Test
    fun `canAddFolder is false when PASS_FOLDERS flag is disabled`() = runTest {
        val featureFlags = FakeFeatureFlagsPreferenceRepository().apply {
            set(FeatureFlag.PASS_FOLDERS, false)
        }
        canCreateFolder.sendValue(true)
        setNavShareId(ShareId(SHARE_ID), featureFlags)
        emitDefaultVault()

        instance.state.test {
            val item = awaitItem() as VaultOptionsUiState.Success
            assertThat(item.canAddFolder).isFalse()
            assertThat(item.canAddFolderNeedsUpgrade).isFalse()
        }
    }

    @Test
    fun `canAddFolder is true and no upgrade needed for paid user when flag is enabled`() = runTest {
        val featureFlags = FakeFeatureFlagsPreferenceRepository().apply {
            set(FeatureFlag.PASS_FOLDERS, true)
        }
        canCreateFolder.sendValue(true)
        observeUpgradeInfo.setResult(upgradeInfoWithUpgrade(false))
        setNavShareId(ShareId(SHARE_ID), featureFlags)
        emitDefaultVault()

        instance.state.test {
            val item = awaitItem() as VaultOptionsUiState.Success
            assertThat(item.canAddFolder).isTrue()
            assertThat(item.canAddFolderNeedsUpgrade).isFalse()
        }
    }

    @Test
    fun `canAddFolder shows upsell for free user when flag is enabled and upgrade available`() = runTest {
        val featureFlags = FakeFeatureFlagsPreferenceRepository().apply {
            set(FeatureFlag.PASS_FOLDERS, true)
        }
        canCreateFolder.sendValue(false)
        observeUpgradeInfo.setResult(upgradeInfoWithUpgrade(true))
        setNavShareId(ShareId(SHARE_ID), featureFlags)
        emitDefaultVault()

        instance.state.test {
            val item = awaitItem() as VaultOptionsUiState.Success
            assertThat(item.canAddFolder).isTrue()
            assertThat(item.canAddFolderNeedsUpgrade).isTrue()
        }
    }

    @Test
    fun `canAddFolder hidden when org restricts folder creation and no upgrade available`() = runTest {
        val featureFlags = FakeFeatureFlagsPreferenceRepository().apply {
            set(FeatureFlag.PASS_FOLDERS, true)
        }
        canCreateFolder.sendValue(false)
        observeUpgradeInfo.setResult(upgradeInfoWithUpgrade(false))
        setNavShareId(ShareId(SHARE_ID), featureFlags)
        emitDefaultVault()

        instance.state.test {
            val item = awaitItem() as VaultOptionsUiState.Success
            assertThat(item.canAddFolder).isFalse()
            assertThat(item.canAddFolderNeedsUpgrade).isFalse()
        }
    }

    // endregion

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

    private fun setNavShareId(
        shareId: ShareId,
        featureFlags: FakeFeatureFlagsPreferenceRepository = FakeFeatureFlagsPreferenceRepository()
    ) {
        instance = VaultOptionsViewModel(
            snackbarDispatcher = snackbarDispatcher,
            savedStateHandle = FakeSavedStateHandleProvider().apply {
                get()[CommonNavArgId.ShareId.key] = shareId.id
            },
            canMigrateVault = canMigrateVault,
            observeVaults = observeVaults,
            canShareShare = canShareVault,
            canManageVaultAccess = canManageVaultAccess,
            canCreateFolder = canCreateFolder,
            observeUpgradeInfo = observeUpgradeInfo,
            observeEncryptedItems = FakeObserveEncryptedItems(),
            preferencesRepository = featureFlags
        )
    }

    companion object {
        private const val SHARE_ID = "VaultOptionsViewModelTest-ShareId"
    }
}
