/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.data.fakes.repositories

import kotlinx.coroutines.flow.Flow
import proton.android.pass.data.api.repositories.AliasRepository
import me.proton.core.domain.entity.UserId
import proton.pass.domain.AliasDetails
import proton.pass.domain.AliasMailbox
import proton.pass.domain.AliasOptions
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import javax.inject.Inject

@Suppress("NotImplementedDeclaration")
class TestAliasRepository @Inject constructor() : AliasRepository {
    override fun getAliasOptions(userId: UserId, shareId: ShareId): Flow<AliasOptions> {
        TODO("Not yet implemented")
    }

    override fun getAliasDetails(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Flow<AliasDetails> {
        TODO("Not yet implemented")
    }

    override fun updateAliasMailboxes(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        mailboxes: List<AliasMailbox>
    ): Flow<Unit> {
        TODO("Not yet implemented")
    }
}
