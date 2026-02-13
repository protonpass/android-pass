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

package proton.android.pass.data.impl.fakes

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.repositories.FolderKeyRepository
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.key.FolderKey

class FakeFolderKeyRepository : FolderKeyRepository {

    private var getFolderKeyResult: Result<FolderKey?> = Result.success(null)
    private var getFolderKeysResult: Result<Map<FolderId, FolderKey>> = Result.success(emptyMap())

    fun setGetFolderKeyResult(result: Result<FolderKey?>) {
        getFolderKeyResult = result
    }

    fun setGetFolderKeysResult(result: Result<Map<FolderId, FolderKey>>) {
        getFolderKeysResult = result
    }

    override suspend fun getFolderKey(
        userId: UserId,
        shareId: ShareId,
        folderId: FolderId
    ): FolderKey? = getFolderKeyResult.getOrThrow()

    override suspend fun getFolderKeys(
        userId: UserId,
        shareId: ShareId,
        folderIds: List<FolderId>
    ): Map<FolderId, FolderKey> = getFolderKeysResult.getOrThrow()
}
