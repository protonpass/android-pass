package proton.android.pass.featurecreateitem.impl.alias.suffixes

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import me.proton.core.compose.component.ProtonAlertDialog
import me.proton.core.compose.component.ProtonDialogTitle
import me.proton.core.compose.component.ProtonTextButton
import proton.android.pass.commonui.api.PassColors
import proton.android.pass.featurecreateitem.impl.R
import proton.android.pass.featurecreateitem.impl.alias.AliasSuffixUiModel

@Composable
fun SelectSuffixDialog(
    modifier: Modifier = Modifier,
    show: Boolean,
    suffixes: List<AliasSuffixUiModel>,
    selectedSuffix: AliasSuffixUiModel?,
    onSuffixChanged: (AliasSuffixUiModel) -> Unit,
    onDismiss: () -> Unit,
) {
    if (!show) return

    ProtonAlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            ProtonTextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.alias_mailbox_dialog_cancel_button),
                    color = PassColors.Dark.textNorm,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        title = {
            ProtonDialogTitle(title = stringResource(R.string.alias_bottomsheet_suffix_title))
        },
        text = {
            SelectSuffixContent(
                suffixes = suffixes,
                selectedSuffix = selectedSuffix,
                onSuffixChanged = onSuffixChanged
            )
        }
    )
}
