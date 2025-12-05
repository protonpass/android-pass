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

package proton.android.pass.data.impl.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.repositories.UserEventRepository
import proton.android.pass.domain.UserEventId
import proton.android.pass.domain.events.UserEventList

class FakeUserEventRepository : UserEventRepository {

    private var fetchLatestEventIdResult: UserEventId = UserEventId("event-1")
    private var getUserEventsResult: UserEventList = createEmptyUserEventList()
    private val getLatestEventIdFlow: MutableStateFlow<UserEventId?> = MutableStateFlow(null)
    private val storeLatestEventIdMemory: MutableList<Pair<UserId, UserEventId>> = mutableListOf()

    fun setFetchLatestEventIdResult(value: UserEventId) {
        fetchLatestEventIdResult = value
    }

    fun setGetUserEventsResult(value: UserEventList) {
        getUserEventsResult = value
    }

    fun setGetLatestEventIdFlow(value: UserEventId?) {
        getLatestEventIdFlow.value = value
    }

    fun getStoreLatestEventIdMemory(): List<Pair<UserId, UserEventId>> = storeLatestEventIdMemory.toList()

    fun clearStoreLatestEventIdMemory() {
        storeLatestEventIdMemory.clear()
    }

    override suspend fun fetchLatestEventId(userId: UserId): UserEventId = fetchLatestEventIdResult

    override suspend fun getUserEvents(userId: UserId, eventId: UserEventId): UserEventList = getUserEventsResult

    override fun getLatestEventId(userId: UserId): Flow<UserEventId?> = getLatestEventIdFlow

    override suspend fun storeLatestEventId(userId: UserId, eventId: UserEventId) {
        storeLatestEventIdMemory.add(Pair(userId, eventId))
    }

    private fun createEmptyUserEventList(): UserEventList {
        return UserEventList(
            lastEventId = UserEventId("event-1"),
            itemsUpdated = emptyList(),
            itemsDeleted = emptyList(),
            aliasNoteChanged = emptyList(),
            sharesCreated = emptyList(),
            sharesUpdated = emptyList(),
            sharesDeleted = emptyList(),
            foldersUpdated = emptyList(),
            foldersDeleted = emptyList(),
            invitesChanged = null,
            groupInvitesChanged = null,
            pendingAliasToCreateChanged = null,
            breachUpdate = null,
            sharesWithInvitesToCreate = emptyList(),
            refreshUser = false,
            eventsPending = false,
            fullRefresh = false
        )
    }
}
