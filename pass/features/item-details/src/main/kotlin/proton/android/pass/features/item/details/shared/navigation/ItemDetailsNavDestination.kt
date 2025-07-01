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

package proton.android.pass.features.item.details.shared.navigation

import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsActionForbiddenReason
import proton.android.pass.commonuimodels.api.UIPasskeyContent
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.AttachmentId
import proton.android.pass.domain.items.ItemCategory

sealed interface ItemDetailsNavDestination {

    data object CloseScreen : ItemDetailsNavDestination

    data object Home : ItemDetailsNavDestination

    data class EditItem(
        val shareId: ShareId,
        val itemId: ItemId,
        val itemCategory: ItemCategory
    ) : ItemDetailsNavDestination

    data class CloneItem(
        val shareId: ShareId,
        val itemId: ItemId
    ) : ItemDetailsNavDestination

    @JvmInline
    value class PasskeyDetails(val passkeyContent: UIPasskeyContent) : ItemDetailsNavDestination

    data class ItemHistory(
        val shareId: ShareId,
        val itemId: ItemId
    ) : ItemDetailsNavDestination

    data class ItemSharing(
        val shareId: ShareId,
        val itemId: ItemId
    ) : ItemDetailsNavDestination

    data class ManageSharedVault(
        val sharedVaultId: ShareId,
        val itemCategory: ItemCategory
    ) : ItemDetailsNavDestination

    data class ItemOptionsMenu(
        val shareId: ShareId,
        val itemId: ItemId
    ) : ItemDetailsNavDestination

    data class ItemTrashMenu(
        val shareId: ShareId,
        val itemId: ItemId
    ) : ItemDetailsNavDestination

    data object ItemMigration : ItemDetailsNavDestination

    data object ItemSharedMigration : ItemDetailsNavDestination

    data object DismissBottomSheet : ItemDetailsNavDestination

    @JvmInline
    value class ItemActionForbidden(
        val reason: ItemDetailsActionForbiddenReason
    ) : ItemDetailsNavDestination

    data object Upgrade : ItemDetailsNavDestination

    @JvmInline
    value class LeaveItemShare(val shareId: ShareId) : ItemDetailsNavDestination

    @JvmInline
    value class WifiNetworkQRClick(val rawSVG: String) : ItemDetailsNavDestination

    data class OpenAttachmentOptions(
        val shareId: ShareId,
        val itemId: ItemId,
        val attachmentId: AttachmentId
    ) : ItemDetailsNavDestination

    data class ViewReusedPasswords(val shareId: ShareId, val itemId: ItemId) : ItemDetailsNavDestination

    data class ContactSection(val shareId: ShareId, val itemId: ItemId) : ItemDetailsNavDestination

    data class OnCreateLoginFromAlias(
        val alias: String,
        val shareId: ShareId
    ) : ItemDetailsNavDestination
}
