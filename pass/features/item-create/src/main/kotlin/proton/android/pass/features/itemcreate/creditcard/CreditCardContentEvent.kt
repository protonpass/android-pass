/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.itemcreate.creditcard

import proton.android.pass.composecomponents.impl.attachments.AttachmentContentEvent
import proton.android.pass.domain.ShareId
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldEvent

internal sealed interface CreditCardContentEvent {
    data object Up : CreditCardContentEvent
    data object Upgrade : CreditCardContentEvent

    @JvmInline
    value class Submit(val shareId: ShareId) : CreditCardContentEvent

    @JvmInline
    value class OnNameChange(val value: String) : CreditCardContentEvent

    @JvmInline
    value class OnNumberChange(val value: String) : CreditCardContentEvent

    @JvmInline
    value class OnCVVChange(val value: String) : CreditCardContentEvent

    @JvmInline
    value class OnCVVFocusChange(val isFocused: Boolean) : CreditCardContentEvent

    @JvmInline
    value class OnPinChange(val value: String) : CreditCardContentEvent

    @JvmInline
    value class OnPinFocusChange(val isFocused: Boolean) : CreditCardContentEvent

    @JvmInline
    value class OnExpirationDateChange(val value: String) : CreditCardContentEvent

    @JvmInline
    value class OnNoteChange(val value: String) : CreditCardContentEvent

    @JvmInline
    value class OnTitleChange(val value: String) : CreditCardContentEvent

    @JvmInline
    value class OnVaultSelect(val shareId: ShareId) : CreditCardContentEvent

    @JvmInline
    value class OnAttachmentEvent(val event: AttachmentContentEvent) : CreditCardContentEvent

    data object DismissAttachmentBanner : CreditCardContentEvent

    @JvmInline
    value class OnCustomFieldEvent(val event: CustomFieldEvent) : CreditCardContentEvent
}
