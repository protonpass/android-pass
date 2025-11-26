/*
 * Copyright (c) 2023 Proton AG
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

import androidx.work.WorkManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.GroupInviteRepository
import proton.android.pass.data.api.repositories.UserInviteRepository
import proton.android.pass.data.api.usecases.AcceptInvite
import proton.android.pass.data.api.usecases.AcceptInviteStatus
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.impl.repositories.FetchShareItemsStatus
import proton.android.pass.data.impl.repositories.FetchShareItemsStatusRepository
import proton.android.pass.data.impl.work.FetchShareItemsWorker
import proton.android.pass.domain.InviteId
import proton.android.pass.domain.InviteToken
import proton.android.pass.domain.ShareInvite
import proton.android.pass.notifications.api.NotificationManager
import javax.inject.Inject

class AcceptInviteImpl @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUser,
    private val userInviteRepository: UserInviteRepository,
    private val groupInviteRepository: GroupInviteRepository,
    private val workManager: WorkManager,
    private val fetchShareItemsStatusRepository: FetchShareItemsStatusRepository,
    private val notificationManager: NotificationManager
) : AcceptInvite {

    override fun invoke(inviteToken: InviteToken): Flow<AcceptInviteStatus> = observeCurrentUser()
        .flatMapLatest { user ->
            userInviteRepository.getInvite(user.userId, inviteToken).value()
                ?.let { pendingInvite ->
                    val shareInvite = userInviteRepository.acceptInvite(user.userId, inviteToken)
                    notificationManager.removeReceivedInviteNotification(pendingInvite)
                    downloadItems(user.userId, shareInvite)
                }
                ?: flowOf(AcceptInviteStatus.Error)
        }
        .onStart { emit(AcceptInviteStatus.AcceptingInvite) }

    override fun invoke(inviteId: InviteId): Flow<AcceptInviteStatus> = observeCurrentUser()
        .flatMapLatest { user ->
            val pendingInvite = groupInviteRepository.observePendingGroupInvite(user.userId, inviteId)
                .first()
            if (pendingInvite != null) {
                groupInviteRepository.acceptGroupInvite(
                    userId = user.userId,
                    inviteId = inviteId,
                    inviteToken = pendingInvite.inviteToken
                )
                notificationManager.removeReceivedInviteNotification(pendingInvite)
                flowOf(AcceptInviteStatus.GroupInviteDone)
            } else {
                flowOf(AcceptInviteStatus.Error)
            }
        }
        .onStart { emit(AcceptInviteStatus.AcceptingInvite) }

    private fun downloadItems(userId: UserId, shareInvite: ShareInvite): Flow<AcceptInviteStatus> {
        val (shareId, itemId) = shareInvite
        val request = FetchShareItemsWorker.getRequestFor(userId, shareId)
        workManager.enqueue(request)
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
}
