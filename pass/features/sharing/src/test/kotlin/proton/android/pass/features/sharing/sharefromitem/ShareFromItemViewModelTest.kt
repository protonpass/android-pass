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

package proton.android.pass.features.sharing.sharefromitem

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.commonui.fakes.FakeSavedStateHandleProvider
import proton.android.pass.data.api.repositories.ParentContainer
import proton.android.pass.data.fakes.repositories.FakeBulkMoveToVaultRepository
import proton.android.pass.data.fakes.usecases.FakeGetItemById
import proton.android.pass.data.fakes.usecases.FakeGetUserPlan
import proton.android.pass.data.fakes.usecases.FakeObserveOrganizationSettings
import proton.android.pass.data.fakes.usecases.shares.FakeObserveShare
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.ItemTestFactory

internal class ShareFromItemViewModelTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var bulkMoveToVaultRepository: FakeBulkMoveToVaultRepository
    private lateinit var savedStateHandleProvider: FakeSavedStateHandleProvider
    private lateinit var getItemById: FakeGetItemById

    @Before
    fun setUp() {
        bulkMoveToVaultRepository = FakeBulkMoveToVaultRepository()
        getItemById = FakeGetItemById()
        savedStateHandleProvider = FakeSavedStateHandleProvider().apply {
            get()[CommonNavArgId.ShareId.key] = SHARE_ID.id
            get()[CommonNavArgId.ItemId.key] = ITEM_ID.id
        }
    }

    @Test
    fun `moveItemToSharedVault saves share parent when item is at root`() = runTest {
        getItemById.emit(
            shareId = SHARE_ID,
            itemId = ITEM_ID,
            value = Result.success(
                ItemTestFactory.create(
                    shareId = SHARE_ID,
                    itemId = ITEM_ID,
                    folderId = null
                )
            )
        )
        val viewModel = createViewModel()

        viewModel.moveItemToSharedVault()
        advanceUntilIdle()

        val selection = bulkMoveToVaultRepository.observe().first().value()
        assertThat(selection).isEqualTo(
            mapOf(
                SHARE_ID to mapOf(
                    ParentContainer.Share to setOf(ITEM_ID)
                )
            )
        )
    }

    @Test
    fun `moveItemToSharedVault saves folder parent when item is in folder`() = runTest {
        val folderId = FolderId("folder-id")
        getItemById.emit(
            shareId = SHARE_ID,
            itemId = ITEM_ID,
            value = Result.success(
                ItemTestFactory.create(
                    shareId = SHARE_ID,
                    itemId = ITEM_ID,
                    folderId = folderId
                )
            )
        )
        val viewModel = createViewModel()

        viewModel.moveItemToSharedVault()
        advanceUntilIdle()

        val selection = bulkMoveToVaultRepository.observe().first().value()
        assertThat(selection).isEqualTo(
            mapOf(
                SHARE_ID to mapOf(
                    ParentContainer.Folder(folderId) to setOf(ITEM_ID)
                )
            )
        )
    }

    private fun createViewModel() = ShareFromItemViewModel(
        bulkMoveToVaultRepository = bulkMoveToVaultRepository,
        savedStateHandleProvider = savedStateHandleProvider,
        getUserPlan = FakeGetUserPlan(),
        getItemById = getItemById,
        observeShare = FakeObserveShare(),
        observeOrganizationSettings = FakeObserveOrganizationSettings()
    )

    private companion object {
        private val SHARE_ID = ShareId("share-id")
        private val ITEM_ID = ItemId("item-id")
    }
}
