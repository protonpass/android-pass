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

package proton.android.pass.features.migrate.confirmvault

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.fakes.repositories.FakeBulkMoveToVaultRepository
import proton.android.pass.data.fakes.usecases.FakeMigrateItems
import proton.android.pass.data.fakes.usecases.FakeMigrateVault
import proton.android.pass.data.fakes.usecases.FakeObserveVaultsWithItemCount
import proton.android.pass.data.fakes.usecases.folders.FakeDissolveFolder
import proton.android.pass.data.fakes.usecases.folders.FakeMoveAllItemsInFolder
import proton.android.pass.data.fakes.usecases.folders.FakeMoveFolder
import proton.android.pass.data.fakes.usecases.folders.FakeMoveItemsInsideShare
import proton.android.pass.data.fakes.usecases.folders.FakeObserveFoldersByParentId
import proton.android.pass.data.fakes.usecases.items.FakeGetMigrationItemsSelection
import proton.android.pass.data.fakes.usecases.securelink.FakeObserveHasAssociatedSecureLinks
import proton.android.pass.data.fakes.usecases.shares.FakeObserveShare
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole
import proton.android.pass.test.domain.FolderTestFactory
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.features.migrate.MigrateModeArg
import proton.android.pass.features.migrate.MigrateModeValue
import proton.android.pass.features.migrate.MigrateVaultFilter
import proton.android.pass.features.migrate.MigrateVaultFilterArg
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.notifications.fakes.FakeSnackbarDispatcher
import proton.android.pass.preferences.FakeInternalSettingsRepository
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.SavedStateHandleTestFactory
import proton.android.pass.test.domain.VaultTestFactory

internal class MigrateConfirmVaultViewModelTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var instance: MigrateConfirmVaultViewModel
    private lateinit var migrateItem: FakeMigrateItems
    private lateinit var migrateVault: FakeMigrateVault
    private lateinit var observeVaults: FakeObserveVaultsWithItemCount
    private lateinit var snackbarDispatcher: FakeSnackbarDispatcher
    private lateinit var bulkMoveToVaultRepository: FakeBulkMoveToVaultRepository
    private lateinit var observeHasAssociatedSecureLinks: FakeObserveHasAssociatedSecureLinks
    private lateinit var observeShare: FakeObserveShare
    private lateinit var settingsRepository: FakeInternalSettingsRepository

    @Before
    fun setup() {
        migrateItem = FakeMigrateItems()
        migrateVault = FakeMigrateVault()
        observeVaults = FakeObserveVaultsWithItemCount()
        snackbarDispatcher = FakeSnackbarDispatcher()
        bulkMoveToVaultRepository = FakeBulkMoveToVaultRepository()
        observeHasAssociatedSecureLinks = FakeObserveHasAssociatedSecureLinks()
        observeShare = FakeObserveShare()
        settingsRepository = FakeInternalSettingsRepository()

        instance = buildViewModel(MigrateModeValue.SelectedItems) {
            set(MigrateVaultFilterArg.key, MigrateVaultFilter.All.name)
            set(CommonOptionalNavArgId.ItemId.key, ITEM_ID.id)
        }
    }

    @Test
    fun `emits vault list once vaults are loaded`() = runTest {
        val vault = sourceVault()
        observeVaults.sendResult(Result.success(listOf(vault)))
        instance.state.test {
            val state = awaitItem()
            assertThat(state.isLoading).isInstanceOf(IsLoadingState.NotLoading::class.java)
            assertThat(state.vaultList).isNotEmpty()
        }
    }

    @Test
    fun `emits close if cancel is clicked`() = runTest {
        observeVaults.sendResult(Result.success(listOf(sourceVault())))
        instance.onCancel()
        instance.state.test {
            val state = awaitItem()
            val eventCasted = state.event as Some<ConfirmMigrateEvent>
            assertThat(eventCasted.value).isInstanceOf(ConfirmMigrateEvent.Close::class.java)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `vault selection updates selectedShareId in state`() = runTest {
        val (sourceVault, otherVault) = initialVaults()
        observeVaults.sendResult(Result.success(listOf(sourceVault, otherVault)))

        instance.onVaultSelected(otherVault.vault.shareId)
        instance.state.test {
            val state = awaitItem()
            assertThat(state.selectedShareId).isEqualTo(Some(otherVault.vault.shareId))
        }
    }

    // MigrateAllItems — folder selection

    @Test
    fun `MigrateAllItems - folder selection stores folderId in selectedFolderId`() = runTest {
        instance = buildViewModel(MigrateModeValue.AllVaultItems)
        val (sourceVault, otherVault) = initialVaults()
        observeVaults.sendResult(Result.success(listOf(sourceVault, otherVault)))

        instance.onFolderSelected(SHARE_ID, FOLDER_ID)

        instance.state.test {
            val state = awaitItem()
            assertThat(state.selectedFolderId).isEqualTo(Some(FOLDER_ID))
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `MigrateAllItems - folder selection also stores the shareId in selectedShareId`() = runTest {
        instance = buildViewModel(MigrateModeValue.AllVaultItems)
        val (sourceVault, otherVault) = initialVaults()
        observeVaults.sendResult(Result.success(listOf(sourceVault, otherVault)))

        instance.onFolderSelected(OTHER_SHARE_ID, FOLDER_ID)

        instance.state.test {
            val state = awaitItem()
            assertThat(state.selectedShareId).isEqualTo(Some(OTHER_SHARE_ID))
            cancelAndConsumeRemainingEvents()
        }
    }

    // MigrateAllItems — isSameVaultMove

    @Test
    fun `MigrateAllItems - isSameVaultMove is true when folder is in the source vault`() = runTest {
        instance = buildViewModel(MigrateModeValue.AllVaultItems)
        observeVaults.sendResult(Result.success(listOf(sourceVault())))

        instance.onFolderSelected(SHARE_ID, FOLDER_ID)

        instance.state.test {
            val state = awaitItem()
            assertThat(state.isSameVaultMove).isTrue()
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `MigrateAllItems - isSameVaultMove is false when destination is a different vault`() = runTest {
        instance = buildViewModel(MigrateModeValue.AllVaultItems)
        val (sourceVault, otherVault) = initialVaults()
        observeVaults.sendResult(Result.success(listOf(sourceVault, otherVault)))

        instance.onVaultSelected(OTHER_SHARE_ID)

        instance.state.test {
            val state = awaitItem()
            assertThat(state.isSameVaultMove).isFalse()
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `MigrateAllItems - isSameVaultMove is false when no destination is selected`() = runTest {
        instance = buildViewModel(MigrateModeValue.AllVaultItems)
        observeVaults.sendResult(Result.success(listOf(sourceVault())))

        instance.state.test {
            val state = awaitItem()
            assertThat(state.isSameVaultMove).isFalse()
            cancelAndConsumeRemainingEvents()
        }
    }

    // MigrateAllItems — onConfirm

    @Test
    fun `MigrateAllItems - confirm with folder passes destFolderId to migrator`() = runTest {
        instance = buildViewModel(MigrateModeValue.AllVaultItems)
        observeVaults.sendResult(Result.success(listOf(sourceVault())))

        instance.onFolderSelected(SHARE_ID, FOLDER_ID)
        instance.onConfirm()

        val memory = migrateVault.memory()
        assertThat(memory).hasSize(1)
        assertThat(memory.first().destFolderId).isEqualTo(FOLDER_ID)
    }

    @Test
    fun `MigrateAllItems - confirm with different vault passes null destFolderId to migrator`() = runTest {
        instance = buildViewModel(MigrateModeValue.AllVaultItems)
        val (sourceVault, otherVault) = initialVaults()
        observeVaults.sendResult(Result.success(listOf(sourceVault, otherVault)))

        instance.onVaultSelected(OTHER_SHARE_ID)
        instance.onConfirm()

        val memory = migrateVault.memory()
        assertThat(memory).hasSize(1)
        assertThat(memory.first().destFolderId).isNull()
        assertThat(memory.first().destination).isEqualTo(OTHER_SHARE_ID)
    }

    @Test
    fun `MigrateAllItems - confirm with folder passes source shareId as origin`() = runTest {
        instance = buildViewModel(MigrateModeValue.AllVaultItems)
        observeVaults.sendResult(Result.success(listOf(sourceVault())))

        instance.onFolderSelected(SHARE_ID, FOLDER_ID)
        instance.onConfirm()

        val memory = migrateVault.memory()
        assertThat(memory).hasSize(1)
        assertThat(memory.first().origin).isEqualTo(SHARE_ID)
        assertThat(memory.first().destination).isEqualTo(SHARE_ID)
    }

    // MigrateAllItems — selectedFolderId cleared when switching to vault-only selection

    @Test
    fun `MigrateAllItems - selecting vault after folder clears folderId`() = runTest {
        instance = buildViewModel(MigrateModeValue.AllVaultItems)
        val (sourceVault, otherVault) = initialVaults()
        observeVaults.sendResult(Result.success(listOf(sourceVault, otherVault)))

        instance.onFolderSelected(SHARE_ID, FOLDER_ID)
        instance.onVaultSelected(OTHER_SHARE_ID)

        instance.state.test {
            val state = awaitItem()
            assertThat(state.selectedFolderId).isEqualTo(None)
            cancelAndConsumeRemainingEvents()
        }
    }

    // MigrateAllItems — source vault enabled when it has folders (regardless of item count in them)

    @Test
    fun `MigrateAllItems - source vault root is disabled but folders are exposed when it has folders`() = runTest {
        val fakeFolders = FakeObserveFoldersByParentId().apply {
            sendResult(Result.success(listOf(FolderTestFactory.create(shareId = SHARE_ID, folderId = FOLDER_ID))))
        }
        instance = buildViewModel(MigrateModeValue.AllVaultItems, fakeFolders)
        observeVaults.sendResult(Result.success(listOf(sourceVault())))

        instance.state.test {
            val state = awaitItem()
            val sourceVaultState = state.vaultList.first { it.vaultWithItemCount.vault.shareId == SHARE_ID }
            assertThat(sourceVaultState.status)
                .isEqualTo(VaultStatus.Disabled(VaultStatus.DisabledReason.SameVault))
            assertThat(sourceVaultState.folderTree).isNotEmpty()
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `MigrateAllItems - source vault is disabled when it has no folders`() = runTest {
        instance = buildViewModel(MigrateModeValue.AllVaultItems)
        observeVaults.sendResult(Result.success(listOf(sourceVault())))

        instance.state.test {
            val state = awaitItem()
            val sourceVaultState = state.vaultList.first { it.vaultWithItemCount.vault.shareId == SHARE_ID }
            assertThat(sourceVaultState.status)
                .isEqualTo(VaultStatus.Disabled(VaultStatus.DisabledReason.SameVault))
            assertThat(sourceVaultState.folderTree).isEmpty()
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `MigrateAllItems - source vault root is disabled but folders exposed when folder has no items`() = runTest {
        val fakeFolders = FakeObserveFoldersByParentId().apply {
            sendResult(Result.success(listOf(FolderTestFactory.create(shareId = SHARE_ID, folderId = FOLDER_ID))))
        }
        instance = buildViewModel(MigrateModeValue.AllVaultItems, fakeFolders)
        observeVaults.sendResult(Result.success(listOf(sourceVault())))

        instance.state.test {
            val state = awaitItem()
            val sourceVaultState = state.vaultList.first { it.vaultWithItemCount.vault.shareId == SHARE_ID }
            assertThat(sourceVaultState.status)
                .isEqualTo(VaultStatus.Disabled(VaultStatus.DisabledReason.SameVault))
            assertThat(sourceVaultState.folderTree).isNotEmpty()
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `MigrateAllItems - selecting source vault confirms flatten to root with null folder`() = runTest {
        instance = buildViewModel(MigrateModeValue.AllVaultItems)
        observeVaults.sendResult(Result.success(listOf(sourceVault())))

        instance.onVaultSelected(SHARE_ID)
        instance.onConfirm()

        val memory = migrateVault.memory()
        assertThat(memory).hasSize(1)
        assertThat(memory.first().origin).isEqualTo(SHARE_ID)
        assertThat(memory.first().destination).isEqualTo(SHARE_ID)
        assertThat(memory.first().destFolderId).isNull()
    }

    @Test
    fun `MigrateAllItems - viewer-access destination vault is disabled`() = runTest {
        instance = buildViewModel(MigrateModeValue.AllVaultItems)
        val viewerVault = VaultWithItemCount(
            vault = VaultTestFactory.create(shareId = OTHER_SHARE_ID, role = ShareRole.Read),
            activeItemCount = 5,
            trashedItemCount = 0
        )
        observeVaults.sendResult(Result.success(listOf(sourceVault(), viewerVault)))

        instance.state.test {
            val state = awaitItem()
            val viewerVaultState = state.vaultList.first { it.vaultWithItemCount.vault.shareId == OTHER_SHARE_ID }
            assertThat(viewerVaultState.status)
                .isEqualTo(VaultStatus.Disabled(VaultStatus.DisabledReason.NoPermission))
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `MigrateAllItems - admin-access destination vault is enabled`() = runTest {
        instance = buildViewModel(MigrateModeValue.AllVaultItems)
        val adminVault = VaultWithItemCount(
            vault = VaultTestFactory.create(shareId = OTHER_SHARE_ID, role = ShareRole.Admin),
            activeItemCount = 5,
            trashedItemCount = 0
        )
        observeVaults.sendResult(Result.success(listOf(sourceVault(), adminVault)))

        instance.state.test {
            val state = awaitItem()
            val adminVaultState = state.vaultList.first { it.vaultWithItemCount.vault.shareId == OTHER_SHARE_ID }
            assertThat(adminVaultState.status).isEqualTo(VaultStatus.Enabled)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `MigrateAllItems - read-only vault has no folders exposed`() = runTest {
        val folder = FolderTestFactory.create()
        val fakeFolders = FakeObserveFoldersByParentId().apply {
            sendResult(Result.success(listOf(folder)))
        }
        instance = buildViewModel(MigrateModeValue.AllVaultItems, fakeFolders)
        val viewerVault = VaultWithItemCount(
            vault = VaultTestFactory.create(shareId = OTHER_SHARE_ID, role = ShareRole.Read),
            activeItemCount = 5,
            trashedItemCount = 0
        )
        observeVaults.sendResult(Result.success(listOf(sourceVault(), viewerVault)))

        instance.state.test {
            val state = awaitItem()
            val viewerVaultState = state.vaultList.first { it.vaultWithItemCount.vault.shareId == OTHER_SHARE_ID }
            assertThat(viewerVaultState.folderTree).isEmpty()
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `MigrateAllItems - source vault folders are exposed even though root is disabled`() = runTest {
        val folder = FolderTestFactory.create(shareId = SHARE_ID, folderId = FOLDER_ID)
        val fakeFolders = FakeObserveFoldersByParentId().apply {
            sendResult(Result.success(listOf(folder)))
        }
        instance = buildViewModel(MigrateModeValue.AllVaultItems, fakeFolders)
        observeVaults.sendResult(Result.success(listOf(sourceVault())))

        instance.state.test {
            val state = awaitItem()
            val sourceVaultState = state.vaultList.first { it.vaultWithItemCount.vault.shareId == SHARE_ID }
            assertThat(sourceVaultState.status)
                .isEqualTo(VaultStatus.Disabled(VaultStatus.DisabledReason.SameVault))
            assertThat(sourceVaultState.folderTree).isNotEmpty()
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `hasFolders is true when vault has folders`() = runTest {
        val folder = FolderTestFactory.create()
        val fakeFolders = FakeObserveFoldersByParentId().apply {
            sendResult(Result.success(listOf(folder)))
        }
        val vm = buildViewModel(MigrateModeValue.SelectedItems, fakeFolders) {
            set(MigrateVaultFilterArg.key, MigrateVaultFilter.All.name)
            set(CommonOptionalNavArgId.ItemId.key, ITEM_ID.id)
        }
        observeVaults.sendResult(Result.success(listOf(sourceVault())))

        vm.state.test {
            val state = awaitItem()
            val vaultState = state.vaultList.firstOrNull { it.vaultWithItemCount.vault.shareId == SHARE_ID }
            assertThat(vaultState?.hasFolders).isTrue()
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `hasFolders is false when vault has no folders`() = runTest {
        observeVaults.sendResult(Result.success(listOf(sourceVault())))

        instance.state.test {
            val state = awaitItem()
            val vaultState = state.vaultList.firstOrNull { it.vaultWithItemCount.vault.shareId == SHARE_ID }
            assertThat(vaultState?.hasFolders).isFalse()
            cancelAndConsumeRemainingEvents()
        }
    }

    private fun buildViewModel(
        mode: MigrateModeValue,
        observeFoldersByParentId: FakeObserveFoldersByParentId = FakeObserveFoldersByParentId(),
        extraArgs: (androidx.lifecycle.SavedStateHandle.() -> Unit)? = null
    ): MigrateConfirmVaultViewModel = MigrateConfirmVaultViewModel(
        migrator = MigrateConfirmVaultMigrator(
            migrateItems = migrateItem,
            migrateVault = migrateVault,
            moveFolder = FakeMoveFolder(),
            dissolveFolder = FakeDissolveFolder(),
            moveAllItemsInFolder = FakeMoveAllItemsInFolder(),
            moveItemsInsideShare = FakeMoveItemsInsideShare(),
            snackbarDispatcher = snackbarDispatcher,
            bulkMoveToVaultRepository = bulkMoveToVaultRepository
        ),
        snackbarDispatcher = snackbarDispatcher,
        observeVaults = observeVaults,
        bulkMoveToVaultRepository = bulkMoveToVaultRepository,
        observeHasAssociatedSecureLinks = observeHasAssociatedSecureLinks,
        savedStateHandle = SavedStateHandleTestFactory.create().apply {
            set(CommonNavArgId.ShareId.key, SHARE_ID.id)
            set(MigrateModeArg.key, mode.name)
            extraArgs?.invoke(this)
        },
        observeShare = observeShare,
        settingsRepository = settingsRepository,
        observeFolders = observeFoldersByParentId,
        getMigrationItemsSelection = FakeGetMigrationItemsSelection()
    )

    private fun sourceVault(): VaultWithItemCount = VaultWithItemCount(
        vault = VaultTestFactory.create(shareId = SHARE_ID),
        activeItemCount = 1,
        trashedItemCount = 0
    )

    private fun initialVaults(): Pair<VaultWithItemCount, VaultWithItemCount> = Pair(
        VaultWithItemCount(
            vault = VaultTestFactory.create(shareId = SHARE_ID),
            activeItemCount = 1,
            trashedItemCount = 0
        ),
        VaultWithItemCount(
            vault = VaultTestFactory.create(shareId = OTHER_SHARE_ID),
            activeItemCount = 1,
            trashedItemCount = 0
        )
    )

    companion object {
        private val SHARE_ID = ShareId("123")
        private val OTHER_SHARE_ID = ShareId("OTHER_SHARE_ID")
        private val ITEM_ID = ItemId("789")
        private val FOLDER_ID = FolderId("folder-1")
    }
}
