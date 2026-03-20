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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.GroupInviteRepository
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.repositories.ShareMembersRepository
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.repositories.UserInviteRepository
import proton.android.pass.data.api.usecases.AcceptInvite
import proton.android.pass.data.api.usecases.AcceptInviteStatus
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.folders.RefreshFolders
import proton.android.pass.data.api.work.WorkerItem
import proton.android.pass.data.api.work.WorkerLauncher
import proton.android.pass.data.impl.repositories.FetchShareItemsStatus
import proton.android.pass.data.impl.repositories.FetchShareItemsStatusRepository
import proton.android.pass.domain.InviteId
import proton.android.pass.domain.InviteToken
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.PendingInvite
import proton.android.pass.domain.ShareId
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.InviteNotificationModel
import proton.android.pass.notifications.api.NotificationManager
import javax.inject.Inject

class AcceptInviteImpl @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUser,
    private val shareRepository: ShareRepository,
    private val shareMembersRepository: ShareMembersRepository,
    private val userInviteRepository: UserInviteRepository,
    private val groupInviteRepository: GroupInviteRepository,
    private val workerLauncher: WorkerLauncher,
    private val fetchShareItemsStatusRepository: FetchShareItemsStatusRepository,
    private val notificationManager: NotificationManager,
    private val itemRepository: ItemRepository,
    private val refreshFolders: RefreshFolders
) : AcceptInvite {

    override fun invoke(inviteToken: InviteToken): Flow<AcceptInviteStatus> = observeCurrentUser()
        .flatMapLatest { user ->
            userInviteRepository.getInvite(user.userId, inviteToken).value()
                ?.let { pendingInvite ->
                    val (shareId, itemId) = userInviteRepository.acceptInvite(
                        user.userId,
                        inviteToken
                    )
                    notificationManager.removeReceivedInviteNotification(pendingInvite.toRemoveModel())
                    shareRepository.recreateShare(user.userId, shareId)
                    val updatedCount = shareMembersRepository.getShareMembersTotal(user.userId, shareId)
                    shareRepository.updateMembersCount(user.userId, shareId, updatedCount)
                    downloadItems(user.userId, pendingInvite, shareId, itemId)
                }
                ?: flowOf(AcceptInviteStatus.Error)
        }
        .onStart { emit(AcceptInviteStatus.AcceptingInvite) }

    override fun invoke(inviteId: InviteId): Flow<AcceptInviteStatus> = observeCurrentUser()
        .flatMapLatest { user ->
            val pendingInvite =
                groupInviteRepository.observePendingGroupInvite(user.userId, inviteId)
                    .first()
            if (pendingInvite != null) {
                groupInviteRepository.acceptGroupInvite(
                    userId = user.userId,
                    inviteId = inviteId,
                    inviteToken = pendingInvite.inviteToken
                )
                notificationManager.removeReceivedInviteNotification(pendingInvite.toRemoveModel())
                flowOf(AcceptInviteStatus.GroupInviteDone)
            } else {
                flowOf(AcceptInviteStatus.Error)
            }
        }
        .onStart { emit(AcceptInviteStatus.AcceptingInvite) }

    private fun downloadItems(
        userId: UserId,
        pendingInvite: PendingInvite,
        shareId: ShareId,
        itemId: ItemId
    ): Flow<AcceptInviteStatus> = when (pendingInvite) {
        is PendingInvite.UserItem,
        is PendingInvite.GroupItem -> downloadItemsSynchronously(userId, shareId, itemId)

        is PendingInvite.UserVault,
        is PendingInvite.GroupVault -> downloadItemsUsingWorker(userId, shareId, itemId)
    }

    private fun downloadItemsUsingWorker(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Flow<AcceptInviteStatus> {
        workerLauncher.launch(WorkerItem.FetchShareItems(userId, shareId))
        return fetchShareItemsStatusRepository.observe(shareId)
            .map { fetchShareItemsStatus ->
                when (fetchShareItemsStatus) {
                    is FetchShareItemsStatus.NotStarted -> AcceptInviteStatus.AcceptingInvite
                    is FetchShareItemsStatus.Syncing -> AcceptInviteStatus.DownloadingItems(
                        downloaded = fetchShareItemsStatus.current,
                        total = fetchShareItemsStatus.total
                    )

                    is FetchShareItemsStatus.Done -> {
                        fetchShareItemsStatusRepository.clear(shareId)
                        AcceptInviteStatus.UserInviteDone(
                            items = fetchShareItemsStatus.items,
                            shareId = shareId,
                            itemId = itemId
                        )
                    }
                }
            }
    }

    private fun downloadItemsSynchronously(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Flow<AcceptInviteStatus> = flow {
        runCatching {
            refreshFolders(userId, setOf(shareId))
            val itemRevisions = itemRepository.downloadItemsAndObserveProgress(
                userId = userId,
                shareId = shareId
            ) { progress ->
                emit(
                    AcceptInviteStatus.DownloadingItems(
                        downloaded = progress.current,
                        total = progress.total
                    )
                )
            }
            itemRepository.setShareItems(
                userId = userId,
                items = mapOf(shareId to itemRevisions),
                onProgress = {}
            )
            itemRepository.getById(
                userId = userId,
                shareId = shareId,
                itemId = itemId
            )
            emit(
                AcceptInviteStatus.UserInviteDone(
                    items = itemRevisions.size,
                    shareId = shareId,
                    itemId = itemId
                )
            )
        }.getOrElse {
            PassLogger.w(TAG, "Could not accept item invite")
            PassLogger.w(TAG, it)
            emit(AcceptInviteStatus.Error)
        }
    }

    companion object {
        private const val TAG = "AcceptInviteImpl"
    }
}

fun PendingInvite.toRemoveModel(): InviteNotificationModel = when (this) {
    is PendingInvite.UserItem -> InviteNotificationModel.UserItem(inviterEmail)
    is PendingInvite.UserVault -> InviteNotificationModel.UserVault(inviterEmail)
    is PendingInvite.GroupItem -> InviteNotificationModel.GroupItem(inviterEmail, invitedEmail)
    is PendingInvite.GroupVault -> InviteNotificationModel.GroupVault(inviterEmail, invitedEmail)
}
