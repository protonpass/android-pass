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

package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.first
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.asResultWithoutLoading
import proton.android.pass.data.api.repositories.AliasRepository
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.usecases.GetShareById
import proton.android.pass.data.api.usecases.UpdateAlias
import proton.android.pass.data.api.usecases.UpdateAliasContent
import proton.android.pass.data.api.usecases.UpdateAliasItemContent
import proton.android.pass.domain.AliasMailbox
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemContents
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class UpdateAliasImpl @Inject constructor(
    private val aliasRepository: AliasRepository,
    private val itemRepository: ItemRepository,
    private val getShareById: GetShareById
) : UpdateAlias {

    override suspend fun invoke(
        userId: UserId,
        item: Item,
        content: UpdateAliasContent
    ): Item {
        if (content.mailboxes is Some) {
            val mailboxes = (content.mailboxes as Some<List<AliasMailbox>>).value
            runCatching {
                aliasRepository.updateAliasMailboxes(
                    userId,
                    item.shareId,
                    item.id,
                    mailboxes
                )
                    .asResultWithoutLoading()
                    .first()
            }.fold(
                onSuccess = {
                    PassLogger.d(TAG, "Alias mailboxes updated")
                },
                onFailure = {
                    PassLogger.d(TAG, it, "Error updating alias mailboxes")
                    throw it
                }
            )
        }

        if (content.hasSLNote) {
            aliasRepository.updateAliasNote(userId, item.shareId, item.id, content.slNote)
        }

        if (content.hasDisplayName) {
            aliasRepository.updateAliasName(userId, item.shareId, item.id, content.displayName)
        }

        if (content.itemData is Some) {
            val itemData = (content.itemData as Some<UpdateAliasItemContent>).value
            return updateItemContent(userId, item, itemData)
        }

        return item
    }

    private suspend fun updateItemContent(
        userId: UserId,
        item: Item,
        content: UpdateAliasItemContent
    ): Item {
        val share = getShareById(userId, item.shareId)
        val itemContents = ItemContents.Alias(
            title = content.title,
            note = content.note,
            aliasEmail = "" // Not used when creating the payload
        )

        return itemRepository.updateItem(userId, share, item, itemContents)
    }

    private companion object {

        private const val TAG = "UpdateAliasImpl"

    }

}
