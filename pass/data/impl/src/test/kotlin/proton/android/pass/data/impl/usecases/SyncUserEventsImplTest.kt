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

package proton.android.pass.data.impl.usecases

import com.google.common.truth.Truth.assertThat
import proton.android.pass.data.api.repositories.IndexingStatus
import proton.android.pass.data.api.repositories.ItemSyncStatus
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import proton.android.pass.data.api.repositories.SyncMode
import proton.android.pass.data.api.usecases.RefreshSharesAndEnqueueSync
import proton.android.pass.data.api.work.FetchItemsState
import proton.android.pass.data.api.usecases.RefreshSharesResult
import proton.android.pass.data.fakes.repositories.FakeAliasRepository
import proton.android.pass.data.fakes.repositories.FakeItemRepository
import proton.android.pass.data.fakes.repositories.FakeSearchIndexRepository
import proton.android.pass.data.fakes.usecases.FakePromoteNewInviteToInvite
import proton.android.pass.data.fakes.usecases.FakeRefreshAliasSlNotes
import proton.android.pass.data.fakes.usecases.FakeRefreshBreaches
import proton.android.pass.data.fakes.usecases.FakeRefreshGroupInvites
import proton.android.pass.data.fakes.usecases.FakeItemSyncStatusRepository
import proton.android.pass.data.fakes.usecases.FakeRefreshOrganizationSettings
import proton.android.pass.data.fakes.usecases.FakeRefreshSharesAndEnqueueSync
import proton.android.pass.data.fakes.usecases.FakeRefreshUserInvites
import proton.android.pass.data.fakes.usecases.FakeRefreshUserAccess
import proton.android.pass.data.fakes.usecases.folders.FakeDeleteFoldersLocally
import proton.android.pass.data.fakes.usecases.folders.FakeRefreshFolders
import proton.android.pass.data.fakes.usecases.simplelogin.FakeSyncSimpleLoginPendingAliases
import proton.android.pass.data.api.work.UniqueWorkRequest
import proton.android.pass.data.fakes.work.FakeWorkManagerFacade
import proton.android.pass.data.impl.fakes.FakeEventRepository
import proton.android.pass.data.impl.fakes.FakeUserEventRepository
import proton.android.pass.data.impl.fakes.FakeShareRepository
import proton.android.pass.domain.FolderId
import proton.android.pass.data.impl.work.FetchItemsWorker
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.UserEventId
import proton.android.pass.domain.UserId
import proton.android.pass.domain.events.EventToken
import proton.android.pass.domain.events.SyncEventInvitesChanged
import proton.android.pass.domain.events.SyncEventShare
import proton.android.pass.domain.events.SyncEventShareFolder
import proton.android.pass.domain.events.SyncEventShareItem
import proton.android.pass.domain.events.UserEventList
import kotlin.test.assertFailsWith

internal class SyncUserEventsImplTest {

    private lateinit var instance: SyncUserEventsImpl
    private lateinit var searchIndexRepository: FakeSearchIndexRepository
    private lateinit var userEventRepository: FakeUserEventRepository
    private lateinit var shareRepository: FakeShareRepository
    private lateinit var itemRepository: FakeItemRepository
    private lateinit var refreshSharesAndEnqueueSync: FakeRefreshSharesAndEnqueueSync
    private lateinit var workManagerFacade: FakeWorkManagerFacade
    private lateinit var refreshPlan: FakeRefreshUserAccess
    private lateinit var refreshUserInvites: FakeRefreshUserInvites
    private lateinit var refreshGroupInvites: FakeRefreshGroupInvites
    private lateinit var refreshBreaches: FakeRefreshBreaches
    private lateinit var deleteFoldersLocally: FakeDeleteFoldersLocally
    private lateinit var refreshFolders: FakeRefreshFolders
    private lateinit var syncPendingAliases: FakeSyncSimpleLoginPendingAliases
    private lateinit var promoteNewInviteToInvite: FakePromoteNewInviteToInvite
    private lateinit var refreshOrganizationSettings: FakeRefreshOrganizationSettings
    private lateinit var itemSyncStatusRepository: FakeItemSyncStatusRepository
    private lateinit var eventRepository: FakeEventRepository
    private lateinit var aliasRepository: FakeAliasRepository
    private lateinit var refreshAliasSlNotes: FakeRefreshAliasSlNotes

    @Before
    fun setup() {
        userEventRepository = FakeUserEventRepository()
        shareRepository = FakeShareRepository()
        itemRepository = FakeItemRepository()
        refreshSharesAndEnqueueSync = FakeRefreshSharesAndEnqueueSync()
        workManagerFacade = FakeWorkManagerFacade()
        refreshPlan = FakeRefreshUserAccess()
        refreshUserInvites = FakeRefreshUserInvites()
        refreshGroupInvites = FakeRefreshGroupInvites()
        refreshBreaches = FakeRefreshBreaches()
        deleteFoldersLocally = FakeDeleteFoldersLocally()
        refreshFolders = FakeRefreshFolders()
        syncPendingAliases = FakeSyncSimpleLoginPendingAliases()
        promoteNewInviteToInvite = FakePromoteNewInviteToInvite()
        refreshOrganizationSettings = FakeRefreshOrganizationSettings()
        itemSyncStatusRepository = FakeItemSyncStatusRepository()
        eventRepository = FakeEventRepository()
        aliasRepository = FakeAliasRepository()
        refreshAliasSlNotes = FakeRefreshAliasSlNotes()
        searchIndexRepository = FakeSearchIndexRepository()

        instance = SyncUserEventsImpl(
            userEventRepository = userEventRepository,
            shareRepository = shareRepository,
            itemRepository = itemRepository,
            deleteFoldersLocally = deleteFoldersLocally,
            refreshFolders = refreshFolders,
            refreshSharesAndEnqueueSync = refreshSharesAndEnqueueSync,
            workManagerFacade = workManagerFacade,
            refreshUserAccess = refreshPlan,
            refreshUserInvites = refreshUserInvites,
            refreshGroupInvites = refreshGroupInvites,
            syncPendingAliases = syncPendingAliases,
            promoteNewInviteToInvite = promoteNewInviteToInvite,
            refreshBreaches = refreshBreaches,
            refreshOrganizationSettings = refreshOrganizationSettings,
            itemSyncStatusRepository = itemSyncStatusRepository,
            eventRepository = eventRepository,
            aliasRepository = aliasRepository,
            refreshAliasSlNotes = refreshAliasSlNotes,
            searchIndexRepository = searchIndexRepository
        )
    }

