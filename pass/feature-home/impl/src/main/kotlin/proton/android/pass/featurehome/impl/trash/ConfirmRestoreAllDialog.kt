package proton.android.pass.featurehome.impl.trash

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.composecomponents.impl.dialogs.ConfirmWithLoadingDialog
import proton.android.pass.featurehome.impl.R
import me.proton.core.presentation.R as CoreR

@Composable
internal fun ConfirmRestoreAllDialog(
    show: Boolean,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    ConfirmWithLoadingDialog(
        show = show,
        isLoading = isLoading,
        isConfirmActionDestructive = false,
        title = stringResource(R.string.alert_confirm_restore_all_title),
        message = stringResource(R.string.alert_confirm_restore_all_message),
        confirmText = stringResource(id = CoreR.string.presentation_alert_ok),
        cancelText = stringResource(id = CoreR.string.presentation_alert_cancel),
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        onCancel = onDismiss
    )
}

@Preview
@Composable
fun ConfirmRestoreAllDialogPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    PassTheme(isDark = input.first) {
        Surface {
            ConfirmRestoreAllDialog(
                show = true,
                isLoading = input.second,
                onDismiss = {},
                onConfirm = {}
            )
        }
    }
}
