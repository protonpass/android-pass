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
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import proton.android.pass.data.api.usecases.folders.ObserveFolderItemCount
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ShareId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeObserveFolderItemCount @Inject constructor() : ObserveFolderItemCount {

    private val resultFlow = MutableStateFlow<Result<Int>?>(null)

    var lastShareId: ShareId? = null
        private set
    var lastFolderId: FolderId? = null
        private set
    var lastItemState: ItemState? = null
        private set

    fun sendResult(result: Result<Int>) {
        resultFlow.value = result
    }

    override fun invoke(
        shareId: ShareId,
        folderId: FolderId,
        itemState: ItemState?
    ): Flow<Int> {
        lastShareId = shareId
        lastFolderId = folderId
        lastItemState = itemState
        return resultFlow.filterNotNull().map { it.getOrThrow() }
    }
}
