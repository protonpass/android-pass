package proton.android.pass.composecomponents.impl.dialogs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NoPaddingDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    shape: Shape = MaterialTheme.shapes.medium,
    backgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = contentColorFor(backgroundColor),
    properties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        Surface(
            modifier = modifier.fixAlertDialogSize(),
            shape = shape,
            color = backgroundColor,
            contentColor = contentColor
        ) {
            content()
        }
    }
}


private fun Modifier.fixAlertDialogSize() = fillMaxWidth(fraction = ALERT_DIALOG_WIDTH_FRACTION)
    .widthIn(max = MaxAlertDialogWidth)

private const val ALERT_DIALOG_WIDTH_FRACTION = 0.9f

// Mobile alert on desktop is 560dp wide
// https://material.io/components/dialogs#specs
private val MaxAlertDialogWidth = 560.dp
