package me.proton.android.pass.featurecreateitem.impl.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.android.pass.featurecreateitem.impl.R
import me.proton.core.compose.component.ProtonAlertDialog
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.default
import me.proton.core.compose.theme.headlineSmall
import me.proton.pass.commonui.api.ThemePreviewProvider

@Composable
fun ConfirmRemoveAliasDialog(
    modifier: Modifier = Modifier,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ProtonAlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = {},
        confirmButton = {
            Text(
                modifier = Modifier
                    .clickable { onConfirm() }
                    .padding(8.dp),
                text = stringResource(R.string.delete_login_alias_dialog_confirm),
                color = ProtonTheme.colors.notificationError,
                style = ProtonTheme.typography.headlineSmall
            )
        },
        dismissButton = {
            Text(
                modifier = Modifier
                    .clickable { onCancel() }
                    .padding(8.dp),
                text = stringResource(R.string.delete_login_alias_dialog_cancel),
                color = ProtonTheme.colors.brandNorm,
                style = ProtonTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = stringResource(R.string.delete_login_alias_dialog_text),
                style = ProtonTheme.typography.default
            )
        }
    )
}

@Preview
@Composable
fun ConfirmRemoveAliasDialogPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    ProtonTheme(isDark = isDark) {
        Surface {
            ConfirmRemoveAliasDialog(
                onCancel = {},
                onConfirm = {},
                onDismiss = {}
            )
        }
    }
}
