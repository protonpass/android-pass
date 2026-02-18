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

package proton.android.pass.data.impl.usecases.folders

import kotlinx.coroutines.flow.first
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.data.api.repositories.FolderRepository
import proton.android.pass.data.api.usecases.folders.CreateFolder
import proton.android.pass.domain.Folder
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId
import javax.inject.Inject

class CreateFolderImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val folderRepository: FolderRepository
) : CreateFolder {

    override suspend fun invoke(
        shareId: ShareId,
        parentFolderId: FolderId?,
        title: String
    ): Folder {
        val userId = requireNotNull(accountManager.getPrimaryUserId().first())
        return folderRepository.createFolder(userId, shareId, parentFolderId, title)
    }
}
