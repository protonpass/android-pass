/*
 * Copyright (c) 2024-2025 Proton AG
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

package proton.android.pass.features.item.details.detail.presentation.handlers

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldType
import proton.android.pass.commonpresentation.api.items.details.handlers.ItemDetailsHandlerObserver
import proton.android.pass.domain.toItemContents
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.commonuimodels.api.items.ItemDetailState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemSection
import proton.android.pass.domain.ItemDiffs
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.Share
import proton.android.pass.domain.attachments.Attachment
import javax.inject.Inject

class NoteItemDetailsHandlerObserverImpl @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider
) : ItemDetailsHandlerObserver<ItemContents.Note>() {

    override fun observe(
        share: Share,
        item: Item,
        attachmentsState: AttachmentsState
    ): Flow<ItemDetailState> = observeNoteItemContents(item)
        .mapLatest { noteItemContents ->
            ItemDetailState.Note(
                itemContents = noteItemContents,
                itemId = item.id,
                shareId = item.shareId,
                isItemPinned = item.isPinned,
                itemShare = share,
                itemCreatedAt = item.createTime,
                itemModifiedAt = item.modificationTime,
                itemLastAutofillAtOption = item.lastAutofillTime,
                itemRevision = item.revision,
                itemState = ItemState.from(item.state),
                itemDiffs = ItemDiffs.Note(),
                itemShareCount = item.shareCount,
                attachmentsState = attachmentsState
            )
        }

    private fun observeNoteItemContents(item: Item): Flow<ItemContents.Note> = flow {
        encryptionContextProvider.withEncryptionContext {
            item.toItemContents<ItemContents.Note> { decrypt(it) }
        }.let { noteItemContents ->
            emit(noteItemContents)
        }
    }

    override fun updateHiddenFieldsContents(
        itemContents: ItemContents.Note,
        revealedHiddenFields: Map<ItemSection, Set<ItemDetailsFieldType.Hidden>>
    ): ItemContents = itemContents

    override fun calculateItemDiffs(
        baseItemContents: ItemContents.Note,
        otherItemContents: ItemContents.Note,
        baseAttachments: List<Attachment>,
        otherAttachments: List<Attachment>
    ): ItemDiffs = ItemDiffs.Note(
        title = calculateItemDiffType(
            baseItemFieldValue = baseItemContents.title,
            otherItemFieldValue = otherItemContents.title
        ),
        note = calculateItemDiffType(
            baseItemFieldValue = baseItemContents.note,
            otherItemFieldValue = otherItemContents.note
        ),
        attachments = calculateItemDiffType(
            baseItemAttachments = baseAttachments,
            otherItemAttachments = otherAttachments
        )
    )

}