    @Test
    fun `local events already up to date returns early`() = runTest {
        val localEventId = UserEventId(EVENT_ID_1)
        val remoteEventId = UserEventId(EVENT_ID_1)

        userEventRepository.setGetLatestEventIdFlow(localEventId)
        userEventRepository.setFetchLatestEventIdResult(remoteEventId)

        instance.invoke(USER_ID)

        assertThat(userEventRepository.getStoreLatestEventIdMemory()).isEmpty()
    }

    @Test
    fun `no local event id triggers full refresh`() = runTest {
        val remoteEventId = UserEventId(EVENT_ID_1)

        userEventRepository.setGetLatestEventIdFlow(null)
        userEventRepository.setFetchLatestEventIdResult(remoteEventId)
        userEventRepository.setGetUserEventsResult(
            createUserEventList(
                lastEventId = remoteEventId,
                fullRefresh = false
            )
        )
        refreshSharesAndEnqueueSync.setResult(RefreshSharesResult.NoSharesSkipped())

        instance.invoke(USER_ID)

        assertThat(userEventRepository.getStoreLatestEventIdMemory()).isNotEmpty()
    }

    @Test
    fun `force sync requests the sync dialog by default`() = runTest {
        givenNoLocalEventId()

        instance.invoke(USER_ID, forceSync = true)

        assertThat(refreshSharesAndEnqueueSync.invocations.first().syncType)
            .isEqualTo(RefreshSharesAndEnqueueSync.SyncType.FULL)
    }

    @Test
    fun `force sync in background mode does not request the sync dialog`() = runTest {
        givenNoLocalEventId()

        instance.invoke(USER_ID, forceSync = true, syncMode = SyncMode.Background)

        assertThat(refreshSharesAndEnqueueSync.invocations.first().syncType)
            .isEqualTo(RefreshSharesAndEnqueueSync.SyncType.FULL_BACKGROUND)
    }

    @Test
    fun `shown force sync defers the success and keeps the dialog open for the user to continue`() = runTest {
        givenNoLocalEventId()
        itemSyncStatusRepository.setMode(SyncMode.ShownToUser)
        itemSyncStatusRepository.emit(ItemSyncStatus.SyncStarted)
        refreshSharesAndEnqueueSync.setResult(
            RefreshSharesResult.SharesFound(
                shareIds = setOf(ShareId(SHARE_ID_1)),
                isWorkerEnqueued = true,
                hasInactiveShares = true,
                hasInvalidGroupShares = false
            )
        )

        instance.invoke(USER_ID, forceSync = true)

        assertThat(refreshSharesAndEnqueueSync.invocations.first().deferSuccess).isTrue()
        assertThat(itemSyncStatusRepository.emittedStatuses.last())
            .isEqualTo(ItemSyncStatus.SyncSuccess(hasInactiveShares = true, hasInvalidGroupShares = false))
        assertThat(itemSyncStatusRepository.observeMode().first()).isEqualTo(SyncMode.ShownToUser)
    }

    @Test
    fun `shown force sync closes the dialog when the search index is not ready`() = runTest {
        givenNoLocalEventId()
        itemSyncStatusRepository.setMode(SyncMode.ShownToUser)
        itemSyncStatusRepository.emit(ItemSyncStatus.SyncStarted)
        searchIndexRepository.setIndexingStatus(IndexingStatus.Idle)
        refreshSharesAndEnqueueSync.setResult(
            RefreshSharesResult.SharesFound(
                shareIds = setOf(ShareId(SHARE_ID_1)),
                isWorkerEnqueued = true,
                hasInactiveShares = false,
                hasInvalidGroupShares = false
            )
        )

        instance.invoke(USER_ID, forceSync = true)

        assertThat(itemSyncStatusRepository.emittedStatuses.last()).isInstanceOf(ItemSyncStatus.SyncSuccess::class.java)
        assertThat(itemSyncStatusRepository.observeMode().first()).isEqualTo(SyncMode.Background)
    }

    @Test
    fun `shown force sync keeps the reported success when a step after the item download fails`() = runTest {
        givenNoLocalEventId()
        itemSyncStatusRepository.setMode(SyncMode.ShownToUser)
        itemSyncStatusRepository.emit(ItemSyncStatus.SyncStarted)
        refreshSharesAndEnqueueSync.setResult(
            RefreshSharesResult.SharesFound(
                shareIds = setOf(ShareId(SHARE_ID_1)),
                isWorkerEnqueued = true,
                hasInactiveShares = false,
                hasInvalidGroupShares = false
            )
        )
        refreshUserInvites.setRefreshResult(Result.failure(IllegalStateException("boom")))

        val result = runCatching { instance.invoke(USER_ID, forceSync = true) }

        assertThat(result.isFailure).isTrue()
        assertThat(itemSyncStatusRepository.emittedStatuses.last()).isInstanceOf(ItemSyncStatus.SyncSuccess::class.java)
        assertThat(itemSyncStatusRepository.emittedStatuses.filterIsInstance<ItemSyncStatus.SyncError>()).isEmpty()
        assertThat(itemSyncStatusRepository.observeMode().first()).isEqualTo(SyncMode.ShownToUser)
    }

