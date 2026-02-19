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

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.usecases.folders.GetFolderHierarchy
import proton.android.pass.domain.Folder
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.VaultId
import proton.android.pass.test.domain.FolderTestFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeGetFolderHierarchy @Inject constructor() : GetFolderHierarchy {

    private var result: Result<List<Folder>> = Result.success(
        listOf(
            FolderTestFactory.create(
                userId = UserId("fake-user-id"),
                shareId = ShareId("fake-share-id"),
                vaultId = VaultId("fake-vault-id"),
                folderId = FolderId("fake-folder-id"),
                folderKey = "fake-folder-key",
                name = "Fake Folder"
            )
        )
    )

    fun setResult(result: Result<List<Folder>>) {
        this.result = result
    }

    override suspend fun invoke(shareId: ShareId, itemId: ItemId): List<Folder> = result.getOrThrow()
}
