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

package proton.android.pass.data.impl.usecases

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Rule
import org.junit.Test
import proton.android.pass.account.fakes.FakeUserAddressRepository
import proton.android.pass.data.api.usecases.RefreshSharesResult
import proton.android.pass.data.fakes.repositories.FakeItemRepository
import proton.android.pass.data.fakes.usecases.FakeItemSyncStatusRepository
import proton.android.pass.data.fakes.usecases.FakeRefreshSharesAndEnqueueSync
import proton.android.pass.data.impl.fakes.FakeEventRepository
import proton.android.pass.data.impl.fakes.FakePassDatabase
import proton.android.pass.data.impl.fakes.FakeShareRepository
import proton.android.pass.data.impl.responses.EventList
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.ShareTestFactory

class ApplyPendingEventsImplTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val userId = UserId("test-user")

    @Test
    fun `indexes a synced event once the transaction has committed`() = runTest {
        val database = FakePassDatabase()
        val wasInTransactionWhenIndexing = mutableListOf<Boolean>()
        val itemRepository = FakeItemRepository().apply {
            onIndexPendingEvent = { wasInTransactionWhenIndexing.add(database.isInTransaction) }
        }

        val share = ShareTestFactory.random()
        val shareRepository = FakeShareRepository().apply {
            setGetByIdResult(Result.success(share))
        }

        val addressRepository = FakeUserAddressRepository()
        val address = addressRepository.generateAddress("test1", userId)
        addressRepository.setAddresses(listOf(address))

        val eventRepository = FakeEventRepository().apply {
            setEvents(
                EventList(
                    shareResponse = null,
                    updatedItems = emptyList(),
                    deletedItemIds = listOf("deleted-item"),
                    newRotationId = null,
                    latestEventId = "last-event",
                    eventsPending = false
                )
            )
        }

        val refreshShares = FakeRefreshSharesAndEnqueueSync().apply {
            setResult(
                RefreshSharesResult.SharesFound(
                    shareIds = setOf(share.id),
                    isWorkerEnqueued = false,
                    hasInactiveShares = false,
                    hasInvalidGroupShares = false
                )
            )
        }

        val instance = ApplyPendingEventsImpl(
            database = database,
            eventRepository = eventRepository,
            addressRepository = addressRepository,
            itemRepository = itemRepository,
            shareRepository = shareRepository,
            itemSyncStatusRepository = FakeItemSyncStatusRepository(),
            refreshSharesAndEnqueueSync = refreshShares
        )

        instance(userId)

        assertThat(itemRepository.getIndexPendingEventMemory()).hasSize(1)
        assertThat(wasInTransactionWhenIndexing).containsExactly(false)
    }
}
