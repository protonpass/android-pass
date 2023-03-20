package proton.android.pass.featurehome.impl.trash

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import proton.android.pass.composecomponents.impl.dialogs.ConfirmWithLoadingDialog
import proton.android.pass.featurehome.impl.R

@Composable
internal fun ConfirmDeleteItemDialog(
    show: Boolean,
    isLoading: Boolean,
    itemName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    ConfirmWithLoadingDialog(
        show = show,
        isLoading = isLoading,
        isConfirmActionDestructive = true,
        title = stringResource(R.string.alert_confirm_delete_item_dialog_title),
        message = stringResource(R.string.alert_confirm_delete_item_dialog_message, itemName),
        confirmText = stringResource(id = me.proton.core.presentation.R.string.presentation_alert_ok),
        cancelText = stringResource(id = me.proton.core.presentation.R.string.presentation_alert_cancel),
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        onCancel = onDismiss
    )
}
