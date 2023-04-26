package proton.android.pass.featuresettings.impl

import androidx.annotation.StringRes
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.notifications.api.SnackbarType

enum class SettingsSnackbarMessage(
    @StringRes override val id: Int,
    override val type: SnackbarType,
    override val isClipboard: Boolean = false
) : SnackbarMessage {
    ErrorPerformingOperation(R.string.settings_error_performing_operation, SnackbarType.ERROR),
    ErrorPerformingSync(R.string.settings_force_sync_error, SnackbarType.ERROR),
    SyncSuccessful(R.string.settings_force_sync_success, SnackbarType.SUCCESS),
    ChangePrimaryVaultSuccess(R.string.settings_primary_vault_change_success, SnackbarType.SUCCESS),
    ChangePrimaryVaultError(R.string.settings_primary_vault_change_error, SnackbarType.ERROR),
    ClearIconCacheSuccess(R.string.settings_clear_icon_cache_success, SnackbarType.SUCCESS),
    ClearIconCacheError(R.string.settings_clear_icon_cache_error, SnackbarType.ERROR)
}
