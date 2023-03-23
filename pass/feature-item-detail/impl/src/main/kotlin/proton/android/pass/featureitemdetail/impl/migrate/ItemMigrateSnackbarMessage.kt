package proton.android.pass.featureitemdetail.impl.migrate

import androidx.annotation.StringRes
import proton.android.pass.featureitemdetail.impl.R
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.notifications.api.SnackbarType

enum class ItemMigrateSnackbarMessage(
    @StringRes override val id: Int,
    override val type: SnackbarType,
    override val isClipboard: Boolean = false
) : SnackbarMessage {
    ItemMigrated(R.string.migrate_item_success_snackbar, SnackbarType.SUCCESS),
    ItemNotMigrated(R.string.migrate_item_error_snackbar, SnackbarType.ERROR)
}
