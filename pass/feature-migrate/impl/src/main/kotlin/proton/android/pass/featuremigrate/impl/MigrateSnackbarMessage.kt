package proton.android.pass.featuremigrate.impl

import androidx.annotation.StringRes
import proton.android.pass.featuremigrate.R
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.notifications.api.SnackbarType

enum class MigrateSnackbarMessage(
    @StringRes override val id: Int,
    override val type: SnackbarType,
    override val isClipboard: Boolean = false
) : SnackbarMessage {
    ItemMigrated(R.string.migrate_item_success_snackbar, SnackbarType.SUCCESS),
    ItemNotMigrated(R.string.migrate_item_error_snackbar, SnackbarType.ERROR),
    VaultItemsMigrated(R.string.migrate_all_items_success_snackbar, SnackbarType.SUCCESS),
    VaultItemsNotMigrated(R.string.migrate_all_items_error_snackbar, SnackbarType.ERROR)
}

