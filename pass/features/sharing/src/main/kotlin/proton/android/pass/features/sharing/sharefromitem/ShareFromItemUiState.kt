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

package proton.android.pass.features.sharing.sharefromitem

import androidx.compose.runtime.Stable
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.Share
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.organizations.OrganizationSharingPolicy

@Stable
internal sealed interface ShareFromItemNavEvent {

    @Stable
    data object Unknown : ShareFromItemNavEvent

    @Stable
    data object MoveToSharedVault : ShareFromItemNavEvent

}

@Stable
internal data class ShareFromItemUiState(
    val shareId: ShareId,
    val itemId: ItemId,
    val event: ShareFromItemNavEvent,
    val canUsePaidFeatures: Boolean,
    private val isNewCryptoEnabled: Boolean,
    private val isItemSharingAvailable: Boolean,
    private val itemOption: Option<Item>,
    private val shareOption: Option<Share>,
    private val organizationSharingPolicyOption: Option<OrganizationSharingPolicy>
) {

    private val isSharedItem: Boolean = when (itemOption) {
        None -> false
        is Some -> itemOption.value.shareCount > 0
    }

    private val isSharedShare: Boolean = when (shareOption) {
        None -> false
        is Some -> shareOption.value.shared
    }

    private val isItemSharingAllowedByOrganization = when (organizationSharingPolicyOption) {
        None -> true
        is Some -> organizationSharingPolicyOption.value.canShareItems
    }

    private val isSecureLinkSharingAllowedByOrganization = when (organizationSharingPolicyOption) {
        None -> true
        is Some -> organizationSharingPolicyOption.value.canShareSecureLinks
    }

    internal val isSingleSharingAvailable: Boolean = when (itemOption) {
        None -> false
        is Some -> when (itemOption.value.itemType) {
            is ItemType.CreditCard,
            is ItemType.Identity,
            is ItemType.Login,
            is ItemType.Custom,
            is ItemType.Note -> true

            is ItemType.Alias,
            ItemType.Password,
            ItemType.Unknown -> false
        }
    }

    internal val canShareViaItemSharing: Boolean
        get() {
            if (!isItemSharingAllowedByOrganization) return false

            if (!isItemSharingAvailable) return false

            return when (shareOption) {
                None -> false
                is Some -> when (val share = shareOption.value) {
                    is Share.Item -> share.canBeShared
                    is Share.Vault -> share.isOwner || share.isAdmin
                }
            }
        }

    internal val canShareViaSecureLink: Boolean
        get() {
            if (!isSecureLinkSharingAllowedByOrganization) return false

            return when (shareOption) {
                None -> false
                is Some -> when (val share = shareOption.value) {
                    is Share.Item -> isNewCryptoEnabled && share.isAdmin
                    is Share.Vault -> share.isAdmin
                }
            }
        }

    internal val canManageAccess: Boolean = isItemSharingAvailable && (isSharedItem || isSharedShare)

    internal companion object {

        fun initial(shareId: ShareId, itemId: ItemId) = ShareFromItemUiState(
            shareId = shareId,
            itemId = itemId,
            event = ShareFromItemNavEvent.Unknown,
            canUsePaidFeatures = false,
            isItemSharingAvailable = false,
            isNewCryptoEnabled = false,
            itemOption = None,
            shareOption = None,
            organizationSharingPolicyOption = None
        )

    }

}