    @Test
    fun `shown force sync reports an error and closes the dialog when the worker fails`() = runTest {
        givenNoLocalEventId()
        itemSyncStatusRepository.setMode(SyncMode.ShownToUser)
        itemSyncStatusRepository.emit(ItemSyncStatus.SyncStarted)
        refreshSharesAndEnqueueSync.setResult(
            RefreshSharesResult.SharesFound(
                shareIds = setOf(ShareId(SHARE_ID_1)),
                isWorkerEnqueued = true,
                hasInactiveShares = false,
                hasInvalidGroupShares = false
            )
        )
        workManagerFacade.awaitResult = FetchItemsState.Failure

        val result = runCatching { instance.invoke(USER_ID, forceSync = true) }

        assertThat(result.isFailure).isTrue()
        assertThat(itemSyncStatusRepository.emittedStatuses.filterIsInstance<ItemSyncStatus.SyncSuccess>()).isEmpty()
        assertThat(itemSyncStatusRepository.emittedStatuses.last())
            .isInstanceOf(ItemSyncStatus.SyncError.DownloadError::class.java)
        assertThat(itemSyncStatusRepository.observeMode().first()).isEqualTo(SyncMode.Background)
    }

    @Test
    fun `shown force sync keeps the error the worker reported when the worker fails`() = runTest {
        givenNoLocalEventId()
        itemSyncStatusRepository.setMode(SyncMode.ShownToUser)
        refreshSharesAndEnqueueSync.setResult(
            RefreshSharesResult.SharesFound(
                shareIds = setOf(ShareId(SHARE_ID_1)),
                isWorkerEnqueued = true,
                hasInactiveShares = false,
                hasInvalidGroupShares = false
            )
        )
        itemSyncStatusRepository.emit(ItemSyncStatus.SyncError.CryptoError(failedShareIds = setOf(ShareId(SHARE_ID_1))))
        workManagerFacade.awaitResult = FetchItemsState.Failure

        val result = runCatching { instance.invoke(USER_ID, forceSync = true) }

        assertThat(result.isFailure).isTrue()
        assertThat(itemSyncStatusRepository.emittedStatuses.last())
            .isEqualTo(ItemSyncStatus.SyncError.CryptoError(failedShareIds = setOf(ShareId(SHARE_ID_1))))
        assertThat(itemSyncStatusRepository.observeMode().first()).isEqualTo(SyncMode.Background)
    }

    @Test
    fun `shown force sync that fails before the sync started reports an error and closes the dialog`() = runTest {
        givenNoLocalEventId()
        itemSyncStatusRepository.setMode(SyncMode.ShownToUser)
        refreshPlan.onInvoke = { throw IllegalStateException("access refresh failed") }

        val result = runCatching { instance.invoke(USER_ID, forceSync = true) }

        assertThat(result.isFailure).isTrue()
        assertThat(itemSyncStatusRepository.emittedStatuses.last())
            .isInstanceOf(ItemSyncStatus.SyncError.DownloadError::class.java)
        assertThat(itemSyncStatusRepository.observeMode().first()).isEqualTo(SyncMode.Background)
    }

    @Test
    fun `shown force sync with no shares emits the success and closes the dialog`() = runTest {
        givenNoLocalEventId()
        itemSyncStatusRepository.setMode(SyncMode.ShownToUser)
        itemSyncStatusRepository.emit(ItemSyncStatus.SyncStarted)
        refreshSharesAndEnqueueSync.setResult(RefreshSharesResult.NoSharesSkipped(hasInactiveShares = true))

        instance.invoke(USER_ID, forceSync = true)

        assertThat(itemSyncStatusRepository.emittedStatuses.last())
            .isEqualTo(ItemSyncStatus.SyncSuccess(hasInactiveShares = true, hasInvalidGroupShares = false))
        assertThat(itemSyncStatusRepository.observeMode().first()).isEqualTo(SyncMode.Background)
    }

    @Test
    fun `shown force sync with no shares keeps the success when a later step fails`() = runTest {
        givenNoLocalEventId()
        itemSyncStatusRepository.setMode(SyncMode.ShownToUser)
        itemSyncStatusRepository.emit(ItemSyncStatus.SyncStarted)
        refreshSharesAndEnqueueSync.setResult(RefreshSharesResult.NoSharesVaultCreated())
        refreshUserInvites.setRefreshResult(Result.failure(IllegalStateException("boom")))

        runCatching { instance.invoke(USER_ID, forceSync = true) }

        assertThat(itemSyncStatusRepository.emittedStatuses.last()).isInstanceOf(ItemSyncStatus.SyncSuccess::class.java)
        assertThat(itemSyncStatusRepository.emittedStatuses.filterIsInstance<ItemSyncStatus.SyncError>()).isEmpty()
    }

    @Test
    fun `deferred success does not overwrite an error the worker already reported`() = runTest {
        givenNoLocalEventId()
        itemSyncStatusRepository.setMode(SyncMode.ShownToUser)
        refreshSharesAndEnqueueSync.setResult(
            RefreshSharesResult.SharesFound(
                shareIds = setOf(ShareId(SHARE_ID_1)),
                isWorkerEnqueued = true,
                hasInactiveShares = false,
                hasInvalidGroupShares = false
            )
        )
        itemSyncStatusRepository.emit(ItemSyncStatus.SyncError.CryptoError(failedShareIds = setOf(ShareId(SHARE_ID_1))))

        instance.invoke(USER_ID, forceSync = true)

        assertThat(itemSyncStatusRepository.emittedStatuses.last())
            .isInstanceOf(ItemSyncStatus.SyncError.CryptoError::class.java)
    }

    @Test
    fun `success is still emitted when the dialog is dismissed during a shares found sync`() = runTest {
        givenNoLocalEventId()
        itemSyncStatusRepository.setMode(SyncMode.ShownToUser)
        itemSyncStatusRepository.emit(ItemSyncStatus.SyncStarted)
        itemSyncStatusRepository.setMode(SyncMode.Background)
        refreshSharesAndEnqueueSync.setResult(
            RefreshSharesResult.SharesFound(
                shareIds = setOf(ShareId(SHARE_ID_1)),
                isWorkerEnqueued = true,
                hasInactiveShares = false,
                hasInvalidGroupShares = false
            )
        )

        instance.invoke(USER_ID, forceSync = true, syncMode = SyncMode.ShownToUser)

        assertThat(itemSyncStatusRepository.emittedStatuses.last())
            .isInstanceOf(ItemSyncStatus.SyncSuccess::class.java)
        assertThat(itemSyncStatusRepository.observeSyncState().first().isSyncing).isFalse()
    }

