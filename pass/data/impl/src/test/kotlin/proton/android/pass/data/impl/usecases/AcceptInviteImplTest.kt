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

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Test
import proton.android.pass.data.api.usecases.AcceptInviteStatus
import proton.android.pass.data.fakes.repositories.FakeGroupInviteRepository
import proton.android.pass.data.fakes.repositories.FakeItemRepository
import proton.android.pass.data.fakes.repositories.FakeUserInviteRepository
import proton.android.pass.data.fakes.usecases.FakeObserveCurrentUser
import proton.android.pass.data.fakes.work.FakeWorkerLauncher
import proton.android.pass.data.impl.fakes.FakeShareRepository
import proton.android.pass.data.impl.repositories.FetchShareItemsStatus
import proton.android.pass.data.impl.repositories.FetchShareItemsStatusRepositoryImpl
import proton.android.pass.data.fakes.usecases.folders.FakeRefreshFolders
import proton.android.pass.domain.InviteToken
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareInvite
import proton.android.pass.notifications.fakes.FakeNotificationManager
import proton.android.pass.test.domain.ItemTestFactory
import proton.android.pass.test.domain.PendingInviteTestFactory
import proton.android.pass.test.domain.UserTestFactory

class AcceptInviteImplTest {

    private lateinit var observeCurrentUser: FakeObserveCurrentUser
    private lateinit var shareRepository: FakeShareRepository
    private lateinit var userInviteRepository: FakeUserInviteRepository
    private lateinit var groupInviteRepository: FakeGroupInviteRepository
    private lateinit var workerLauncher: FakeWorkerLauncher
    private lateinit var fetchShareItemsStatusRepository: FetchShareItemsStatusRepositoryImpl
    private lateinit var notificationManager: FakeNotificationManager
    private lateinit var itemRepository: FakeItemRepository
    private lateinit var refreshFolders: FakeRefreshFolders
    private lateinit var instance: AcceptInviteImpl

    @Before
    fun setUp() {
        observeCurrentUser = FakeObserveCurrentUser()
        shareRepository = FakeShareRepository()
        userInviteRepository = FakeUserInviteRepository()
        groupInviteRepository = FakeGroupInviteRepository()
        workerLauncher = FakeWorkerLauncher()
        fetchShareItemsStatusRepository = FetchShareItemsStatusRepositoryImpl()
        notificationManager = FakeNotificationManager()
        itemRepository = FakeItemRepository()
        refreshFolders = FakeRefreshFolders()

        instance = AcceptInviteImpl(
            observeCurrentUser = observeCurrentUser,
            shareRepository = shareRepository,
            userInviteRepository = userInviteRepository,
            groupInviteRepository = groupInviteRepository,
            workerLauncher = workerLauncher,
            fetchShareItemsStatusRepository = fetchShareItemsStatusRepository,
            notificationManager = notificationManager,
            itemRepository = itemRepository,
            refreshFolders = refreshFolders
        )

        observeCurrentUser.sendUser(UserTestFactory.create(userId = USER_ID))
    }

    @Test
    fun `item invite syncs immediately and emits done without launching worker`() = runTest {
        val shareId = ShareId("share-id")
        val itemId = ItemId("item-id")
        userInviteRepository.setInvite(PendingInviteTestFactory.Item.create())
        userInviteRepository.setAcceptResult(Result.success(ShareInvite(shareId, itemId)))
        itemRepository.setItem(
            ItemTestFactory.create(
                shareId = shareId,
                itemId = itemId
            )
        )

        instance(InviteToken("invite-token")).test {
            assertThat(awaitItem()).isEqualTo(AcceptInviteStatus.AcceptingInvite)
            assertThat(awaitItem()).isEqualTo(AcceptInviteStatus.DownloadingItems(downloaded = 0, total = 0))
            assertThat(awaitItem()).isEqualTo(
                AcceptInviteStatus.UserInviteDone(
                    items = 0,
                    shareId = shareId,
                    itemId = itemId
                )
            )
            cancelAndIgnoreRemainingEvents()
        }

        assertThat(workerLauncher.getLaunchedItems()).isEmpty()
        assertThat(refreshFolders.invocations).hasSize(1)
        assertThat(itemRepository.getSetShareItemsMemory()).hasSize(1)
    }

    @Test
    fun `item invite emits error when synced item cannot be loaded locally`() = runTest {
        val shareId = ShareId("share-id")
        val itemId = ItemId("item-id")
        userInviteRepository.setInvite(PendingInviteTestFactory.Item.create())
        userInviteRepository.setAcceptResult(Result.success(ShareInvite(shareId, itemId)))

        instance(InviteToken("invite-token")).test {
            assertThat(awaitItem()).isEqualTo(AcceptInviteStatus.AcceptingInvite)
            assertThat(awaitItem()).isEqualTo(AcceptInviteStatus.DownloadingItems(downloaded = 0, total = 0))
            assertThat(awaitItem()).isEqualTo(AcceptInviteStatus.Error)
            cancelAndIgnoreRemainingEvents()
        }

        assertThat(workerLauncher.getLaunchedItems()).isEmpty()
    }

    @Test
    fun `vault invite uses worker based sync flow`() = runTest {
        val shareId = ShareId("share-id")
        val itemId = ItemId("item-id")
        userInviteRepository.setInvite(PendingInviteTestFactory.Vault.create())
        userInviteRepository.setAcceptResult(Result.success(ShareInvite(shareId, itemId)))

        instance(InviteToken("invite-token")).test {
            assertThat(awaitItem()).isEqualTo(AcceptInviteStatus.AcceptingInvite)
            assertThat(awaitItem()).isEqualTo(AcceptInviteStatus.AcceptingInvite)

            fetchShareItemsStatusRepository.emit(shareId, FetchShareItemsStatus.Done(3))

            assertThat(awaitItem()).isEqualTo(
                AcceptInviteStatus.UserInviteDone(
                    items = 3,
                    shareId = shareId,
                    itemId = itemId
                )
            )
            cancelAndIgnoreRemainingEvents()
        }

        assertThat(workerLauncher.getLaunchedItems()).hasSize(1)
    }

    private companion object {
        private val USER_ID = UserId("user-id")
    }
}
