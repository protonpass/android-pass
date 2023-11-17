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

package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import proton.android.pass.domain.ShareId
import javax.inject.Inject
import javax.inject.Singleton

sealed interface FetchShareItemsStatus {
    object NotStarted : FetchShareItemsStatus
    data class Syncing(
        val current: Int,
        val total: Int,
    ) : FetchShareItemsStatus

    @JvmInline
    value class Done(val items: Int) : FetchShareItemsStatus
}

interface FetchShareItemsStatusRepository {
    fun emit(shareId: ShareId, status: FetchShareItemsStatus)
    fun observe(shareId: ShareId): Flow<FetchShareItemsStatus>
    fun clear(shareId: ShareId)
    fun clearAll()
}

@Singleton
class FetchShareItemsStatusRepositoryImpl @Inject constructor() : FetchShareItemsStatusRepository {
    private val syncStatus: MutableStateFlow<Map<ShareId, FetchShareItemsStatus>> =
        MutableStateFlow(mutableMapOf())

    override fun emit(shareId: ShareId, status: FetchShareItemsStatus) {
        syncStatus.update {
            it.toMutableMap().apply {
                this[shareId] = status
            }
        }
    }

    override fun clear(shareId: ShareId) {
        syncStatus.update {
            it.toMutableMap().apply {
                this.remove(shareId)
            }
        }
    }

    override fun clearAll() {
        syncStatus.update {
            it.toMutableMap().apply {
                this.clear()
            }
        }
    }

    override fun observe(shareId: ShareId): Flow<FetchShareItemsStatus> = syncStatus
        .map {
            it[shareId] ?: FetchShareItemsStatus.NotStarted
        }
}
