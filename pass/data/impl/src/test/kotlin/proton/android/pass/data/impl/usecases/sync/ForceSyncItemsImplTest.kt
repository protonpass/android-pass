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

package proton.android.pass.data.impl.usecases.sync

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Test
import proton.android.pass.data.api.usecases.sync.ForceSyncResult
import proton.android.pass.data.fakes.repositories.FakeItemRepository
import proton.android.pass.data.fakes.usecases.FakeItemSyncStatusRepository
import proton.android.pass.data.fakes.usecases.folders.FakeRefreshFolders
import proton.android.pass.domain.ShareId

class ForceSyncItemsImplTest {

    private lateinit var refreshFolders: FakeRefreshFolders
    private lateinit var itemRepository: FakeItemRepository
    private lateinit var itemSyncStatusRepository: FakeItemSyncStatusRepository
    private lateinit var instance: ForceSyncItemsImpl

    @Before
    fun setUp() {
        refreshFolders = FakeRefreshFolders()
        itemRepository = FakeItemRepository()
        itemSyncStatusRepository = FakeItemSyncStatusRepository()
        instance = ForceSyncItemsImpl(
            refreshFolders = refreshFolders,
            itemRepository = itemRepository,
            itemSyncStatusRepository = itemSyncStatusRepository
        )
    }

    @Test
    fun `refresh folders is always attempted before syncing items`() = runTest {
        val shareId = ShareId("share-1")

        val result = instance.invoke(
            userId = USER_ID,
            shareIds = setOf(shareId),
            isBackground = false,
            hasInactiveShares = false,
            hasInvalidGroupShares = false
        )

        assertThat(result).isEqualTo(ForceSyncResult.Success)
        assertThat(refreshFolders.invocations).hasSize(1)
        assertThat(refreshFolders.invocations.first().shareIds).containsExactly(shareId)
        assertThat(itemRepository.getDownloadItemsMemory()).hasSize(1)
        assertThat(itemRepository.getSetShareItemsMemory()).hasSize(1)
    }

    @Test
    fun `sync continues when folder refresh fails`() = runTest {
        val shareId = ShareId("share-1")
        refreshFolders.result = Result.failure(IllegalStateException("boom"))

        val result = instance.invoke(
            userId = USER_ID,
            shareIds = setOf(shareId),
            isBackground = false,
            hasInactiveShares = false,
            hasInvalidGroupShares = false
        )

        assertThat(result).isEqualTo(ForceSyncResult.Success)
        assertThat(refreshFolders.invocations).hasSize(1)
        assertThat(itemRepository.getDownloadItemsMemory()).hasSize(1)
        assertThat(itemRepository.getSetShareItemsMemory()).hasSize(1)
    }

    private companion object {
        private val USER_ID = UserId("user-id")
    }
}
