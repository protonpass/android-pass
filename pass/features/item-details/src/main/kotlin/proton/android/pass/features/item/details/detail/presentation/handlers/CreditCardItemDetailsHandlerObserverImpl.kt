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
import proton.android.pass.commonui.api.toItemContents
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.commonuimodels.api.items.ItemDetailState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemCustomFieldSection
import proton.android.pass.domain.ItemDiffs
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.Share
import proton.android.pass.domain.attachments.Attachment
import javax.inject.Inject

class CreditCardItemDetailsHandlerObserverImpl @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider
) : ItemDetailsHandlerObserver<ItemContents.CreditCard>() {

    override fun observe(
        share: Share,
        item: Item,
        attachmentsState: AttachmentsState
    ): Flow<ItemDetailState> = observeCreditCardItemContents(item)
        .mapLatest { creditCardItemContents ->
            ItemDetailState.CreditCard(
                itemContents = creditCardItemContents,
                itemId = item.id,
                shareId = item.shareId,
                isItemPinned = item.isPinned,
                itemShare = share,
                itemCreatedAt = item.createTime,
                itemModifiedAt = item.modificationTime,
                itemLastAutofillAtOption = item.lastAutofillTime,
                itemRevision = item.revision,
                itemState = ItemState.from(item.state),
                itemDiffs = ItemDiffs.CreditCard(),
                itemShareCount = item.shareCount,
                attachmentsState = attachmentsState
            )
        }

    private fun observeCreditCardItemContents(item: Item): Flow<ItemContents.CreditCard> = flow {
        encryptionContextProvider.withEncryptionContext {
            toItemContents(
                itemType = item.itemType,
                encryptionContext = this,
                title = item.title,
                note = item.note,
                flags = item.flags
            ) as ItemContents.CreditCard
        }.let { creditCardItemContents ->
            emit(creditCardItemContents)
        }
    }

    override fun updateItemContents(
        itemContents: ItemContents.CreditCard,
        hiddenFieldType: ItemDetailsFieldType.Hidden,
        hiddenFieldSection: ItemCustomFieldSection,
        hiddenState: HiddenState
    ): ItemContents = when (hiddenFieldType) {
        ItemDetailsFieldType.Hidden.Cvv -> itemContents.copy(
            cvv = hiddenState
        )

        ItemDetailsFieldType.Hidden.Pin -> itemContents.copy(
            pin = hiddenState
        )

        is ItemDetailsFieldType.Hidden.CustomField,
        is ItemDetailsFieldType.Hidden.PrivateKey,
        ItemDetailsFieldType.Hidden.Password -> itemContents
    }

    override fun calculateItemDiffs(
        baseItemContents: ItemContents.CreditCard,
        otherItemContents: ItemContents.CreditCard,
        baseAttachments: List<Attachment>,
        otherAttachments: List<Attachment>
    ): ItemDiffs = encryptionContextProvider.withEncryptionContext {
        ItemDiffs.CreditCard(
            title = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.title,
                otherItemFieldValue = otherItemContents.title
            ),
            note = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.note,
                otherItemFieldValue = otherItemContents.note
            ),
            cardHolder = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.cardHolder,
                otherItemFieldValue = otherItemContents.cardHolder
            ),
            cardNumber = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.number,
                otherItemFieldValue = otherItemContents.number
            ),
            cvv = calculateItemDiffType(
                encryptionContext = this@withEncryptionContext,
                baseItemFieldHiddenState = baseItemContents.cvv,
                otherItemFieldHiddenState = otherItemContents.cvv
            ),
            pin = calculateItemDiffType(
                encryptionContext = this@withEncryptionContext,
                baseItemFieldHiddenState = baseItemContents.pin,
                otherItemFieldHiddenState = otherItemContents.pin
            ),
            expirationDate = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.expirationDate,
                otherItemFieldValue = otherItemContents.expirationDate
            ),
            attachments = calculateItemDiffType(
                baseItemAttachments = baseAttachments,
                otherItemAttachments = otherAttachments
            )
        )
    }

}
