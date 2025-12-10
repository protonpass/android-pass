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

package proton.android.pass.features.itemcreate.alias

import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.Option
import proton.android.pass.domain.CustomFieldType
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.AttachmentId
import java.net.URI

sealed interface CreateAliasNavigation {
    data class CreatedFromBottomsheet(val alias: String) : CreateAliasNavigation
    data class Created(
        val userId: UserId,
        val shareId: ShareId,
        val itemId: ItemId,
        val alias: String
    ) : CreateAliasNavigation

    data class SelectVault(val shareId: ShareId) : CreateAliasNavigation
}

sealed interface UpdateAliasNavigation {
    data class Updated(val shareId: ShareId, val itemId: ItemId) : UpdateAliasNavigation

    data class OpenAttachmentOptions(
        val shareId: ShareId,
        val itemId: ItemId,
        val attachmentId: AttachmentId
    ) : UpdateAliasNavigation
}

sealed interface BaseAliasNavigation {
    data class OnCreateAliasEvent(val event: CreateAliasNavigation) : BaseAliasNavigation
    data class OnUpdateAliasEvent(val event: UpdateAliasNavigation) : BaseAliasNavigation

    data object CloseBottomsheet : BaseAliasNavigation
    data object Upgrade : BaseAliasNavigation
    data object CloseScreen : BaseAliasNavigation
    data object AddAttachment : BaseAliasNavigation
    data object UpsellAttachments : BaseAliasNavigation
    data object SelectMailbox : BaseAliasNavigation
    data object SelectSuffix : BaseAliasNavigation
    data object AddMailbox : BaseAliasNavigation

    @JvmInline
    value class DeleteAllAttachments(val attachmentIds: Set<AttachmentId>) : BaseAliasNavigation

    @JvmInline
    value class OpenDraftAttachmentOptions(val uri: URI) : BaseAliasNavigation

    data object AddCustomField : BaseAliasNavigation
    data class CustomFieldTypeSelected(val type: CustomFieldType) : BaseAliasNavigation

    data class CustomFieldOptions(val currentValue: String, val index: Int) : BaseAliasNavigation
    data class EditCustomField(val currentValue: String, val index: Int) : BaseAliasNavigation
    data object RemovedCustomField : BaseAliasNavigation

    @JvmInline
    value class TotpSuccess(val results: Map<String, Any>) : BaseAliasNavigation
    data object TotpCancel : BaseAliasNavigation

    @JvmInline
    value class OpenImagePicker(val index: Option<Int>) : BaseAliasNavigation

    @JvmInline
    value class ScanTotp(val index: Option<Int>) : BaseAliasNavigation
}
