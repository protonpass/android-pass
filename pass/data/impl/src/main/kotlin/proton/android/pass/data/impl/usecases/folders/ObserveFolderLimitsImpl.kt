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

package proton.android.pass.data.impl.usecases.folders

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.data.api.repositories.UserAccessDataRepository
import proton.android.pass.data.api.usecases.folders.FolderLimitsData
import proton.android.pass.data.api.usecases.folders.ObserveFolderLimits
import proton.android.pass.domain.FolderLimits
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObserveFolderLimitsImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val userAccessDataRepository: UserAccessDataRepository
) : ObserveFolderLimits {

    override fun invoke(): Flow<FolderLimitsData> = accountManager.getPrimaryUserId()
        .filterNotNull()
        .flatMapLatest { userId ->
            userAccessDataRepository.observe(userId).map { userAccessData ->
                FolderLimitsData(
                    maxCount = userAccessData?.folderMaxCount ?: FolderLimits.MAX_FOLDERS_PER_VAULT,
                    maxChildren = userAccessData?.folderMaxChildren ?: FolderLimits.MAX_FOLDER_WIDTH,
                    maxDepth = userAccessData?.folderMaxDepth ?: FolderLimits.MAX_FOLDER_DEPTH
                )
            }
        }
}
