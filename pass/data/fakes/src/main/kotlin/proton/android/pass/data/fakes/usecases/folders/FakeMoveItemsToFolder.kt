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

import proton.android.pass.data.api.usecases.folders.MoveItemsToFolder
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeMoveItemsToFolder @Inject constructor() : MoveItemsToFolder {
    data class Invocation(
        val shareId: ShareId,
        val folderId: FolderId,
        val itemIds: List<ItemId>
    )

    private val _invocations = mutableListOf<Invocation>()
    val invocations: List<Invocation> get() = _invocations

    var result: Result<Unit> = Result.success(Unit)

    override suspend fun invoke(
        shareId: ShareId,
        folderId: FolderId,
        itemIds: List<ItemId>
    ) {
        _invocations += Invocation(shareId, folderId, itemIds)
        result.getOrThrow()
    }
}