    @Test
    fun `success is still emitted when the dialog is dismissed during an empty shares sync`() = runTest {
        givenNoLocalEventId()
        itemSyncStatusRepository.emit(ItemSyncStatus.SyncStarted)
        itemSyncStatusRepository.setMode(SyncMode.Background)
        refreshSharesAndEnqueueSync.setResult(RefreshSharesResult.NoSharesVaultCreated())

        instance.invoke(USER_ID, forceSync = true, syncMode = SyncMode.ShownToUser)

        assertThat(itemSyncStatusRepository.emittedStatuses.last())
            .isInstanceOf(ItemSyncStatus.SyncSuccess::class.java)
    }

    @Test
    fun `cancelling a sync that is still syncing settles the status with an error`() =
        runTest(UnconfinedTestDispatcher()) {
            givenNoLocalEventId()
            refreshSharesAndEnqueueSync.setResult(
                RefreshSharesResult.SharesFound(
                    shareIds = setOf(ShareId(SHARE_ID_1)),
                    isWorkerEnqueued = true,
                    hasInactiveShares = false,
                    hasInvalidGroupShares = false
                )
            )
            itemSyncStatusRepository.setMode(SyncMode.ShownToUser)
            itemSyncStatusRepository.emit(ItemSyncStatus.SyncStarted)
            workManagerFacade.awaitGate = CompletableDeferred()

            val job = launch { instance.invoke(USER_ID, forceSync = true) }
            job.cancelAndJoin()

            assertThat(itemSyncStatusRepository.emittedStatuses.last())
                .isInstanceOf(ItemSyncStatus.SyncError.DownloadError::class.java)
            assertThat(itemSyncStatusRepository.observeSyncState().first().isSyncing).isFalse()
        }

    @Test
    fun `cancelling a shown force sync while waiting for the worker resets the dialog mode`() =
        runTest(UnconfinedTestDispatcher()) {
            givenNoLocalEventId()
            refreshSharesAndEnqueueSync.setResult(
                RefreshSharesResult.SharesFound(
                    shareIds = setOf(ShareId(SHARE_ID_1)),
                    isWorkerEnqueued = true,
                    hasInactiveShares = false,
                    hasInvalidGroupShares = false
                )
            )
            itemSyncStatusRepository.setMode(SyncMode.ShownToUser)
            workManagerFacade.awaitGate = CompletableDeferred()

            val job = launch { instance.invoke(USER_ID, forceSync = true) }
            job.cancelAndJoin()

            assertThat(itemSyncStatusRepository.observeMode().first()).isEqualTo(SyncMode.Background)
        }

    @Test
    fun `background force sync does not defer the success`() = runTest {
        givenNoLocalEventId()

        instance.invoke(USER_ID, forceSync = true, syncMode = SyncMode.Background)

        assertThat(refreshSharesAndEnqueueSync.invocations.first().deferSuccess).isFalse()
    }

    @Test
    fun `cancelling after the success was reported leaves the dialog open`() = runTest(UnconfinedTestDispatcher()) {
        givenNoLocalEventId()
        itemSyncStatusRepository.setMode(SyncMode.ShownToUser)
        itemSyncStatusRepository.emit(ItemSyncStatus.SyncStarted)
        refreshSharesAndEnqueueSync.setResult(
            RefreshSharesResult.SharesFound(
                shareIds = setOf(ShareId(SHARE_ID_1)),
                isWorkerEnqueued = true,
                hasInactiveShares = false,
                hasInvalidGroupShares = false
            )
        )
        val gate = CompletableDeferred<Unit>()
        refreshUserInvites.gate = gate

        val job = launch { instance.invoke(USER_ID, forceSync = true) }
        job.cancelAndJoin()

        assertThat(itemSyncStatusRepository.emittedStatuses.last())
            .isInstanceOf(ItemSyncStatus.SyncSuccess::class.java)
        assertThat(itemSyncStatusRepository.observeMode().first()).isEqualTo(SyncMode.ShownToUser)
    }

    @Test
    fun `background force sync does not report a dialog error when a step fails`() = runTest {
        givenNoLocalEventId()
        refreshUserInvites.setRefreshResult(Result.failure(IllegalStateException("boom")))

        runCatching { instance.invoke(USER_ID, forceSync = true, syncMode = SyncMode.Background) }

        assertThat(itemSyncStatusRepository.emittedStatuses).isEmpty()
    }

    @Test
    fun `concurrent non forced syncs for the same user run only once`() = runTest(UnconfinedTestDispatcher()) {
        givenNoLocalEventId()
        val gate = CompletableDeferred<Unit>()
        refreshSharesAndEnqueueSync.gate = gate

        val calls = List(7) { launch { instance.invoke(USER_ID, syncMode = SyncMode.Background) } }
        gate.complete(Unit)
        calls.joinAll()

        assertThat(refreshSharesAndEnqueueSync.invocations).hasSize(1)
    }

    @Test
    fun `releases the lock after a sync so the next one runs`() = runTest {
        givenNoLocalEventId()

        instance.invoke(USER_ID, syncMode = SyncMode.Background)
        userEventRepository.setGetLatestEventIdFlow(null)
        instance.invoke(USER_ID, syncMode = SyncMode.Background)

        assertThat(refreshSharesAndEnqueueSync.invocations).hasSize(2)
    }

    private suspend fun givenNoLocalEventId() {
        val remoteEventId = UserEventId(EVENT_ID_1)
        userEventRepository.setGetLatestEventIdFlow(null)
        userEventRepository.setFetchLatestEventIdResult(remoteEventId)
        userEventRepository.setGetUserEventsResult(
            createUserEventList(lastEventId = remoteEventId, fullRefresh = false)
        )
        refreshSharesAndEnqueueSync.setResult(RefreshSharesResult.NoSharesSkipped())
    }

    @Test
    fun `processes plan changed event`() = runTest {
        val eventId = UserEventId(EVENT_ID_1)
        setupBasicSync(eventId)

        userEventRepository.setGetUserEventsResult(
            createUserEventList(
                lastEventId = eventId,
                planChanged = true
            )
        )

        instance.invoke(USER_ID)

        assertThat(userEventRepository.getStoreLatestEventIdMemory()).isNotEmpty()
    }

