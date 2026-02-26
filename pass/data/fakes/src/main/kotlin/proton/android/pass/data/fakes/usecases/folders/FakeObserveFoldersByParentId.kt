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
import proton.android.pass.data.api.usecases.folders.ObserveFoldersByParentId
import proton.android.pass.domain.Folder
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeObserveFoldersByParentId @Inject constructor() : ObserveFoldersByParentId {

    private data class Key(val shareId: ShareId, val parentFolderId: FolderId?)

    private var defaultResult: Result<List<Folder>> = Result.success(emptyList())
    private val observeFoldersFlows = mutableMapOf<Key, MutableStateFlow<Result<List<Folder>>>>()

    fun sendResult(result: Result<List<Folder>>): Boolean {
        defaultResult = result
        observeFoldersFlows.values.forEach { it.value = result }
        return true
    }

    fun sendResult(
        shareId: ShareId,
        parentFolderId: FolderId?,
        result: Result<List<Folder>>
    ): Boolean {
        flowFor(Key(shareId, parentFolderId)).value = result
        return true
    }

    private fun flowFor(key: Key): MutableStateFlow<Result<List<Folder>>> = observeFoldersFlows.getOrPut(key) {
        MutableStateFlow(defaultResult)
    }

    override fun invoke(shareId: ShareId, parentFolderId: FolderId?): Flow<List<Folder>> {
        val key = Key(shareId, parentFolderId)
        return flowFor(key).map { it.getOrThrow() }
    }
}
