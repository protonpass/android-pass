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

package proton.android.pass.data.fakes.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import proton.android.pass.data.api.usecases.capabilities.CanCreateFolder
import proton.android.pass.data.api.usecases.capabilities.CanCreateFolderResult
import proton.android.pass.domain.ShareId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeCanCreateFolder @Inject constructor() : CanCreateFolder {

    private val resultFlow: MutableStateFlow<CanCreateFolderResult> =
        MutableStateFlow(CanCreateFolderResult(roleAllows = true, planAllows = true))

    // Convenience: true → both allow; false → role allows, plan blocks (upsell scenario)
    fun sendValue(value: Boolean) {
        resultFlow.tryEmit(CanCreateFolderResult(roleAllows = true, planAllows = value))
    }

    fun sendValue(value: CanCreateFolderResult) {
        resultFlow.tryEmit(value)
    }

    override fun invoke(shareId: ShareId): Flow<CanCreateFolderResult> = resultFlow

    override fun invoke(shareIds: List<ShareId>): Flow<Map<ShareId, CanCreateFolderResult>> =
        resultFlow.map { result -> shareIds.associateWith { result } }
}
