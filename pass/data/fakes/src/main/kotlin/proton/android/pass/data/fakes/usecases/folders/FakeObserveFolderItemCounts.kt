/*
 * Copyright (c) 2025-2026 Proton AG
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

package proton.android.pass.data.fakes.usecases.folders

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.usecases.folders.ObserveFolderItemCounts
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeObserveFolderItemCounts @Inject constructor() : ObserveFolderItemCounts {

    private data class Key(val userId: UserId, val shareId: ShareId)

    private var defaultResult: Result<Map<FolderId, Long>> = Result.success(emptyMap())
    private val observeFolderItemCountsFlows = mutableMapOf<Key, MutableStateFlow<Result<Map<FolderId, Long>>>>()
    private val invocationCounts = mutableMapOf<Key, Int>()

    fun sendResult(result: Result<Map<FolderId, Long>>): Boolean {
        defaultResult = result
        observeFolderItemCountsFlows.values.forEach { it.value = result }
        return true
    }

    fun sendResult(
        userId: UserId,
        shareId: ShareId,
        result: Result<Map<FolderId, Long>>
    ): Boolean {
        flowFor(Key(userId, shareId)).value = result
        return true
    }

    fun invocationCount(userId: UserId, shareId: ShareId): Int = invocationCounts[Key(userId, shareId)] ?: 0

    override fun invoke(userId: UserId, shareId: ShareId): Flow<Map<FolderId, Long>> {
        val key = Key(userId, shareId)
        invocationCounts[key] = (invocationCounts[key] ?: 0) + 1
        return flowFor(key).map { it.getOrThrow() }
    }

    private fun flowFor(key: Key): MutableStateFlow<Result<Map<FolderId, Long>>> =
        observeFolderItemCountsFlows.getOrPut(key) { MutableStateFlow(defaultResult) }
}
