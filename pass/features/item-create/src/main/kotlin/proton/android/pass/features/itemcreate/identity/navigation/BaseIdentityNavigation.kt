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

package proton.android.pass.features.itemcreate.identity.navigation

import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.domain.CustomFieldType
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.AttachmentId
import proton.android.pass.features.itemcreate.identity.navigation.bottomsheets.AddIdentityFieldType
import proton.android.pass.features.itemcreate.identity.ui.IdentitySectionType
import java.net.URI

sealed interface BaseIdentityNavigation {
    data object CloseScreen : BaseIdentityNavigation
    data object DismissBottomsheet : BaseIdentityNavigation

    data class OpenExtraFieldBottomSheet(
        val addIdentityFieldType: AddIdentityFieldType,
        val sectionIndex: Option<Int> = None
    ) : BaseIdentityNavigation

    data object OpenCustomFieldBottomSheet : BaseIdentityNavigation

    @JvmInline
    value class CustomFieldTypeSelected(val type: CustomFieldType) : BaseIdentityNavigation

    data class CustomFieldOptions(val title: String, val index: Int) : BaseIdentityNavigation

    data class EditCustomField(val title: String, val index: Int) : BaseIdentityNavigation

    data object RemovedCustomField : BaseIdentityNavigation

    data class ExtraSectionOptions(val title: String, val index: Int) : BaseIdentityNavigation

    data class EditCustomSection(val title: String, val index: Int) : BaseIdentityNavigation

    data object RemoveCustomSection : BaseIdentityNavigation

    data object AddExtraSection : BaseIdentityNavigation

    data object AddAttachment : BaseIdentityNavigation

    data object UpsellAttachments : BaseIdentityNavigation

    @JvmInline
    value class DeleteAllAttachments(val attachmentIds: Set<AttachmentId>) : BaseIdentityNavigation

    data class OpenAttachmentOptions(
        val shareId: ShareId,
        val itemId: ItemId,
        val attachmentId: AttachmentId
    ) : BaseIdentityNavigation

    @JvmInline
    value class OpenDraftAttachmentOptions(val uri: URI) : BaseIdentityNavigation

    @JvmInline
    value class TotpSuccess(val results: Map<String, Any>) : BaseIdentityNavigation
    data object TotpCancel : BaseIdentityNavigation

    data class OpenImagePicker(
        val specialIndex: Option<Int>,
        val sectionIndex: Option<Int>,
        val index: Option<Int>
    ) : BaseIdentityNavigation

    data class ScanTotp(val section: IdentitySectionType, val index: Int) : BaseIdentityNavigation
}
