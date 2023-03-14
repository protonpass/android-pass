package proton.android.pass.featureitemcreate.impl.alias.suffixes

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.collections.immutable.ImmutableList
import me.proton.core.compose.component.ProtonAlertDialog
import me.proton.core.compose.component.ProtonDialogTitle
import me.proton.core.compose.component.ProtonTextButton
import proton.android.pass.featureitemcreate.impl.R
import proton.android.pass.featureitemcreate.impl.alias.AliasSuffixUiModel

@Composable
fun SelectSuffixDialog(
    modifier: Modifier = Modifier,
    show: Boolean,
    suffixes: ImmutableList<AliasSuffixUiModel>,
    selectedSuffix: AliasSuffixUiModel?,
    color: Color,
    onSuffixChanged: (AliasSuffixUiModel) -> Unit,
    onDismiss: () -> Unit,
) {
    if (!show) return

    val suffix = selectedSuffix ?: suffixes.firstOrNull()
    var suffixState by remember { mutableStateOf(suffix) }

    ProtonAlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        confirmButton = {
            ProtonTextButton(onClick = {
                suffixState?.let { onSuffixChanged(it) }
            }) {
                Text(
                    text = stringResource(R.string.alias_mailbox_dialog_confirm_button),
                    color = color,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        dismissButton = {
            ProtonTextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.alias_mailbox_dialog_cancel_button),
                    color = color,
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
                selectedSuffix = suffixState,
                color = color,
                onSuffixChanged = {
                    suffixState = it
                }
            )
        }
    )
}
