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

package proton.android.pass.features.itemcreate.bottomsheets.createitem

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.commonui.fakes.FakeSavedStateHandleProvider
import proton.android.pass.data.fakes.usecases.FakeCanCreateAlias
import proton.android.pass.data.fakes.usecases.FakeCanCreateItemInVault
import proton.android.pass.data.fakes.usecases.FakeCanCreateItemsInFolder
import proton.android.pass.data.fakes.usecases.FakeObserveUpgradeInfo
import proton.android.pass.data.fakes.usecases.FakeObserveVaultsWithItemCount
import proton.android.pass.data.fakes.usecases.items.FakeObserveCanCreateItems
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.searchoptions.api.VaultSelectionOption
import proton.android.pass.searchoptions.fakes.FakeHomeSearchOptionsRepository
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.VaultTestFactory

internal class CreateItemBottomSheetViewModelTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var savedStateHandleProvider: FakeSavedStateHandleProvider
    private lateinit var observeVaults: FakeObserveVaultsWithItemCount
    private lateinit var canCreateItemInVault: FakeCanCreateItemInVault
    private lateinit var homeSearchOptionsRepository: FakeHomeSearchOptionsRepository
    private lateinit var observeCanCreateItems: FakeObserveCanCreateItems
    private lateinit var observeUpgradeInfo: FakeObserveUpgradeInfo
    private lateinit var canCreateAlias: FakeCanCreateAlias
    private lateinit var canCreateItemsInFolder: FakeCanCreateItemsInFolder

    @Before
    fun setUp() {
        savedStateHandleProvider = FakeSavedStateHandleProvider()
        observeVaults = FakeObserveVaultsWithItemCount()
        canCreateItemInVault = FakeCanCreateItemInVault()
        homeSearchOptionsRepository = FakeHomeSearchOptionsRepository()
        observeCanCreateItems = FakeObserveCanCreateItems()
        observeUpgradeInfo = FakeObserveUpgradeInfo()
        canCreateAlias = FakeCanCreateAlias()
        canCreateItemsInFolder = FakeCanCreateItemsInFolder()

        savedStateHandleProvider.get().set(
            CreateItemBottomSheetModeNavArgId.key,
            CreateItemBottomSheetMode.HomeFull
        )
    }

    private fun createViewModel(): CreateItemBottomSheetViewModel = CreateItemBottomSheetViewModel(
        homeSearchOptionsRepository = homeSearchOptionsRepository,
        observeUpgradeInfo = observeUpgradeInfo,
        savedStateHandleProvider = savedStateHandleProvider,
        observeCanCreateItems = observeCanCreateItems,
        canCreateAlias = canCreateAlias,
        observeVaultsWithItemCount = observeVaults,
        canCreateItemInVault = canCreateItemInVault,
        canCreateItemsInFolder = canCreateItemsInFolder
    )

    private fun vaultWithItemCount(shareId: ShareId, role: ShareRole = ShareRole.Admin): VaultWithItemCount =
        VaultWithItemCount(
            vault = VaultTestFactory.create(shareId = shareId, role = role),
            activeItemCount = 0,
            trashedItemCount = 0
        )

    @Test
    fun `shareId is set when HomeFull mode and current vault selection is writable`() = runTest {
        val shareId = ShareId("vault1")
        homeSearchOptionsRepository.setVaultSelectionOption(VaultSelectionOption.Vault(shareId))
        val vm = createViewModel()
        val job = vm.stateFlow.launchIn(this)

        observeVaults.sendResult(Result.success(listOf(vaultWithItemCount(shareId, ShareRole.Admin))))
        observeCanCreateItems.emit(true)
        advanceUntilIdle()

        assertThat(vm.stateFlow.value.shareId).isEqualTo(shareId)
        assertThat(vm.stateFlow.value.folderId).isNull()
        job.cancel()
    }

    @Test
    fun `shareId is null when HomeFull mode and current vault selection is read-only`() = runTest {
        val shareId = ShareId("vault1")
        homeSearchOptionsRepository.setVaultSelectionOption(VaultSelectionOption.Vault(shareId))
        canCreateItemInVault.setResult(shareId, false)
        val vm = createViewModel()
        val job = vm.stateFlow.launchIn(this)

        observeVaults.sendResult(Result.success(listOf(vaultWithItemCount(shareId, ShareRole.Read))))
        observeCanCreateItems.emit(true)
        advanceUntilIdle()

        assertThat(vm.stateFlow.value.shareId).isNull()
        assertThat(vm.stateFlow.value.folderId).isNull()
        job.cancel()
    }

    @Test
    fun `shareId and folderId set when HomeFull mode and current folder selection is writable`() = runTest {
        val shareId = ShareId("vault1")
        val folderId = FolderId("folder1")
        homeSearchOptionsRepository.setVaultSelectionOption(VaultSelectionOption.Folder(shareId, folderId))
        canCreateItemsInFolder.sendValue(true)
        val vm = createViewModel()
        val job = vm.stateFlow.launchIn(this)

        observeVaults.sendResult(Result.success(listOf(vaultWithItemCount(shareId, ShareRole.Admin))))
        observeCanCreateItems.emit(true)
        advanceUntilIdle()

        assertThat(vm.stateFlow.value.shareId).isEqualTo(shareId)
        assertThat(vm.stateFlow.value.folderId).isEqualTo(folderId)
        job.cancel()
    }

    @Test
    fun `folderId is null when HomeFull mode and user cannot create items in folder`() = runTest {
        val shareId = ShareId("vault1")
        val folderId = FolderId("folder1")
        homeSearchOptionsRepository.setVaultSelectionOption(VaultSelectionOption.Folder(shareId, folderId))
        canCreateItemsInFolder.sendValue(false)
        val vm = createViewModel()
        val job = vm.stateFlow.launchIn(this)

        observeVaults.sendResult(Result.success(listOf(vaultWithItemCount(shareId, ShareRole.Admin))))
        observeCanCreateItems.emit(true)
        advanceUntilIdle()

        assertThat(vm.stateFlow.value.shareId).isEqualTo(shareId)
        assertThat(vm.stateFlow.value.folderId).isNull()
        job.cancel()
    }

    @Test
    fun `shareId and folderId are null when HomeFull mode and folder is in read-only vault`() = runTest {
        val shareId = ShareId("vault1")
        val folderId = FolderId("folder1")
        homeSearchOptionsRepository.setVaultSelectionOption(VaultSelectionOption.Folder(shareId, folderId))
        canCreateItemInVault.setResult(shareId, false)
        val vm = createViewModel()
        val job = vm.stateFlow.launchIn(this)

        observeVaults.sendResult(Result.success(listOf(vaultWithItemCount(shareId, ShareRole.Read))))
        observeCanCreateItems.emit(true)
        advanceUntilIdle()

        assertThat(vm.stateFlow.value.shareId).isNull()
        assertThat(vm.stateFlow.value.folderId).isNull()
        job.cancel()
    }

    @Test
    fun `nav shareId takes precedence over current vault selection`() = runTest {
        val navShareId = ShareId("nav-vault")
        val selectedShareId = ShareId("selected-vault")
        savedStateHandleProvider.get().set(CommonOptionalNavArgId.ShareId.key, navShareId.id)
        homeSearchOptionsRepository.setVaultSelectionOption(VaultSelectionOption.Vault(selectedShareId))
        val vm = createViewModel()
        val job = vm.stateFlow.launchIn(this)

        observeVaults.sendResult(Result.success(listOf(vaultWithItemCount(selectedShareId))))
        observeCanCreateItems.emit(true)
        advanceUntilIdle()

        assertThat(vm.stateFlow.value.shareId).isEqualTo(navShareId)
        job.cancel()
    }

    @Test
    fun `shareId is null when HomeFull mode and AllVaults is selected`() = runTest {
        homeSearchOptionsRepository.setVaultSelectionOption(VaultSelectionOption.AllVaults)
        val vm = createViewModel()
        val job = vm.stateFlow.launchIn(this)

        observeVaults.sendResult(Result.success(listOf(vaultWithItemCount(ShareId("vault1")))))
        observeCanCreateItems.emit(true)
        advanceUntilIdle()

        assertThat(vm.stateFlow.value.shareId).isNull()
        job.cancel()
    }
}
