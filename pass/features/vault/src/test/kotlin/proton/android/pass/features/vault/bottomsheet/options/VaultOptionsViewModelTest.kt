/*
 * Copyright (c) 2023-2024 Proton AG
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
import proton.android.pass.commonui.fakes.TestSavedStateHandleProvider
import proton.android.pass.data.api.usecases.capabilities.VaultAccessData
import proton.android.pass.data.fakes.usecases.FakeObserveEncryptedItems
import proton.android.pass.data.fakes.usecases.TestCanManageVaultAccess
import proton.android.pass.data.fakes.usecases.TestCanMigrateVault
import proton.android.pass.data.fakes.usecases.TestCanShareShare
import proton.android.pass.data.fakes.usecases.TestObserveVaults
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.fakes.TestSnackbarDispatcher
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.TestUtils
import proton.android.pass.test.domain.TestVault

class VaultOptionsViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var instance: VaultOptionsViewModel

    private lateinit var snackbarDispatcher: TestSnackbarDispatcher
    private lateinit var canMigrateVault: TestCanMigrateVault
    private lateinit var canShareVault: TestCanShareShare
    private lateinit var canManageVaultAccess: TestCanManageVaultAccess
    private lateinit var observeVaults: TestObserveVaults

    @Before
    fun setup() {
        snackbarDispatcher = TestSnackbarDispatcher()
        canShareVault = TestCanShareShare()
        canMigrateVault = TestCanMigrateVault()
        canManageVaultAccess = TestCanManageVaultAccess()
        observeVaults = TestObserveVaults()
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
                canViewMembers = false,
                canCreateSecureLink = false
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
                canViewMembers = false,
                canCreateSecureLink = false
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
                canViewMembers = false,
                canCreateSecureLink = false
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
                canViewMembers = false,
                canCreateSecureLink = false
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
                canViewMembers = true,
                canCreateSecureLink = false
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
                canViewMembers = true,
                canCreateSecureLink = false
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
                canViewMembers = false,
                canCreateSecureLink = false
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
                canViewMembers = false,
                canCreateSecureLink = false
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

    private fun emitDefaultVault(owned: Boolean = true, shared: Boolean = true): Vault {
        val defaultVault = TestVault.create(
            shareId = ShareId(SHARE_ID),
            isOwned = owned,
            shared = shared,
            members = if (shared) 2 else 1
        )

        observeVaults.sendResult(Result.success(listOf(defaultVault)))
        return defaultVault
    }

    private fun vaultWith(owned: Boolean): Vault =
        TestVault.create(shareId = ShareId("ShareId-${TestUtils.randomString()}"), isOwned = owned)

    private fun setNavShareId(shareId: ShareId) {
        instance = VaultOptionsViewModel(
            snackbarDispatcher = snackbarDispatcher,
            savedStateHandle = TestSavedStateHandleProvider().apply {
                get()[CommonNavArgId.ShareId.key] = shareId.id
            },
            canMigrateVault = canMigrateVault,
            observeVaults = observeVaults,
            canShareShare = canShareVault,
            canManageVaultAccess = canManageVaultAccess,
            observeEncryptedItems = FakeObserveEncryptedItems()
        )
    }

    companion object {
        private const val SHARE_ID = "VaultOptionsViewModelTest-ShareId"
    }
}
