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

package proton.android.pass.features.itemcreate.identity.navigation

import proton.android.pass.composecomponents.impl.attachments.AttachmentContentEvent
import proton.android.pass.domain.ShareId
import proton.android.pass.features.itemcreate.identity.presentation.IdentityField
import proton.android.pass.features.itemcreate.identity.ui.IdentitySectionType

sealed interface IdentityContentEvent {

    data object Up : IdentityContentEvent

    @JvmInline
    value class Submit(val shareId: ShareId) : IdentityContentEvent

    data class OnFieldChange(
        val field: IdentityField,
        val value: String
    ) : IdentityContentEvent

    data class OnFocusChange(
        val field: IdentityField,
        val isFocused: Boolean
    ) : IdentityContentEvent

    @JvmInline
    value class OnVaultSelect(val shareId: ShareId) : IdentityContentEvent

    data class OnCustomFieldClick(
        val index: Int,
        val field: IdentityField.CustomField
    ) : IdentityContentEvent

    data class OnCustomFieldOptions(
        val index: Int,
        val label: String,
        val field: IdentityField.CustomField
    ) : IdentityContentEvent

    data object OnAddPersonalDetailField : IdentityContentEvent
    data object OnAddAddressDetailField : IdentityContentEvent
    data object OnAddContactDetailField : IdentityContentEvent
    data object OnAddWorkField : IdentityContentEvent
    data object OnAddExtraSection : IdentityContentEvent

    @JvmInline
    value class OnAddExtraSectionCustomField(val index: Int) : IdentityContentEvent

    data class OnExtraSectionOptions(val index: Int, val label: String) : IdentityContentEvent

    @JvmInline
    value class OnAttachmentEvent(val event: AttachmentContentEvent) : IdentityContentEvent

    data object DismissAttachmentBanner : IdentityContentEvent

    data object PasteTotp : IdentityContentEvent

    data class OnScanTotp(val section: IdentitySectionType, val index: Int) : IdentityContentEvent
}
