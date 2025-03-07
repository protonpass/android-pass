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

package proton.android.pass.features.itemcreate.custom.createupdate.ui

import proton.android.pass.common.api.Option
import proton.android.pass.composecomponents.impl.attachments.AttachmentContentEvent
import proton.android.pass.domain.ShareId
import proton.android.pass.features.itemcreate.custom.createupdate.presentation.FieldIdentifier

enum class FieldChange {
    Title, SSID, Password, PrivateKey, PublicKey
}

sealed interface ItemContentEvent {
    data object Up : ItemContentEvent

    @JvmInline
    value class Submit(val shareId: ShareId) : ItemContentEvent

    data class OnFieldValueChange(val field: FieldChange, val value: String) : ItemContentEvent

    data class OnFieldFocusChange(val field: FieldChange, val isFocused: Boolean) : ItemContentEvent

    data class OnCustomFieldChange(
        val field: FieldIdentifier,
        val value: String
    ) : ItemContentEvent

    @JvmInline
    value class OnVaultSelect(val shareId: ShareId) : ItemContentEvent

    data class OnCustomFieldOptions(
        val field: FieldIdentifier,
        val label: String
    ) : ItemContentEvent

    data class OnCustomFieldFocused(
        val field: FieldIdentifier,
        val isFocused: Boolean
    ) : ItemContentEvent

    @JvmInline
    value class OnAddCustomField(val sectionIndex: Option<Int>) : ItemContentEvent

    data object OnAddSection : ItemContentEvent

    data class OnSectionOptions(val index: Int, val label: String) : ItemContentEvent

    @JvmInline
    value class OnAttachmentEvent(val event: AttachmentContentEvent) : ItemContentEvent

    data object DismissAttachmentBanner : ItemContentEvent
}
