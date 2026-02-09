/*
 * Copyright (c) 2025 Proton AG
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
import proton.android.pass.data.api.usecases.folders.ObserveFolders
import proton.android.pass.domain.Folder
import proton.android.pass.domain.ShareId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeObserveFolders @Inject constructor() : ObserveFolders {

    private data class Key(val userId: UserId, val shareId: ShareId)

    private var defaultResult: Result<List<Folder>> = Result.success(emptyList())
    private val observeFoldersFlows = mutableMapOf<Key, MutableStateFlow<Result<List<Folder>>>>()
    private val invocationCounts = mutableMapOf<Key, Int>()

    fun sendResult(result: Result<List<Folder>>): Boolean {
        defaultResult = result
        observeFoldersFlows.values.forEach { it.value = result }
        return true
    }

    fun sendResult(
        userId: UserId,
        shareId: ShareId,
        result: Result<List<Folder>>
    ): Boolean {
        flowFor(Key(userId, shareId)).value = result
        return true
    }

    fun invocationCount(userId: UserId, shareId: ShareId): Int = invocationCounts[Key(userId, shareId)] ?: 0

    private fun flowFor(key: Key): MutableStateFlow<Result<List<Folder>>> = observeFoldersFlows.getOrPut(key) {
        MutableStateFlow(defaultResult)
    }

    override fun invoke(userId: UserId, shareId: ShareId): Flow<List<Folder>> {
        val key = Key(userId, shareId)
        invocationCounts[key] = (invocationCounts[key] ?: 0) + 1
        return flowFor(key).map { it.getOrThrow() }
    }
}
