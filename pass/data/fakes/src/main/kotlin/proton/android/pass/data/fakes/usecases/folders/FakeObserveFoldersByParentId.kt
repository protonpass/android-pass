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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.usecases.folders.ObserveFoldersByParentId
import proton.android.pass.domain.Folder
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeObserveFoldersByParentId @Inject constructor() : ObserveFoldersByParentId {

    private data class Key(
        val userId: UserId?,
        val shareId: ShareId,
        val parentFolderId: FolderId?,
        val allFolders: Boolean
    )

    private var currentDefault: Result<List<Folder>>? = Result.success(emptyList())
    private val observeFoldersFlows = mutableMapOf<Key, MutableSharedFlow<Result<List<Folder>>>>()
    private val invocationCounts = mutableMapOf<Key, Int>()

    constructor(defaultResult: Result<List<Folder>>?) : this() {
        currentDefault = defaultResult
    }

    fun sendResult(result: Result<List<Folder>>): Boolean {
        currentDefault = result
        observeFoldersFlows.values.forEach { it.tryEmit(result) }
        return true
    }

    fun sendResult(
        shareId: ShareId,
        parentFolderId: FolderId?,
        result: Result<List<Folder>>
    ): Boolean {
        flowFor(Key(null, shareId, parentFolderId, allFolders = false)).tryEmit(result)
        return true
    }

    fun sendResult(
        userId: UserId,
        shareId: ShareId,
        parentFolderId: FolderId?,
        result: Result<List<Folder>>
    ): Boolean {
        flowFor(Key(userId, shareId, parentFolderId, allFolders = false)).tryEmit(result)
        return true
    }

    fun sendResult(
        userId: UserId,
        shareId: ShareId,
        result: Result<List<Folder>>
    ): Boolean {
        flowFor(Key(userId, shareId, parentFolderId = null, allFolders = true)).tryEmit(result)
        return true
    }

    fun sendResult(shareId: ShareId, result: Result<List<Folder>>): Boolean {
        flowFor(Key(null, shareId, parentFolderId = null, allFolders = true)).tryEmit(result)
        return true
    }

    fun invocationCount(
        userId: UserId,
        shareId: ShareId,
        parentFolderId: FolderId? = null
    ): Int = invocationCounts[Key(userId, shareId, parentFolderId, allFolders = false)] ?: 0

    fun invocationCount(userId: UserId, shareId: ShareId): Int =
        invocationCounts[Key(userId, shareId, parentFolderId = null, allFolders = true)] ?: 0

    private fun flowFor(key: Key): MutableSharedFlow<Result<List<Folder>>> = observeFoldersFlows.getOrPut(key) {
        MutableSharedFlow<Result<List<Folder>>>(replay = 1).also { flow ->
            currentDefault?.let { flow.tryEmit(it) }
        }
    }

    override fun invoke(
        userId: UserId,
        shareId: ShareId,
        parentFolderId: FolderId?
    ): Flow<List<Folder>> {
        val key = Key(userId, shareId, parentFolderId, allFolders = false)
        invocationCounts[key] = (invocationCounts[key] ?: 0) + 1
        return flowFor(key).map { it.getOrThrow() }
    }

    override fun invoke(userId: UserId, shareId: ShareId): Flow<List<Folder>> {
        val key = Key(userId, shareId, parentFolderId = null, allFolders = true)
        invocationCounts[key] = (invocationCounts[key] ?: 0) + 1
        return flowFor(key).map { it.getOrThrow() }
    }

    override fun invoke(shareId: ShareId, parentFolderId: FolderId?): Flow<List<Folder>> {
        val key = Key(null, shareId, parentFolderId, allFolders = false)
        invocationCounts[key] = (invocationCounts[key] ?: 0) + 1
        return flowFor(key).map { it.getOrThrow() }
    }

    override fun invoke(shareId: ShareId): Flow<List<Folder>> {
        val key = Key(null, shareId, parentFolderId = null, allFolders = true)
        invocationCounts[key] = (invocationCounts[key] ?: 0) + 1
        return flowFor(key).map { it.getOrThrow() }
    }
}
