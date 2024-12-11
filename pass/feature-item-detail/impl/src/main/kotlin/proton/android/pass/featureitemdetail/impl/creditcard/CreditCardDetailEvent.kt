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

package proton.android.pass.featureitemdetail.impl.creditcard

import proton.android.pass.composecomponents.impl.attachments.AttachmentContentEvent

sealed interface CreditCardDetailEvent {

    data object OnCardHolderClick : CreditCardDetailEvent

    data object OnToggleNumberClick : CreditCardDetailEvent

    data object OnNumberClick : CreditCardDetailEvent

    data object OnToggleCvvClick : CreditCardDetailEvent

    data object OnCvvClick : CreditCardDetailEvent

    data object OnTogglePinClick : CreditCardDetailEvent

    data object OnUpgradeClick : CreditCardDetailEvent

    data object OnShareClick : CreditCardDetailEvent

    data object OnViewItemHistoryClicked : CreditCardDetailEvent

    @JvmInline
    value class OnAttachmentEvent(
        val attachmentContentEvent: AttachmentContentEvent
    ) : CreditCardDetailEvent
}
