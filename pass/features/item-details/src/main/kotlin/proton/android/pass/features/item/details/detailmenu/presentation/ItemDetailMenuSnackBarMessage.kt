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

package proton.android.pass.features.item.details.detailmenu.presentation

import androidx.annotation.StringRes
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.notifications.api.SnackbarType
import proton.android.pass.composecomponents.impl.R as CompR

internal enum class ItemDetailMenuSnackBarMessage(
    @StringRes override val id: Int,
    override val type: SnackbarType,
    override val isClipboard: Boolean = false
) : SnackbarMessage.StructuredMessage {

    ItemNoteCopied(
        id = CompR.string.snack_bar_message_item_note_copied,
        type = SnackbarType.NORM,
        isClipboard = true
    ),

    ItemMigrationError(
        id = CompR.string.snack_bar_message_item_migration_error,
        type = SnackbarType.ERROR
    ),

    ItemMonitorExcludedSuccess(
        id = CompR.string.snack_bar_message_item_monitor_excluded_success,
        type = SnackbarType.SUCCESS
    ),

    ItemMonitorExcludedError(
        id = CompR.string.snack_bar_message_item_monitor_excluded_error,
        type = SnackbarType.ERROR
    ),

    ItemMonitorIncludedSuccess(
        id = CompR.string.snack_bar_message_item_monitor_included_success,
        type = SnackbarType.SUCCESS
    ),

    ItemMonitorIncludedError(
        id = CompR.string.snack_bar_message_item_monitor_included_error,
        type = SnackbarType.ERROR
    ),

    ItemPinnedSuccess(
        id = CompR.string.snack_bar_message_item_pinned_success,
        type = SnackbarType.SUCCESS
    ),

    ItemPinnedError(
        id = CompR.string.snack_bar_message_item_pinned_error,
        type = SnackbarType.ERROR
    ),

    ItemUnpinnedSuccess(
        id = CompR.string.snack_bar_message_item_unpinned_success,
        type = SnackbarType.SUCCESS
    ),

    ItemUnpinnedError(
        id = CompR.string.snack_bar_message_item_unpinned_error,
        type = SnackbarType.ERROR
    ),

    ItemTrashedSuccess(
        id = CompR.string.snack_bar_message_item_trashed_success,
        type = SnackbarType.SUCCESS
    ),

    ItemTrashedError(
        id = CompR.string.snack_bar_message_item_trashed_error,
        type = SnackbarType.ERROR
    )

}
