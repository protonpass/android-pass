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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.FolderRepository
import proton.android.pass.data.api.usecases.folders.ObserveFolders
import proton.android.pass.domain.Folder
import proton.android.pass.domain.ShareId
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import javax.inject.Inject

class ObserveFoldersImpl @Inject constructor(
    private val featureFlagsPreferencesRepository: FeatureFlagsPreferencesRepository,
    private val folderRepository: FolderRepository
) : ObserveFolders {

    override fun invoke(userId: UserId, shareId: ShareId): Flow<List<Folder>> =
        featureFlagsPreferencesRepository.get<Boolean>(FeatureFlag.PASS_FOLDERS)
            .flatMapLatest { isEnabled ->
                if (isEnabled) {
                    folderRepository.observeFolders(userId, shareId)
                } else {
                    flowOf(emptyList())
                }
            }
}
