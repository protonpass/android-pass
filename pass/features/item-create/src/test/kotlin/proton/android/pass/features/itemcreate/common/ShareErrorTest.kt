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
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.data.fakes.usecases.folders.FakeObserveFoldersByParentId
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.test.MainDispatcherRule

class ShareErrorTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Test
    fun `empty share list closes the create screen`() {
        assertThat(ShareError.EmptyShareList.closesCreateScreen).isTrue()
    }

    @Test
    fun `shares not available closes the create screen`() {
        assertThat(ShareError.SharesNotAvailable.closesCreateScreen).isTrue()
    }

    @Test
    fun `other errors keep the create screen open`() {
        assertThat(ShareError.UpgradeInfoNotAvailable.closesCreateScreen).isFalse()
        assertThat(ShareError.NoPrimaryVault.closesCreateScreen).isFalse()
    }

    @Test
    fun `zero vaults produce an error that closes the create screen`() = runTest {
        val stateFlow = getShareUiStateFlow(
            navShareIdState = flowOf(None),
            selectedShareIdState = flowOf(None),
            selectedFolderNameFlow = flowOf(null),
            selectedFolderIdFlow = flowOf(None),
            observeAllVaultsFlow = flowOf(LoadingResult.Success(emptyList<VaultWithItemCount>())),
            observeDefaultVaultFlow = flowOf(LoadingResult.Success(None)),
            observeFoldersByParentId = FakeObserveFoldersByParentId(Result.success(emptyList())),
            viewModelScope = backgroundScope,
            tag = "TestTag"
        )

        stateFlow.test {
            var state = awaitItem()
            while (state !is ShareUiState.Error) state = awaitItem()
            assertThat(state.shareError).isEqualTo(ShareError.EmptyShareList)
            assertThat(state.shareError.closesCreateScreen).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