    @Test
    fun `processes shares created events`() = runTest {
        val eventId = UserEventId(EVENT_ID_1)
        setupBasicSync(eventId)

        val share1 = SyncEventShare(ShareId(SHARE_ID_1), EventToken(TOKEN_1))
        val share2 = SyncEventShare(ShareId(SHARE_ID_2), EventToken(TOKEN_2))

        userEventRepository.setGetUserEventsResult(
            createUserEventList(
                lastEventId = eventId,
                sharesCreated = listOf(share1, share2)
            )
        )

        instance.invoke(USER_ID)

        assertThat(userEventRepository.getStoreLatestEventIdMemory()).isNotEmpty()
        assertThat(workManagerFacade.getEnqueuedWork()).isNotEmpty()
        val enqueuedWorkRequest = workManagerFacade.getEnqueuedWork().firstOrNull()?.second
        assertThat(enqueuedWorkRequest).isInstanceOf(UniqueWorkRequest.FetchItems::class.java)
        val fetchItemsRequest = enqueuedWorkRequest as? UniqueWorkRequest.FetchItems
        assertThat(fetchItemsRequest?.shareIds).containsExactly(ShareId(SHARE_ID_1), ShareId(SHARE_ID_2))
    }

    @Test
    fun `processes shares updated events`() = runTest {
        val eventId = UserEventId(EVENT_ID_1)
        setupBasicSync(eventId)

        val share1 = SyncEventShare(ShareId(SHARE_ID_1), EventToken(TOKEN_1))
        val share2 = SyncEventShare(ShareId(SHARE_ID_2), EventToken(TOKEN_2))

        userEventRepository.setGetUserEventsResult(
            createUserEventList(
                lastEventId = eventId,
                sharesUpdated = listOf(share1, share2)
            )
        )

        instance.invoke(USER_ID)

        assertThat(userEventRepository.getStoreLatestEventIdMemory()).isNotEmpty()
        assertThat(shareRepository.refreshShareMemory()).hasSize(2)
    }

    @Test
    fun `processes shares deleted events`() = runTest {
        val eventId = UserEventId(EVENT_ID_1)
        setupBasicSync(eventId)

        val share1 = SyncEventShare(ShareId(SHARE_ID_1), EventToken(TOKEN_1))
        val share2 = SyncEventShare(ShareId(SHARE_ID_2), EventToken(TOKEN_2))

        userEventRepository.setGetUserEventsResult(
            createUserEventList(
                lastEventId = eventId,
                sharesDeleted = listOf(share1, share2)
            )
        )

        instance.invoke(USER_ID)

        assertThat(userEventRepository.getStoreLatestEventIdMemory()).isNotEmpty()
    }

    @Test
    fun `processes items updated events`() = runTest {
        val eventId = UserEventId(EVENT_ID_1)
        setupBasicSync(eventId)

        val item1 = SyncEventShareItem(ShareId(SHARE_ID_1), ItemId(ITEM_ID_1), EventToken(TOKEN_1))
        val item2 = SyncEventShareItem(ShareId(SHARE_ID_2), ItemId(ITEM_ID_2), EventToken(TOKEN_2))

        userEventRepository.setGetUserEventsResult(
            createUserEventList(
                lastEventId = eventId,
                itemsUpdated = listOf(item1, item2)
            )
        )

        instance.invoke(USER_ID)

        assertThat(userEventRepository.getStoreLatestEventIdMemory()).isNotEmpty()
    }

    @Test
    fun `refreshes SL notes of the items updated`() = runTest {
        val eventId = UserEventId(EVENT_ID_1)
        setupBasicSync(eventId)

        val item1 = SyncEventShareItem(ShareId(SHARE_ID_1), ItemId(ITEM_ID_1), EventToken(TOKEN_1))
        val item2 = SyncEventShareItem(ShareId(SHARE_ID_2), ItemId(ITEM_ID_2), EventToken(TOKEN_2))

        userEventRepository.setGetUserEventsResult(
            createUserEventList(
                lastEventId = eventId,
                itemsUpdated = listOf(item1, item2)
            )
        )

        instance.invoke(USER_ID)

        assertThat(aliasRepository.getRefreshAliasSlNotesForItemsMemory()).containsExactly(
            USER_ID to listOf(
                ShareId(SHARE_ID_1) to ItemId(ITEM_ID_1),
                ShareId(SHARE_ID_2) to ItemId(ITEM_ID_2)
            )
        )
    }

    @Test
    fun `does not refresh SL notes when no item was updated`() = runTest {
        val eventId = UserEventId(EVENT_ID_1)
        setupBasicSync(eventId)

        userEventRepository.setGetUserEventsResult(createUserEventList(lastEventId = eventId))

        instance.invoke(USER_ID)

        assertThat(aliasRepository.getRefreshAliasSlNotesForItemsMemory()).isEmpty()
    }

    @Test
    fun `processes folders updated before items updated`() = runTest {
        val eventId = UserEventId(EVENT_ID_1)
        setupBasicSync(eventId)
        val callOrder = mutableListOf<String>()
        refreshFolders.onInvoke = { callOrder += "folders" }
        itemRepository.onRefreshItem = { callOrder += "items" }

        userEventRepository.setGetUserEventsResult(
            createUserEventList(
                lastEventId = eventId,
                foldersUpdated = listOf(
                    SyncEventShareFolder(
                        shareId = ShareId(SHARE_ID_1),
                        folderId = FolderId(FOLDER_ID_1),
                        eventToken = EventToken(TOKEN_1)
                    )
                ),
                itemsUpdated = listOf(
                    SyncEventShareItem(
                        shareId = ShareId(SHARE_ID_1),
                        itemId = ItemId(ITEM_ID_1),
                        eventToken = EventToken(TOKEN_2)
                    )
                )
            )
        )

        instance.invoke(USER_ID)

        assertThat(callOrder).containsExactly("folders", "items").inOrder()
    }

