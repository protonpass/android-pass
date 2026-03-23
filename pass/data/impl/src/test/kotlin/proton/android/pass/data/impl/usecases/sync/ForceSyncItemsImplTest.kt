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
import kotlin.test.assertFailsWith
import proton.android.pass.data.api.repositories.ItemSyncStatus
import proton.android.pass.data.api.usecases.sync.ForceSyncResult
import proton.android.pass.data.fakes.repositories.FakeItemRepository
import proton.android.pass.data.fakes.repositories.ItemRevisionTestFactory
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
            hasInactiveShares = false,
            hasInvalidGroupShares = false,
            hasInvalidAddressShares = false
        )

        assertThat(result).isEqualTo(ForceSyncResult.Success)
        assertThat(refreshFolders.invocations).hasSize(1)
        assertThat(refreshFolders.invocations.first().shareIds).containsExactly(shareId)
        assertThat(itemRepository.getDownloadItemsMemory()).hasSize(1)
        assertThat(itemRepository.getSetShareItemsMemory()).hasSize(1)
    }

    @Test
    fun `refresh folders failure aborts sync`() = runTest {
        val shareId = ShareId("share-1")
        refreshFolders.result = Result.failure(IllegalStateException("folder refresh failed"))

        val error = assertFailsWith<IllegalStateException> {
            instance.invoke(
                userId = USER_ID,
                shareIds = setOf(shareId),
                hasInactiveShares = false,
                hasInvalidGroupShares = false,
                hasInvalidAddressShares = false
            )
        }

        assertThat(error.message).isEqualTo("folder refresh failed")
        assertThat(itemRepository.getDownloadItemsMemory()).isEmpty()
        assertThat(itemRepository.getSetShareItemsMemory()).isEmpty()
    }

    @Test
    fun `emits SyncSuccess when all shares sync successfully`() = runTest {
        val shareId = ShareId("share-1")
        itemRepository.setDownloadItemsResult(shareId, List(3) { ItemRevisionTestFactory.create(itemId = "item-$it") })

        val result = invoke(shareIds = setOf(shareId))

        assertThat(result).isEqualTo(ForceSyncResult.Success)
        assertThat(itemSyncStatusRepository.emittedStatuses.last())
            .isInstanceOf(ItemSyncStatus.SyncSuccess::class.java)
    }

    @Test
    fun `emits CryptoError when items fail to decrypt`() = runTest {
        val shareId = ShareId("share-1")
        itemRepository.setDownloadItemsResult(shareId, List(3) { ItemRevisionTestFactory.create(itemId = "item-$it") })
        itemRepository.setShareItemsInsertedCount = 2
        itemRepository.setShareItemsFailedShareIds = setOf(shareId)

        val result = invoke(shareIds = setOf(shareId))

        assertThat(result).isEqualTo(ForceSyncResult.PartialSuccess)
        val lastStatus = itemSyncStatusRepository.emittedStatuses.last()
        assertThat(lastStatus).isInstanceOf(ItemSyncStatus.SyncError.CryptoError::class.java)
        assertThat((lastStatus as ItemSyncStatus.SyncError.CryptoError).failedShareIds)
            .containsExactly(shareId)
    }

    @Test
    fun `corrects downloaded count for shares with crypto failures`() = runTest {
        val shareId = ShareId("share-1")
        itemRepository.setDownloadItemsResult(shareId, List(3) { ItemRevisionTestFactory.create(itemId = "item-$it") })
        itemRepository.setShareItemsInsertedCount = 2
        itemRepository.setShareItemsFailedShareIds = setOf(shareId)

        invoke(shareIds = setOf(shareId))

        val correctedDownload = itemSyncStatusRepository.emittedStatuses
            .filterIsInstance<ItemSyncStatus.SyncDownloading>()
            .lastOrNull { it.shareId == shareId }
        assertThat(correctedDownload).isNotNull()
        assertThat(correctedDownload!!.current).isEqualTo(2)
        assertThat(correctedDownload.total).isEqualTo(2)
    }

    @Test
    fun `does not emit corrected SyncDownloading for shares that succeed`() = runTest {
        val shareId = ShareId("share-1")
        itemRepository.setDownloadItemsResult(shareId, List(3) { ItemRevisionTestFactory.create(itemId = "item-$it") })

        invoke(shareIds = setOf(shareId))

        // Only download-phase SyncDownloading events, none with corrected counts
        val downloadingEvents = itemSyncStatusRepository.emittedStatuses
            .filterIsInstance<ItemSyncStatus.SyncDownloading>()
            .filter { it.shareId == shareId }
        assertThat(downloadingEvents).hasSize(1)
        assertThat(downloadingEvents.first().total).isEqualTo(3)
    }

    @Test
    fun `returns Error and emits DownloadError when download fails`() = runTest {
        val shareId = ShareId("share-1")
        itemRepository.downloadItemsException = RuntimeException("network error")

        val result = invoke(shareIds = setOf(shareId))

        assertThat(result).isEqualTo(ForceSyncResult.Error)
        assertThat(itemSyncStatusRepository.emittedStatuses.last())
            .isInstanceOf(ItemSyncStatus.SyncError.DownloadError::class.java)
    }

    @Test
    fun `returns Success with empty shareIds`() = runTest {
        val result = invoke(shareIds = emptySet())

        assertThat(result).isEqualTo(ForceSyncResult.Success)
        assertThat(itemSyncStatusRepository.emittedStatuses).isEmpty()
    }

    private suspend fun invoke(
        shareIds: Set<ShareId>,
        hasInactiveShares: Boolean = false,
        hasInvalidGroupShares: Boolean = false,
        hasInvalidAddressShares: Boolean = false
    ) = instance.invoke(
        userId = USER_ID,
        shareIds = shareIds,
        hasInactiveShares = hasInactiveShares,
        hasInvalidGroupShares = hasInvalidGroupShares,
        hasInvalidAddressShares = hasInvalidAddressShares
    )

    private companion object {
        private val USER_ID = UserId("user-id")
    }
}
