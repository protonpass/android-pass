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
import proton.android.pass.data.api.usecases.CanPerformPaidAction
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemDiffs
import proton.android.pass.domain.ItemSection
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.Share
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.totp.api.TotpManager
import javax.inject.Inject

class CreditCardItemDetailsHandlerObserverImpl @Inject constructor(
    override val encryptionContextProvider: EncryptionContextProvider,
    override val totpManager: TotpManager,
    override val canDisplayTotp: CanDisplayTotp,
    private val canPerformPaidAction: CanPerformPaidAction
) : ItemDetailsHandlerObserver<ItemContents.CreditCard, ItemDetailsFieldType.CreditCardItemAction>(
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
        observeCustomFieldTotps(item),
        canPerformPaidAction()
    ) { creditCardItemContents, customFieldTotps, canPerformPaidAction ->
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
            attachmentsState = attachmentsState,
            customFieldTotps = customFieldTotps,
            detailEvent = detailEvent,
            isDowngraded = !canPerformPaidAction
        )
    }

    override fun updateHiddenFieldsContents(
        itemContents: ItemContents.CreditCard,
        revealedHiddenCopyableFields: Map<ItemSection, Set<ItemDetailsFieldType.HiddenCopyable>>
    ): ItemContents {
        val revealedFields = revealedHiddenCopyableFields[ItemSection.CreditCard] ?: emptyList()
        return itemContents.copy(
            cvv = updateHiddenStateValue(
                hiddenState = itemContents.cvv,
                shouldBeRevealed = revealedFields.any { it is ItemDetailsFieldType.HiddenCopyable.Cvv },
                encryptionContextProvider = encryptionContextProvider
            ),
            pin = updateHiddenStateValue(
                hiddenState = itemContents.pin,
                shouldBeRevealed = revealedFields.any { it is ItemDetailsFieldType.HiddenCopyable.Pin },
                encryptionContextProvider = encryptionContextProvider
            ),
            customFields = updateHiddenCustomFieldContents(
                customFields = itemContents.customFields,
                revealedHiddenFields = revealedHiddenCopyableFields[ItemSection.CustomField].orEmpty()
            )
        )
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
            ),
            customFields = calculateItemDiffTypes(
                encryptionContext = this@withEncryptionContext,
                baseItemCustomFieldsContent = baseItemContents.customFields,
                otherItemCustomFieldsContent = otherItemContents.customFields
            )
        )
    }

    override suspend fun performAction(
        fieldType: ItemDetailsFieldType.CreditCardItemAction,
        callback: suspend (DetailEvent) -> Unit
    ) = Unit
}
