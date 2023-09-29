/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.featurevault.impl.bottomsheet.options

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.commonui.fakes.TestSavedStateHandleProvider
import proton.android.pass.data.api.usecases.capabilities.VaultAccessData
import proton.android.pass.data.fakes.usecases.TestCanManageVaultAccess
import proton.android.pass.data.fakes.usecases.TestCanMigrateVault
import proton.android.pass.data.fakes.usecases.TestCanShareVault
import proton.android.pass.data.fakes.usecases.TestObserveVaults
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.fakes.TestSnackbarDispatcher
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.TestFeatureFlagsPreferenceRepository
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.TestUtils
import proton.pass.domain.ShareId
import proton.pass.domain.Vault

class VaultOptionsViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var instance: VaultOptionsViewModel

    private lateinit var snackbarDispatcher: TestSnackbarDispatcher
    private lateinit var canMigrateVault: TestCanMigrateVault
    private lateinit var canShareVault: TestCanShareVault
    private lateinit var canManageVaultAccess: TestCanManageVaultAccess
    private lateinit var observeVaults: TestObserveVaults
    private lateinit var ffRepo: TestFeatureFlagsPreferenceRepository

    @Before
    fun setup() {
        snackbarDispatcher = TestSnackbarDispatcher()
        canShareVault = TestCanShareVault()
        canMigrateVault = TestCanMigrateVault()
        canManageVaultAccess = TestCanManageVaultAccess()
        observeVaults = TestObserveVaults()
        ffRepo = TestFeatureFlagsPreferenceRepository()
        setNavShareId(ShareId(SHARE_ID))
    }

    @Test
    fun `can edit vault if owner value set to true`() = runTest {
        testCanEdit(true)
    }

    @Test
    fun `cannot edit vault if owner set to false`() = runTest {
        testCanEdit(false)
    }

    private suspend fun testCanEdit(expected: Boolean) {
        emitDefaultVault(owned = expected)
        instance.state.test {
            val item = awaitItem()
            assertThat(item).isInstanceOf(VaultOptionsUiState.Success::class.java)

            val casted = item as VaultOptionsUiState.Success
            assertThat(casted.showEdit).isEqualTo(expected)
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
        emitDefaultVault(primary = true)
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

    // RemovePrimaryVault tests
    @Test
    fun `if RemovePrimaryVault enabled cannot remove last owned vault if primary`() = runTest {
        ffRepo.set(FeatureFlag.REMOVE_PRIMARY_VAULT, true)
        val ownedVault = vaultWith(isPrimary = false, owned = true)
        val vaults = listOf(
            vaultWith(isPrimary = false, owned = false),
            ownedVault,
            vaultWith(isPrimary = false, owned = false),
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
        ffRepo.set(FeatureFlag.REMOVE_PRIMARY_VAULT, true)
        val ownedVault = vaultWith(isPrimary = true, owned = true)
        val vaults = listOf(
            vaultWith(isPrimary = false, owned = false),
            ownedVault,
            vaultWith(isPrimary = false, owned = false),
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
        primary: Boolean = true,
        owned: Boolean = true,
        shared: Boolean = true
    ): Vault {
        val defaultVault = Vault(
            shareId = ShareId(SHARE_ID),
            name = "Test vault",
            isPrimary = primary,
            isOwned = owned,
            members = if (shared) 2 else 1,
            shared = shared
        )

        observeVaults.sendResult(Result.success(listOf(defaultVault)))
        return defaultVault
    }

    private fun vaultWith(isPrimary: Boolean, owned: Boolean): Vault = Vault(
        shareId = ShareId("ShareId-${TestUtils.randomString()}"),
        name = "Some vault",
        isPrimary = isPrimary,
        isOwned = owned,
        members = 1,
        shared = false
    )

    private fun setNavShareId(shareId: ShareId) {
        instance = VaultOptionsViewModel(
            snackbarDispatcher = snackbarDispatcher,
            savedStateHandle = TestSavedStateHandleProvider().apply {
                get()[CommonNavArgId.ShareId.key] = shareId.id
            },
            canMigrateVault = canMigrateVault,
            observeVaults = observeVaults,
            canShareVault = canShareVault,
            canManageVaultAccess = canManageVaultAccess,
            featureFlagRepository = ffRepo
        )
    }

    companion object {
        private const val SHARE_ID = "VaultOptionsViewModelTest-ShareId"
    }
}
