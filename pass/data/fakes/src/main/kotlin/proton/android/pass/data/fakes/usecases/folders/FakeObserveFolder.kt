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

package proton.android.pass.data.fakes.usecases.folders

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.usecases.folders.ObserveFolder
import proton.android.pass.domain.Folder
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeObserveFolder @Inject constructor() : ObserveFolder {

    private data class Key(
        val userId: UserId,
        val shareId: ShareId,
        val folderId: FolderId
    )

    private var defaultResult: Result<Folder?> = Result.success(null)
    private val observeFolderFlows = mutableMapOf<Key, MutableStateFlow<Result<Folder?>>>()
    private val invocationCounts = mutableMapOf<Key, Int>()

    fun sendResult(result: Result<Folder?>): Boolean {
        defaultResult = result
        observeFolderFlows.values.forEach { it.value = result }
        return true
    }

    fun sendResult(
        userId: UserId,
        shareId: ShareId,
        folderId: FolderId,
        result: Result<Folder?>
    ): Boolean {
        flowFor(Key(userId, shareId, folderId)).value = result
        return true
    }

    fun invocationCount(
        userId: UserId,
        shareId: ShareId,
        folderId: FolderId
    ): Int = invocationCounts[Key(userId, shareId, folderId)] ?: 0

    private fun flowFor(key: Key): MutableStateFlow<Result<Folder?>> = observeFolderFlows.getOrPut(key) {
        MutableStateFlow(defaultResult)
    }

    override fun invoke(
        userId: UserId,
        shareId: ShareId,
        folderId: FolderId
    ): Flow<Folder?> {
        val key = Key(userId, shareId, folderId)
        invocationCounts[key] = (invocationCounts[key] ?: 0) + 1
        return flowFor(key).map { it.getOrThrow() }
    }
}
