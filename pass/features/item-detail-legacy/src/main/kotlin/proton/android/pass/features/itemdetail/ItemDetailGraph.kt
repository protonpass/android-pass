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

package proton.android.pass.features.itemdetail

import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.PasskeyId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.AttachmentId

sealed interface ItemDetailNavigation {

    data class OnEdit(val itemUiModel: ItemUiModel) : ItemDetailNavigation

    data object OnMigrate : ItemDetailNavigation

    data object OnMigrateSharedWarning : ItemDetailNavigation

    data class OnCreateLoginFromAlias(
        val alias: String,
        val shareId: ShareId
    ) : ItemDetailNavigation

    data class OnViewItem(val shareId: ShareId, val itemId: ItemId) : ItemDetailNavigation

    data object CloseScreen : ItemDetailNavigation
    data object DismissBottomSheet : ItemDetailNavigation

    @JvmInline
    value class Upgrade(val popBefore: Boolean = false) : ItemDetailNavigation

    data class ManageItem(val shareId: ShareId, val itemId: ItemId) : ItemDetailNavigation

    @JvmInline
    value class ManageVault(val shareId: ShareId) : ItemDetailNavigation

    data class OnShareVault(val shareId: ShareId, val itemId: ItemId) : ItemDetailNavigation

    data class OnViewItemHistory(
        val shareId: ShareId,
        val itemId: ItemId
    ) : ItemDetailNavigation

    data class ViewPasskeyDetails(
        val shareId: ShareId,
        val itemId: ItemId,
        val passkeyId: PasskeyId
    ) : ItemDetailNavigation

    data class ViewReusedPasswords(val shareId: ShareId, val itemId: ItemId) : ItemDetailNavigation

    data class OnTrashAlias(val shareId: ShareId, val itemId: ItemId) : ItemDetailNavigation

    data class OnContactsClicked(val shareId: ShareId, val itemId: ItemId) : ItemDetailNavigation

    @JvmInline
    value class LeaveItemShare(val shareId: ShareId) : ItemDetailNavigation

    data class OpenAttachmentOptions(
        val shareId: ShareId,
        val itemId: ItemId,
        val attachmentId: AttachmentId
    ) : ItemDetailNavigation
}