    @Test
    fun `processes items deleted events`() = runTest {
        val eventId = UserEventId(EVENT_ID_1)
        setupBasicSync(eventId)

        val item1 = SyncEventShareItem(ShareId(SHARE_ID_1), ItemId(ITEM_ID_1), EventToken(TOKEN_1))
        val item2 = SyncEventShareItem(ShareId(SHARE_ID_1), ItemId(ITEM_ID_2), EventToken(TOKEN_2))
        val item3 = SyncEventShareItem(ShareId(SHARE_ID_2), ItemId(ITEM_ID_3), EventToken(TOKEN_3))

        userEventRepository.setGetUserEventsResult(
            createUserEventList(
                lastEventId = eventId,
                itemsDeleted = listOf(item1, item2, item3)
            )
        )

        instance.invoke(USER_ID)

        assertThat(userEventRepository.getStoreLatestEventIdMemory()).isNotEmpty()
    }

    @Test
    fun `processes user invites changed event`() = runTest {
        val eventId = UserEventId(EVENT_ID_1)
        setupBasicSync(eventId)

        val invitesChanged = SyncEventInvitesChanged(EventToken(TOKEN_1))

        userEventRepository.setGetUserEventsResult(
            createUserEventList(
                lastEventId = eventId,
                invitesChanged = invitesChanged
            )
        )

        instance.invoke(USER_ID)

        assertThat(userEventRepository.getStoreLatestEventIdMemory()).isNotEmpty()
    }

    @Test
    fun `processes group invites changed event`() = runTest {
        val eventId = UserEventId(EVENT_ID_1)
        setupBasicSync(eventId)

        val groupInvitesChanged = SyncEventInvitesChanged(EventToken(TOKEN_1))

        userEventRepository.setGetUserEventsResult(
            createUserEventList(
                lastEventId = eventId,
                groupInvitesChanged = groupInvitesChanged
            )
        )

        instance.invoke(USER_ID)

        assertThat(userEventRepository.getStoreLatestEventIdMemory()).isNotEmpty()
    }

    @Test
    fun `processes pending alias changed event`() = runTest {
        val eventId = UserEventId(EVENT_ID_1)
        setupBasicSync(eventId)

        val pendingAliasChanged = SyncEventInvitesChanged(EventToken(TOKEN_1))

        userEventRepository.setGetUserEventsResult(
            createUserEventList(
                lastEventId = eventId,
                pendingAliasToCreateChanged = pendingAliasChanged
            )
        )

        instance.invoke(USER_ID)

        assertThat(userEventRepository.getStoreLatestEventIdMemory()).isNotEmpty()
    }

    @Test
    fun `processes new user invites changed events`() = runTest {
        val eventId = UserEventId(EVENT_ID_1)
        setupBasicSync(eventId)

        val share1 = SyncEventShare(ShareId(SHARE_ID_1), EventToken(TOKEN_1))
        val share2 = SyncEventShare(ShareId(SHARE_ID_2), EventToken(TOKEN_2))

        userEventRepository.setGetUserEventsResult(
            createUserEventList(
                lastEventId = eventId,
                sharesWithInvitesToCreate = listOf(share1, share2)
            )
        )

        instance.invoke(USER_ID)

        assertThat(userEventRepository.getStoreLatestEventIdMemory()).isNotEmpty()
        assertThat(promoteNewInviteToInvite.getInvocationMemory()).hasSize(2)
        assertThat(promoteNewInviteToInvite.getInvocationMemory()[0].second).isEqualTo(ShareId(SHARE_ID_1))
        assertThat(promoteNewInviteToInvite.getInvocationMemory()[1].second).isEqualTo(ShareId(SHARE_ID_2))
    }

    @Test
    fun `full refresh when event list has fullRefresh flag`() = runTest {
        val eventId = UserEventId(EVENT_ID_1)
        setupBasicSync(eventId)

        userEventRepository.setGetUserEventsResult(
            createUserEventList(
                lastEventId = eventId,
                fullRefresh = true
            )
        )
        refreshSharesAndEnqueueSync.setResult(RefreshSharesResult.NoSharesSkipped())

        instance.invoke(USER_ID)

        assertThat(userEventRepository.getStoreLatestEventIdMemory()).isNotEmpty()
    }

    @Test
    fun `full refresh waits for worker when shares found and worker enqueued`() = runTest {
        val eventId = UserEventId(EVENT_ID_1)
        setupBasicSync(eventId)

        userEventRepository.setGetUserEventsResult(
            createUserEventList(
                lastEventId = eventId,
                fullRefresh = true
            )
        )
        refreshSharesAndEnqueueSync.setResult(
            RefreshSharesResult.SharesFound(
                shareIds = setOf(ShareId(SHARE_ID_1)),
                isWorkerEnqueued = true,
                hasInactiveShares = false,
                hasInvalidGroupShares = false
            )
        )

        val uniqueName = FetchItemsWorker.getOneTimeUniqueWorkName(USER_ID)

        instance.invoke(USER_ID)

        assertThat(userEventRepository.getStoreLatestEventIdMemory()).isNotEmpty()
        assertThat(workManagerFacade.getAwaitedWorkNames()).contains(uniqueName)
    }

    @Test
    fun `full refresh refreshes alias notes after completed item worker`() = runTest {
        val eventId = UserEventId(EVENT_ID_1)
        setupBasicSync(eventId)

        userEventRepository.setGetUserEventsResult(
            createUserEventList(
                lastEventId = eventId,
                fullRefresh = true
            )
        )
        refreshSharesAndEnqueueSync.setResult(
            RefreshSharesResult.SharesFound(
                shareIds = setOf(ShareId(SHARE_ID_1)),
                isWorkerEnqueued = true,
                hasInactiveShares = false,
                hasInvalidGroupShares = false
            )
        )

        instance.invoke(USER_ID)

        assertThat(refreshAliasSlNotes.getInvocationMemory()).containsExactly(USER_ID)
    }

    @Test
    fun `full refresh refreshes alias notes when item worker is not enqueued`() = runTest {
        val eventId = UserEventId(EVENT_ID_1)
        setupBasicSync(eventId)

        userEventRepository.setGetUserEventsResult(
            createUserEventList(
                lastEventId = eventId,
                fullRefresh = true
            )
        )
        refreshSharesAndEnqueueSync.setResult(RefreshSharesResult.NoSharesSkipped())

        instance.invoke(USER_ID)

        assertThat(refreshAliasSlNotes.getInvocationMemory()).containsExactly(USER_ID)
    }

