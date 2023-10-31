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
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.InviteRepository
import proton.android.pass.data.api.usecases.AcceptInvite
import proton.android.pass.data.api.usecases.AcceptInviteStatus
import proton.android.pass.data.impl.repositories.FetchShareItemsStatus
import proton.android.pass.data.impl.repositories.FetchShareItemsStatusRepository
import proton.android.pass.data.impl.work.FetchShareItemsWorker
import proton.pass.domain.InviteToken
import proton.pass.domain.ShareId
import javax.inject.Inject

class AcceptInviteImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val inviteRepository: InviteRepository,
    private val workManager: WorkManager,
    private val fetchShareItemsStatusRepository: FetchShareItemsStatusRepository

) : AcceptInvite {
    override fun invoke(invite: InviteToken): Flow<AcceptInviteStatus> = accountManager
        .getPrimaryUserId()
        .filterNotNull()
        .flatMapLatest { userId ->
            val shareId = inviteRepository.acceptInvite(userId, invite)
            downloadItems(userId, shareId)
        }
        .onStart { emit(AcceptInviteStatus.AcceptingInvite) }

    private fun downloadItems(userId: UserId, shareId: ShareId): Flow<AcceptInviteStatus> {
        val request = FetchShareItemsWorker.getRequestFor(userId, shareId)
        workManager.enqueue(request)
        return fetchShareItemsStatusRepository.observe(shareId)
            .map {
                when (it) {
                    is FetchShareItemsStatus.NotStarted -> AcceptInviteStatus.AcceptingInvite
                    is FetchShareItemsStatus.Syncing -> AcceptInviteStatus.DownloadingItems(
                        downloaded = it.current,
                        total = it.total
                    )
                    is FetchShareItemsStatus.Done -> {
                        fetchShareItemsStatusRepository.clear(shareId)
                        AcceptInviteStatus.Done(
                            items = it.items,
                            shareId = shareId
                        )
                    }
                }
            }
    }
}
