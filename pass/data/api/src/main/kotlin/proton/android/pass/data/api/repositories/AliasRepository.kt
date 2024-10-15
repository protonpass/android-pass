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

package proton.android.pass.data.api.repositories

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import proton.android.pass.domain.AliasDetails
import proton.android.pass.domain.AliasMailbox
import proton.android.pass.domain.AliasOptions
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.aliascontacts.Contact
import proton.android.pass.domain.aliascontacts.ContactId

interface AliasRepository {
    fun getAliasOptions(userId: UserId, shareId: ShareId): Flow<AliasOptions>
    fun getAliasDetails(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Flow<AliasDetails>

    fun updateAliasMailboxes(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        mailboxes: List<AliasMailbox>
    ): Flow<Unit>

    suspend fun changeAliasStatus(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        enable: Boolean
    )

    suspend fun changeAliasStatus(
        userId: UserId,
        items: List<Pair<ShareId, ItemId>>,
        enabled: Boolean
    ): AliasItemsChangeStatusResult

    suspend fun observeAliasContacts(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Flow<List<Contact>>

    suspend fun observeAliasContact(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        contactId: ContactId
    ): Flow<Contact>

    suspend fun createAliasContact(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        email: String,
        name: String?
    ): Contact

    suspend fun deleteAliasContact(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        contactId: ContactId
    )

    suspend fun updateBlockedAliasContact(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        contactId: ContactId,
        blocked: Boolean
    ): Contact
}


sealed interface AliasItemsChangeStatusResult {
    @JvmInline
    value class AllChanged(val items: List<Pair<ShareId, ItemId>>) : AliasItemsChangeStatusResult

    @JvmInline
    value class SomeChanged(val items: List<Pair<ShareId, ItemId>>) : AliasItemsChangeStatusResult

    @JvmInline
    value class NoneChanged(val exception: Throwable) : AliasItemsChangeStatusResult
}
