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

package proton.android.pass.features.migrate

import androidx.annotation.StringRes
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.notifications.api.SnackbarType

enum class MigrateSnackbarMessage(
    @StringRes override val id: Int,
    override val type: SnackbarType,
    override val isClipboard: Boolean = false
) : SnackbarMessage.StructuredMessage {
    ItemMigrated(R.string.migrate_item_success_snackbar, SnackbarType.SUCCESS),
    ItemNotMigrated(R.string.migrate_item_error_snackbar, SnackbarType.ERROR),
    SomeItemsNotMigrated(R.string.migrate_item_some_not_migrated_error_snackbar, SnackbarType.ERROR),
    VaultItemsMigrated(R.string.migrate_all_items_success_snackbar, SnackbarType.SUCCESS),
    VaultItemsNotMigrated(R.string.migrate_all_items_error_snackbar, SnackbarType.ERROR),
    CouldNotInit(R.string.migrate_init_error_snackbar, SnackbarType.ERROR)
}

