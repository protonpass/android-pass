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

package proton.android.pass.features.itemcreate.common

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.data.api.usecases.defaultvault.VaultWithFolder
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.VaultTestFactory

class ShareUiStateFlowTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private fun makeVaultWithItemCount(shareId: ShareId = ShareId("vaultA")): VaultWithItemCount = VaultWithItemCount(
        vault = VaultTestFactory.create(shareId = shareId, name = "Test Vault"),
        activeItemCount = 1,
        trashedItemCount = 0
    )

    /**
     * Invokes [getShareUiStateFlow] with the given parameters and collects the first
     * [ShareUiState.Success] item emitted. Uses [TestScope.backgroundScope] so the
     * internal [stateIn] coroutine is cancelled automatically when the test ends.
     */
    private suspend fun TestScope.collectSuccess(
        navShareId: Option<ShareId> = None,
        selectedShareId: Option<ShareId> = None,
        selectedFolderId: Option<FolderId> = None,
        defaultVaultWithFolder: Option<VaultWithFolder> = None,
        vault: VaultWithItemCount = makeVaultWithItemCount()
    ): ShareUiState.Success {
        val stateFlow = getShareUiStateFlow(
            navShareIdState = flowOf(navShareId),
            selectedShareIdState = flowOf(selectedShareId),
            selectedFolderNameFlow = flowOf(null),
            selectedFolderIdFlow = flowOf(selectedFolderId),
            observeAllVaultsFlow = flowOf(LoadingResult.Success(listOf(vault))),
            observeDefaultVaultFlow = flowOf(LoadingResult.Success(defaultVaultWithFolder)),
            viewModelScope = backgroundScope,
            tag = "TestTag"
        )

        var result: ShareUiState.Success? = null
        stateFlow.test {
            while (result == null) {
                val item = awaitItem()
                if (item is ShareUiState.Success) result = item
            }
            cancelAndIgnoreRemainingEvents()
        }
        return result!!
    }

    // -----------------------------------------------------------------------
    // Test 1: All Items path — default folder is injected
    // -----------------------------------------------------------------------

    @Test
    fun `all items path with stored default folder applies effectiveFolderId`() = runTest {
        val testFolderId = FolderId("folder-1")
        val vault = makeVaultWithItemCount()
        val vaultWithFolder = VaultWithFolder(vault = vault, folderId = Some(testFolderId))

        val state = collectSuccess(
            navShareId = None,
            selectedShareId = None,
            selectedFolderId = None,
            defaultVaultWithFolder = Some(vaultWithFolder),
            vault = vault
        )

        assertThat(state.selectedFolder?.id).isEqualTo(testFolderId)
    }

    // -----------------------------------------------------------------------
    // Test 2: Explicit vault selected — default folder NOT injected
    // -----------------------------------------------------------------------

    @Test
    fun `explicit selectedShareId prevents default folder injection`() = runTest {
        val testFolderId = FolderId("folder-1")
        val vault = makeVaultWithItemCount(ShareId("vaultA"))
        val vaultWithFolder = VaultWithFolder(vault = vault, folderId = Some(testFolderId))

        val state = collectSuccess(
            navShareId = None,
            selectedShareId = Some(ShareId("vaultA")),
            selectedFolderId = None,
            defaultVaultWithFolder = Some(vaultWithFolder),
            vault = vault
        )

        // Explicit vault selected → no default folder injection
        assertThat(state.selectedFolder).isNull()
    }

    // -----------------------------------------------------------------------
    // Test 3: Nav vault provided — default folder NOT injected
    // -----------------------------------------------------------------------

    @Test
    fun `navShareId prevents default folder injection`() = runTest {
        val testFolderId = FolderId("folder-1")
        val vault = makeVaultWithItemCount(ShareId("vaultA"))
        val vaultWithFolder = VaultWithFolder(vault = vault, folderId = Some(testFolderId))

        val state = collectSuccess(
            navShareId = Some(ShareId("vaultA")),
            selectedShareId = None,
            selectedFolderId = None,
            defaultVaultWithFolder = Some(vaultWithFolder),
            vault = vault
        )

        // Nav vault provided → no default folder injection
        assertThat(state.selectedFolder).isNull()
    }

    // -----------------------------------------------------------------------
    // Test 4: User-selected folder overrides the stored default folder
    // -----------------------------------------------------------------------

    @Test
    fun `user selected folder overrides default folder from stored preference`() = runTest {
        val defaultFolderId = FolderId("folder-1")
        val userSelectedFolderId = FolderId("folder-2")
        val vault = makeVaultWithItemCount()
        val vaultWithFolder = VaultWithFolder(vault = vault, folderId = Some(defaultFolderId))

        val state = collectSuccess(
            navShareId = None,
            selectedShareId = None,
            selectedFolderId = Some(userSelectedFolderId),
            defaultVaultWithFolder = Some(vaultWithFolder),
            vault = vault
        )

        // User's explicit folder selection wins
        assertThat(state.selectedFolder?.id).isEqualTo(userSelectedFolderId)
    }
}
