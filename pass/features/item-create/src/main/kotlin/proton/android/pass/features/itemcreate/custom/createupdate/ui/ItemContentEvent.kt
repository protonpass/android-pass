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
import proton.android.pass.domain.WifiSecurityType
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldEvent

internal enum class FieldChange {
    Password,
    PrivateKey,
    PublicKey,
    SSID,
    Title,
    WifiSecurityType,
    Note
}

internal sealed interface ItemContentEvent {

    data object Up : ItemContentEvent

    @JvmInline
    value class Submit(val shareId: ShareId) : ItemContentEvent

    data class OnFieldValueChange(val field: FieldChange, val value: Any) : ItemContentEvent

    data class OnFieldFocusChange(val field: FieldChange, val isFocused: Boolean) : ItemContentEvent

    @JvmInline
    value class OnVaultSelect(val shareId: ShareId) : ItemContentEvent

    data object OnAddSection : ItemContentEvent

    data class OnSectionOptions(val index: Int, val label: String) : ItemContentEvent

    @JvmInline
    value class OnAttachmentEvent(val event: AttachmentContentEvent) : ItemContentEvent

    data object DismissAttachmentBanner : ItemContentEvent

    data object OnPasteTOTPSecret : ItemContentEvent

    data class OnOpenTOTPScanner(val sectionIndex: Option<Int>, val index: Int) : ItemContentEvent

    @JvmInline
    value class OnOpenWifiSecurityType(val wifiSecurityType: WifiSecurityType) : ItemContentEvent

    data object OnUpgrade : ItemContentEvent

    @JvmInline
    value class OnCustomFieldEvent(val event: CustomFieldEvent) : ItemContentEvent
}
