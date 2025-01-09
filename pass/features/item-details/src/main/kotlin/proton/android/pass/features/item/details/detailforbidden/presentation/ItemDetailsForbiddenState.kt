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

package proton.android.pass.features.item.details.detailforbidden.presentation

import androidx.annotation.StringRes
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsActionForbiddenReason
import proton.android.pass.features.item.details.R

internal data class ItemDetailsForbiddenState(
    private val reason: ItemDetailsActionForbiddenReason
) {

    @StringRes
    internal val title: Int = when (reason) {
        ItemDetailsActionForbiddenReason.EditItemPermissionRequired,
        ItemDetailsActionForbiddenReason.EditItemTrashed,
        ItemDetailsActionForbiddenReason.EditItemUpgradeRequired -> {
            R.string.item_details_forbidden_actions_edit_title
        }

        ItemDetailsActionForbiddenReason.ShareItemLimitReached,
        ItemDetailsActionForbiddenReason.ShareItemPermissionRequired,
        ItemDetailsActionForbiddenReason.ShareItemTrashed -> {
            R.string.item_details_forbidden_actions_share_title
        }
    }

    @StringRes
    internal val message: Int = when (reason) {
        ItemDetailsActionForbiddenReason.EditItemPermissionRequired -> {
            R.string.item_details_forbidden_actions_edit_permission
        }

        ItemDetailsActionForbiddenReason.EditItemTrashed -> {
            R.string.item_details_forbidden_actions_edit_trash
        }

        ItemDetailsActionForbiddenReason.EditItemUpgradeRequired -> {
            R.string.item_details_forbidden_actions_edit_upgrade
        }

        ItemDetailsActionForbiddenReason.ShareItemLimitReached -> {
            R.string.item_details_forbidden_actions_share_limit
        }

        ItemDetailsActionForbiddenReason.ShareItemPermissionRequired -> {
            R.string.item_details_forbidden_actions_sharing_permission
        }

        ItemDetailsActionForbiddenReason.ShareItemTrashed -> {
            R.string.item_details_forbidden_actions_share_trash
        }
    }

    internal val showUpgrade: Boolean = when (reason) {
        ItemDetailsActionForbiddenReason.EditItemPermissionRequired,
        ItemDetailsActionForbiddenReason.EditItemTrashed,
        ItemDetailsActionForbiddenReason.ShareItemLimitReached,
        ItemDetailsActionForbiddenReason.ShareItemPermissionRequired,
        ItemDetailsActionForbiddenReason.ShareItemTrashed -> false

        ItemDetailsActionForbiddenReason.EditItemUpgradeRequired -> true
    }

}
