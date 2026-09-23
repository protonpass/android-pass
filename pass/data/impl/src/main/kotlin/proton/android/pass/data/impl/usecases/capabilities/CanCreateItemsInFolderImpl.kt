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

package proton.android.pass.data.impl.usecases.capabilities

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import proton.android.pass.data.api.errors.ShareNotAvailableError
import proton.android.pass.data.api.usecases.capabilities.CanCreateFolder
import proton.android.pass.data.api.usecases.capabilities.CanCreateItemsInFolder
import proton.android.pass.data.api.usecases.shares.ObserveShare
import proton.android.pass.domain.Share
import proton.android.pass.domain.ShareId
import javax.inject.Inject

class CanCreateItemsInFolderImpl @Inject constructor(
    private val canCreateFolder: CanCreateFolder,
    private val observeShare: ObserveShare
) : CanCreateItemsInFolder {

    override fun invoke(shareId: ShareId): Flow<Boolean> = combine(
        canCreateFolder(shareId),
        observeShare(shareId = shareId)
    ) { canCreateFolderResult, share ->
        share is Share.Vault && canCreateFolderResult.planAllows
    }.catch { error ->
        if (error is ShareNotAvailableError) emit(false) else throw error
    }
}
