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

package proton.android.pass.features.itemdetail.creditcard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.composecomponents.impl.attachments.AttachmentSection
import proton.android.pass.composecomponents.impl.item.details.sections.shared.PassItemDetailsHistorySection
import proton.android.pass.composecomponents.impl.item.details.sections.shared.PassItemDetailsMoreInfoSection
import proton.android.pass.composecomponents.impl.utils.passItemColors
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.Share
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.features.itemdetail.common.NoteSection

@Composable
internal fun CreditCardDetailContent(
    modifier: Modifier = Modifier,
    contents: CreditCardDetailUiState.ItemContent,
    share: Share,
    isDowngradedMode: Boolean,
    isPinned: Boolean,
    canViewItemHistory: Boolean,
    isFileAttachmentsEnabled: Boolean,
    attachmentsState: AttachmentsState,
    hasMoreThanOneVaultShare: Boolean,
    onEvent: (CreditCardDetailEvent) -> Unit
) {
    val model = contents.model.contents as ItemContents.CreditCard

    Column(
        modifier = modifier.padding(horizontal = Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.mediumSmall)
    ) {
        CreditCardTitle(
            modifier = Modifier.padding(Spacing.none, Spacing.mediumSmall),
            title = model.title,
            share = share,
            onShareClick = { onEvent(CreditCardDetailEvent.OnShareClick) },
            isPinned = isPinned,
            hasMoreThanOneVaultShare = hasMoreThanOneVaultShare
        )

        MainCreditCardSection(
            cardHolder = model.cardHolder,
            number = contents.cardNumber,
            cvv = model.cvv,
            pin = model.pin,
            expirationDate = model.expirationDate,
            isDowngradedMode = isDowngradedMode,
            onEvent = onEvent
        )

        if (model.note.isNotBlank()) {
            NoteSection(
                text = model.note,
                accentColor = PassTheme.colors.cardInteractionNorm
            )
        }

        if (isFileAttachmentsEnabled) {
            AttachmentSection(
                attachmentsState = attachmentsState,
                isDetail = true,
                itemColors = passItemColors(ItemCategory.CreditCard),
                onEvent = { onEvent(CreditCardDetailEvent.OnAttachmentEvent(it)) }
            )
        }

        PassItemDetailsHistorySection(
            lastAutofillAtOption = contents.model.lastAutofillTime.toOption(),
            revision = contents.model.revision,
            createdAt = contents.model.createTime,
            modifiedAt = contents.model.modificationTime,
            onViewItemHistoryClicked = { onEvent(CreditCardDetailEvent.OnViewItemHistoryClicked) },
            itemColors = passItemColors(itemCategory = ItemCategory.CreditCard),
            shouldDisplayItemHistoryButton = canViewItemHistory
        )

        PassItemDetailsMoreInfoSection(
            itemId = contents.model.id,
            shareId = contents.model.shareId
        )
    }
}
