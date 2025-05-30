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
import kotlinx.coroutines.flow.combine
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldType
import proton.android.pass.commonpresentation.api.items.details.handlers.ItemDetailsHandlerObserver
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.commonuimodels.api.items.DetailEvent
import proton.android.pass.commonuimodels.api.items.ItemDetailState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.CanDisplayTotp
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemDiffs
import proton.android.pass.domain.ItemSection
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.Share
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.totp.api.TotpManager
import javax.inject.Inject

class NoteItemDetailsHandlerObserverImpl @Inject constructor(
    override val encryptionContextProvider: EncryptionContextProvider,
    override val totpManager: TotpManager,
    override val canDisplayTotp: CanDisplayTotp
) : ItemDetailsHandlerObserver<ItemContents.Note, ItemDetailsFieldType.NoteItemAction>(
    encryptionContextProvider = encryptionContextProvider,
    totpManager = totpManager,
    canDisplayTotp = canDisplayTotp
) {

    override fun observe(
        share: Share,
        item: Item,
        attachmentsState: AttachmentsState,
        savedStateEntries: Map<String, Any?>,
        detailEvent: DetailEvent
    ): Flow<ItemDetailState> = combine(
        observeItemContents(item),
        observeCustomFieldTotps(item)
    ) { noteItemContents, customFieldTotps ->
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
            attachmentsState = attachmentsState,
            customFieldTotps = customFieldTotps,
            detailEvent = detailEvent
        )
    }

    override fun updateHiddenFieldsContents(
        itemContents: ItemContents.Note,
        revealedHiddenCopyableFields: Map<ItemSection, Set<ItemDetailsFieldType.HiddenCopyable>>
    ): ItemContents = itemContents.copy(
        customFields = updateHiddenCustomFieldContents(
            customFields = itemContents.customFields,
            revealedHiddenFields = revealedHiddenCopyableFields[ItemSection.CustomField].orEmpty()
        )
    )

    override fun calculateItemDiffs(
        baseItemContents: ItemContents.Note,
        otherItemContents: ItemContents.Note,
        baseAttachments: List<Attachment>,
        otherAttachments: List<Attachment>
    ): ItemDiffs = encryptionContextProvider.withEncryptionContext {
        ItemDiffs.Note(
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
            ),
            customFields = calculateItemDiffTypes(
                encryptionContext = this@withEncryptionContext,
                baseItemCustomFieldsContent = baseItemContents.customFields,
                otherItemCustomFieldsContent = otherItemContents.customFields
            )
        )
    }

    override suspend fun performAction(
        fieldType: ItemDetailsFieldType.NoteItemAction,
        callback: suspend (DetailEvent) -> Unit
    ) = Unit
}
