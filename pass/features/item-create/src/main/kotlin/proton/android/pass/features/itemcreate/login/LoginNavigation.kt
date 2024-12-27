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

package proton.android.pass.features.itemcreate.login

import proton.android.pass.common.api.Option
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.AttachmentId
import proton.android.pass.features.itemcreate.bottomsheets.customfield.CustomFieldType
import java.net.URI

sealed interface CreateLoginNavigation {
    @JvmInline
    value class LoginCreated(val itemUiModel: ItemUiModel) : CreateLoginNavigation

    @JvmInline
    value class SelectVault(val shareId: ShareId) : CreateLoginNavigation

    @JvmInline
    value class LoginCreatedWithPasskey(val createPasskeyResponse: String) : CreateLoginNavigation
}

sealed interface UpdateLoginNavigation {
    data class LoginUpdated(val shareId: ShareId, val itemId: ItemId) : UpdateLoginNavigation
}

sealed interface BaseLoginNavigation {
    data class OnCreateLoginEvent(val event: CreateLoginNavigation) : BaseLoginNavigation
    data class OnUpdateLoginEvent(val event: UpdateLoginNavigation) : BaseLoginNavigation

    data class CreateAlias(
        val shareId: ShareId,
        val showUpgrade: Boolean,
        val title: Option<String>
    ) : BaseLoginNavigation

    data object GeneratePassword : BaseLoginNavigation
    data object Upgrade : BaseLoginNavigation
    data class ScanTotp(
        val index: Option<Int>
    ) : BaseLoginNavigation
    data object Close : BaseLoginNavigation

    data class AliasOptions(
        val shareId: ShareId,
        val showUpgrade: Boolean
    ) : BaseLoginNavigation
    data object DeleteAlias : BaseLoginNavigation
    data class EditAlias(
        val shareId: ShareId,
        val showUpgrade: Boolean
    ) : BaseLoginNavigation

    data object AddCustomField : BaseLoginNavigation
    data class CustomFieldTypeSelected(
        val type: CustomFieldType
    ) : BaseLoginNavigation

    data class CustomFieldOptions(val currentValue: String, val index: Int) : BaseLoginNavigation
    data class EditCustomField(val currentValue: String, val index: Int) : BaseLoginNavigation
    data object RemovedCustomField : BaseLoginNavigation

    @JvmInline
    value class TotpSuccess(val results: Map<String, Any>) : BaseLoginNavigation
    data object TotpCancel : BaseLoginNavigation

    @JvmInline
    value class OpenImagePicker(val index: Option<Int>) : BaseLoginNavigation

    data object AddAttachment : BaseLoginNavigation

    data object DeleteAllAttachments : BaseLoginNavigation

    @JvmInline
    value class OpenDraftAttachmentOptions(val uri: URI) : BaseLoginNavigation

    @JvmInline
    value class OpenAttachmentOptions(val attachmentId: AttachmentId) : BaseLoginNavigation
}
