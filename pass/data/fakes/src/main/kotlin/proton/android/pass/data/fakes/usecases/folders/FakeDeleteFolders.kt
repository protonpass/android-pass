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

import proton.android.pass.data.api.usecases.folders.DeleteFolders
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeDeleteFolders @Inject constructor() : DeleteFolders {

    private var result: Result<Unit> = Result.success(Unit)

    private val memory = mutableListOf<Payload>()

    fun memory(): List<Payload> = memory

    fun setResult(result: Result<Unit>) {
        this.result = result
    }

    override suspend fun invoke(shareId: ShareId, folderIds: List<FolderId>) {
        memory.add(Payload(shareId, folderIds))
        result.getOrThrow()
    }

    data class Payload(
        val shareId: ShareId,
        val folderIds: List<FolderId>
    )
}
