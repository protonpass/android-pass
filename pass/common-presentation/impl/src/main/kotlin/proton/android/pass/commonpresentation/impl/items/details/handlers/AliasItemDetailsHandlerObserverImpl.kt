/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.commonpresentation.impl.items.details.handlers

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldType
import proton.android.pass.commonpresentation.api.items.details.handlers.ItemDetailsHandlerObserver
import proton.android.pass.commonui.api.toItemContents
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.commonuimodels.api.items.ItemDetailState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.ObserveAliasDetails
import proton.android.pass.domain.AliasDetails
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemCustomFieldSection
import proton.android.pass.domain.ItemDiffs
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.Share
import proton.android.pass.domain.attachments.Attachment
import javax.inject.Inject

class AliasItemDetailsHandlerObserverImpl @Inject constructor(
    private val observeAliasDetails: ObserveAliasDetails,
    private val encryptionContextProvider: EncryptionContextProvider
) : ItemDetailsHandlerObserver<ItemContents.Alias>() {

    override fun observe(
        share: Share,
        item: Item,
        attachmentsState: AttachmentsState
    ): Flow<ItemDetailState> = combine(
        observeAliasItemContents(item),
        observeAliasDetails(item.shareId, item.id).onStart { emit(AliasDetails.EMPTY) }
    ) { aliasItemContents, aliasDetails ->
        ItemDetailState.Alias(
            itemContents = aliasItemContents,
            itemId = item.id,
            shareId = item.shareId,
            isItemPinned = item.isPinned,
            itemShare = share,
            itemCreatedAt = item.createTime,
            itemModifiedAt = item.modificationTime,
            itemLastAutofillAtOption = item.lastAutofillTime,
            itemRevision = item.revision,
            itemState = ItemState.from(item.state),
            itemDiffs = ItemDiffs.Alias(),
            itemShareCount = item.shareCount,
            mailboxes = aliasDetails.mailboxes,
            attachmentsState = attachmentsState
        )
    }

    private fun observeAliasItemContents(item: Item): Flow<ItemContents.Alias> = flow {
        encryptionContextProvider.withEncryptionContext {
            toItemContents(
                itemType = item.itemType,
                encryptionContext = this,
                title = item.title,
                note = item.note,
                flags = item.flags
            ) as ItemContents.Alias
        }.also { aliasItemContents ->
            emit(aliasItemContents)
        }
    }

    override fun updateItemContents(
        itemContents: ItemContents.Alias,
        hiddenFieldType: ItemDetailsFieldType.Hidden,
        hiddenFieldSection: ItemCustomFieldSection,
        hiddenState: HiddenState
    ): ItemContents = when (hiddenFieldType) {
        is ItemDetailsFieldType.Hidden.CustomField,
        ItemDetailsFieldType.Hidden.Cvv,
        ItemDetailsFieldType.Hidden.Password,
        ItemDetailsFieldType.Hidden.Pin -> itemContents
    }

    override fun calculateItemDiffs(
        baseItemContents: ItemContents.Alias,
        otherItemContents: ItemContents.Alias,
        baseAttachments: List<Attachment>,
        otherAttachments: List<Attachment>
    ): ItemDiffs = ItemDiffs.Alias(
        title = calculateItemDiffType(
            baseItemFieldValue = baseItemContents.title,
            otherItemFieldValue = otherItemContents.title
        ),
        note = calculateItemDiffType(
            baseItemFieldValue = baseItemContents.note,
            otherItemFieldValue = otherItemContents.note
        ),
        aliasEmail = calculateItemDiffType(
            baseItemFieldValue = baseItemContents.aliasEmail,
            otherItemFieldValue = otherItemContents.aliasEmail
        ),
        attachments = calculateItemDiffType(
            baseItemAttachments = baseAttachments,
            otherItemAttachments = otherAttachments
        )
    )

}
