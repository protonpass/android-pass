/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.itemcreate.custom.createupdate.navigation

import proton.android.pass.common.api.Option
import proton.android.pass.domain.CustomFieldType
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.WifiSecurityType
import proton.android.pass.domain.attachments.AttachmentId
import java.net.URI

sealed interface BaseCustomItemNavigation {

    data object CloseScreen : BaseCustomItemNavigation

    data object DismissBottomsheet : BaseCustomItemNavigation

    @JvmInline
    value class AddCustomField(val sectionIndex: Option<Int>) : BaseCustomItemNavigation

    data object RemoveCustomField : BaseCustomItemNavigation

    data class CustomFieldTypeSelected(
        val type: CustomFieldType,
        val sectionIndex: Option<Int>
    ) : BaseCustomItemNavigation

    data class CustomFieldOptions(
        val title: String,
        val index: Int,
        val sectionIndex: Option<Int>
    ) : BaseCustomItemNavigation

    data class EditCustomField(
        val title: String,
        val index: Int,
        val sectionIndex: Option<Int>
    ) : BaseCustomItemNavigation

    data object AddSection : BaseCustomItemNavigation

    data object RemoveSection : BaseCustomItemNavigation

    data class SectionOptions(val title: String, val index: Int) : BaseCustomItemNavigation

    data class EditSection(val title: String, val index: Int) : BaseCustomItemNavigation

    data object AddAttachment : BaseCustomItemNavigation

    data object UpsellAttachments : BaseCustomItemNavigation

    @JvmInline
    value class DeleteAllAttachments(val attachmentIds: Set<AttachmentId>) : BaseCustomItemNavigation

    data class OpenAttachmentOptions(
        val shareId: ShareId,
        val itemId: ItemId,
        val attachmentId: AttachmentId
    ) : BaseCustomItemNavigation

    @JvmInline
    value class OpenDraftAttachmentOptions(val uri: URI) : BaseCustomItemNavigation

    data class OpenTOTPScanner(val sectionIndex: Option<Int>, val index: Int) : BaseCustomItemNavigation

    @JvmInline
    value class TotpSuccess(val results: Map<String, Any>) : BaseCustomItemNavigation

    data object TotpCancel : BaseCustomItemNavigation

    data class OpenImagePicker(val sectionIndex: Option<Int>, val index: Int) : BaseCustomItemNavigation

    @JvmInline
    value class OpenWifiSecurityTypeSelector(val wifiSecurityType: WifiSecurityType) : BaseCustomItemNavigation

    @JvmInline
    value class WifiSecurityTypeSelected(val wifiSecurityType: WifiSecurityType) : BaseCustomItemNavigation

}
