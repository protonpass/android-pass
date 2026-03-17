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

package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.first
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.AliasRepository
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.RefreshAliasSlNotes
import javax.inject.Inject

class RefreshAliasSlNotesImpl @Inject constructor(
    private val aliasRepository: AliasRepository,
    private val shareRepository: ShareRepository
) : RefreshAliasSlNotes {

    override suspend fun invoke(userId: UserId) {
        val shares = shareRepository.observeAllShares(userId, includeHidden = true).first()
        aliasRepository.refreshBulkAliasSlNotes(userId, shareIds = shares.map { it.id })
    }
}
