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
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.AliasItemsChangeStatusResult
import proton.android.pass.data.api.repositories.AliasRepository
import proton.android.pass.domain.AliasDetails
import proton.android.pass.domain.AliasMailbox
import proton.android.pass.domain.AliasOptions
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.events.EventToken
import javax.inject.Inject

@Suppress("NotImplementedDeclaration")
class FakeAliasRepository @Inject constructor() : AliasRepository {

    override fun getAliasOptions(userId: UserId, shareId: ShareId): Flow<AliasOptions> {
        TODO("Not yet implemented")
    }

    override fun observeAliasDetails(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        eventToken: EventToken?
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

    override suspend fun changeAliasStatus(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        enable: Boolean
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun changeAliasStatus(
        userId: UserId,
        items: List<Pair<ShareId, ItemId>>,
        enabled: Boolean
    ): AliasItemsChangeStatusResult {
        TODO("Not yet implemented")
    }

    override suspend fun updateAliasName(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        name: String
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun updateAliasNote(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        note: String
    ) = Unit

}