    @Test
    fun `full refresh throws when refreshing alias notes fails`() = runTest {
        val eventId = UserEventId(EVENT_ID_1)
        setupBasicSync(eventId)
        userEventRepository.setGetUserEventsResult(
            createUserEventList(
                lastEventId = eventId,
                fullRefresh = true
            )
        )
        refreshSharesAndEnqueueSync.setResult(RefreshSharesResult.NoSharesSkipped())
        refreshAliasSlNotes.setResult(Result.failure(IllegalStateException("SL notes refresh failed")))

        assertFailsWith<IllegalStateException> {
            instance.invoke(USER_ID)
        }
    }

    @Test
    fun `full refresh skips worker wait when no shares found`() = runTest {
        val eventId = UserEventId(EVENT_ID_1)
        setupBasicSync(eventId)

        userEventRepository.setGetUserEventsResult(
            createUserEventList(
                lastEventId = eventId,
                fullRefresh = true
            )
        )
        refreshSharesAndEnqueueSync.setResult(RefreshSharesResult.NoSharesSkipped())

        instance.invoke(USER_ID)

        assertThat(userEventRepository.getStoreLatestEventIdMemory()).isNotEmpty()
    }

    @Test
    fun `processes multiple event batches when eventsPending is true`() = runTest {
        val eventId1 = UserEventId(EVENT_ID_1)
        val eventId2 = UserEventId(EVENT_ID_2)
        val eventId3 = UserEventId(EVENT_ID_3)
        setupBasicSync(eventId1)

        // First batch - events pending
        userEventRepository.setGetUserEventsResult(
            createUserEventList(
                lastEventId = eventId2,
                eventsPending = true
            )
        )

        // Second batch - events pending
        userEventRepository.setGetUserEventsResult(
            createUserEventList(
                lastEventId = eventId3,
                eventsPending = false
            )
        )

        instance.invoke(USER_ID)

        val storedEvents = userEventRepository.getStoreLatestEventIdMemory()
        assertThat(storedEvents).isNotEmpty()
    }

    @Test
    fun `stores latest event id after each batch`() = runTest {
        val eventId = UserEventId(EVENT_ID_1)
        setupBasicSync(eventId)

        userEventRepository.setGetUserEventsResult(
            createUserEventList(
                lastEventId = eventId,
                eventsPending = false
            )
        )

        instance.invoke(USER_ID)

        val storedEvents = userEventRepository.getStoreLatestEventIdMemory()
        assertThat(storedEvents).hasSize(1)
        assertThat(storedEvents[0].second).isEqualTo(eventId)
    }

    @Test
    fun `processes alias note changed events`() = runTest {
        val eventId = UserEventId(EVENT_ID_1)
        setupBasicSync(eventId)

        val alias1 = SyncEventShareItem(ShareId(SHARE_ID_1), ItemId(ITEM_ID_1), EventToken(TOKEN_1))
        val alias2 = SyncEventShareItem(ShareId(SHARE_ID_2), ItemId(ITEM_ID_2), EventToken(TOKEN_2))

        userEventRepository.setGetUserEventsResult(
            createUserEventList(
                lastEventId = eventId,
                aliasNoteChanged = listOf(alias1, alias2)
            )
        )

        instance.invoke(USER_ID)

        assertThat(userEventRepository.getStoreLatestEventIdMemory()).isNotEmpty()
        assertThat(aliasRepository.getRefreshAliasSlNoteMemory()).hasSize(2)
    }

    @Test
    fun `handles empty event lists`() = runTest {
        val eventId = UserEventId(EVENT_ID_1)
        setupBasicSync(eventId)

        userEventRepository.setGetUserEventsResult(
            createUserEventList(
                lastEventId = eventId,
                itemsUpdated = emptyList(),
                itemsDeleted = emptyList(),
                sharesCreated = emptyList(),
                sharesUpdated = emptyList(),
                sharesDeleted = emptyList()
            )
        )

        instance.invoke(USER_ID)

        assertThat(userEventRepository.getStoreLatestEventIdMemory()).isNotEmpty()
    }

    @Test
    fun `handles null invites changed events`() = runTest {
        val eventId = UserEventId(EVENT_ID_1)
        setupBasicSync(eventId)

        userEventRepository.setGetUserEventsResult(
            createUserEventList(
                lastEventId = eventId,
                invitesChanged = null,
                groupInvitesChanged = null,
                pendingAliasToCreateChanged = null
            )
        )

        instance.invoke(USER_ID)

        assertThat(userEventRepository.getStoreLatestEventIdMemory()).isNotEmpty()
        assertThat(workManagerFacade.getAwaitedWorkNames()).isEmpty()
    }

    @Test
    fun `handles work manager with finished state`() = runTest {
        val eventId = UserEventId(EVENT_ID_1)
        setupBasicSync(eventId)

        userEventRepository.setGetUserEventsResult(
            createUserEventList(
                lastEventId = eventId,
                fullRefresh = true
            )
        )
        refreshSharesAndEnqueueSync.setResult(
            RefreshSharesResult.SharesFound(
                shareIds = setOf(ShareId(SHARE_ID_1)),
                isWorkerEnqueued = true,
                hasInactiveShares = false,
                hasInvalidGroupShares = false
            )
        )

        instance.invoke(USER_ID)

        assertThat(userEventRepository.getStoreLatestEventIdMemory()).isNotEmpty()
    }

    @Test
    fun `items updated - all items are refreshed`() = runTest {
        val eventId = UserEventId(EVENT_ID_1)
        setupBasicSync(eventId)

        val items = listOf(
            SyncEventShareItem(ShareId(SHARE_ID_1), ItemId(ITEM_ID_1), EventToken(TOKEN_1)),
            SyncEventShareItem(ShareId(SHARE_ID_1), ItemId(ITEM_ID_2), EventToken(TOKEN_2)),
            SyncEventShareItem(ShareId(SHARE_ID_2), ItemId(ITEM_ID_3), EventToken(TOKEN_3))
        )
        userEventRepository.setGetUserEventsResult(
            createUserEventList(lastEventId = eventId, itemsUpdated = items)
        )

        instance.invoke(USER_ID)

        val refreshed = itemRepository.getRefreshItemMemory()
        assertThat(refreshed).hasSize(3)
        assertThat(refreshed.map { it.shareId to it.itemId }).containsExactly(
            ShareId(SHARE_ID_1) to ItemId(ITEM_ID_1),
            ShareId(SHARE_ID_1) to ItemId(ITEM_ID_2),
            ShareId(SHARE_ID_2) to ItemId(ITEM_ID_3)
        )
    }

    @Test
    fun `items updated - large batch beyond concurrency limit is fully processed`() = runTest {
        val eventId = UserEventId(EVENT_ID_1)
        setupBasicSync(eventId)

        val items = (1..30).map { i ->
            SyncEventShareItem(ShareId(SHARE_ID_1), ItemId("item-$i"), EventToken("token-$i"))
        }
        userEventRepository.setGetUserEventsResult(
            createUserEventList(lastEventId = eventId, itemsUpdated = items)
        )

        instance.invoke(USER_ID)

        assertThat(itemRepository.getRefreshItemMemory()).hasSize(30)
    }

    @Test
    fun `shares created - all shares are recreated`() = runTest {
        val eventId = UserEventId(EVENT_ID_1)
        setupBasicSync(eventId)

        val shares = listOf(
            SyncEventShare(ShareId(SHARE_ID_1), EventToken(TOKEN_1)),
            SyncEventShare(ShareId(SHARE_ID_2), EventToken(TOKEN_2))
        )
        userEventRepository.setGetUserEventsResult(
            createUserEventList(lastEventId = eventId, sharesCreated = shares)
        )

        instance.invoke(USER_ID)

        val recreated = shareRepository.getRecreateShareMemory()
        assertThat(recreated).hasSize(2)
        assertThat(recreated.map { it.shareId }).containsExactly(ShareId(SHARE_ID_1), ShareId(SHARE_ID_2))
    }

    @Test
    fun `shares updated - all shares are refreshed with correct ids`() = runTest {
        val eventId = UserEventId(EVENT_ID_1)
        setupBasicSync(eventId)

        val shares = listOf(
            SyncEventShare(ShareId(SHARE_ID_1), EventToken(TOKEN_1)),
            SyncEventShare(ShareId(SHARE_ID_2), EventToken(TOKEN_2))
        )
        userEventRepository.setGetUserEventsResult(
            createUserEventList(lastEventId = eventId, sharesUpdated = shares)
        )

        instance.invoke(USER_ID)

        val refreshed = shareRepository.refreshShareMemory()
        assertThat(refreshed).hasSize(2)
        assertThat(refreshed.map { it.shareId }).containsExactly(ShareId(SHARE_ID_1), ShareId(SHARE_ID_2))
    }

    private fun setupBasicSync(initialEventId: UserEventId) {
        val localEventId = UserEventId("${initialEventId.id}-local")
        val remoteEventId = UserEventId("${initialEventId.id}-remote")
        userEventRepository.setGetLatestEventIdFlow(localEventId)
        userEventRepository.setFetchLatestEventIdResult(remoteEventId)
    }

    private fun createUserEventList(
        lastEventId: UserEventId,
        itemsUpdated: List<SyncEventShareItem> = emptyList(),
        itemsDeleted: List<SyncEventShareItem> = emptyList(),
        aliasNoteChanged: List<SyncEventShareItem> = emptyList(),
        sharesCreated: List<SyncEventShare> = emptyList(),
        sharesUpdated: List<SyncEventShare> = emptyList(),
        sharesDeleted: List<SyncEventShare> = emptyList(),
        foldersUpdated: List<SyncEventShareFolder> = emptyList(),
        foldersDeleted: List<SyncEventShareFolder> = emptyList(),
        invitesChanged: SyncEventInvitesChanged? = null,
        groupInvitesChanged: SyncEventInvitesChanged? = null,
        pendingAliasToCreateChanged: SyncEventInvitesChanged? = null,
        breachUpdate: SyncEventInvitesChanged? = null,
        organizationInfoChanged: SyncEventInvitesChanged? = null,
        sharesWithInvitesToCreate: List<SyncEventShare> = emptyList(),
        planChanged: Boolean = false,
        eventsPending: Boolean = false,
        fullRefresh: Boolean = false
    ): UserEventList = UserEventList(
        lastEventId = lastEventId,
        itemsUpdated = itemsUpdated,
        itemsDeleted = itemsDeleted,
        aliasNoteChanged = aliasNoteChanged,
        sharesCreated = sharesCreated,
        sharesUpdated = sharesUpdated,
        sharesDeleted = sharesDeleted,
        foldersUpdated = foldersUpdated,
        foldersDeleted = foldersDeleted,
        invitesChanged = invitesChanged,
        groupInvitesChanged = groupInvitesChanged,
        pendingAliasToCreateChanged = pendingAliasToCreateChanged,
        sharesWithInvitesToCreate = sharesWithInvitesToCreate,
        breachUpdate = breachUpdate,
        organizationInfoChanged = organizationInfoChanged,
        refreshUser = planChanged,
        eventsPending = eventsPending,
        fullRefresh = fullRefresh
    )


    companion object {
        private val USER_ID = UserId("test-user-id")

        // Event IDs
        private const val EVENT_ID_1 = "event-1"
        private const val EVENT_ID_2 = "event-2"
        private const val EVENT_ID_3 = "event-3"

        // Share IDs
        private const val SHARE_ID_1 = "share-1"
        private const val SHARE_ID_2 = "share-2"
        private const val FOLDER_ID_1 = "folder-1"

        // Item IDs
        private const val ITEM_ID_1 = "item-1"
        private const val ITEM_ID_2 = "item-2"
        private const val ITEM_ID_3 = "item-3"

        // Event Tokens
        private const val TOKEN_1 = "token-1"
        private const val TOKEN_2 = "token-2"
        private const val TOKEN_3 = "token-3"
    }

}
