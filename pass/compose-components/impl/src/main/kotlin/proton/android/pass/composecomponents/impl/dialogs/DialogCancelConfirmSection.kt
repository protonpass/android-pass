package proton.android.pass.composecomponents.impl.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.component.ProtonTextButton
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import me.proton.core.presentation.compose.R as CoreR

@Composable
fun DialogCancelConfirmSection(
    modifier: Modifier = Modifier,
    cancelText: String = stringResource(CoreR.string.presentation_alert_cancel),
    confirmText: String = stringResource(CoreR.string.presentation_alert_ok),
    color: Color,
    disabledColor: Color = color,
    confirmEnabled: Boolean = true,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val confirmColor = if (confirmEnabled) color else disabledColor
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        ProtonTextButton(onClick = onDismiss) {
            Text(
                text = cancelText,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        ProtonTextButton(
            enabled = confirmEnabled,
            onClick = onConfirm
        ) {
            Text(
                text = confirmText,
                color = confirmColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview
@Composable
fun DialogCancelConfirmSectionPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    PassTheme(isDark = input.first) {
        Surface {
            DialogCancelConfirmSection(
                color = PassTheme.colors.loginInteractionNorm,
                confirmEnabled = input.second,
                onConfirm = {},
                onDismiss = {}
            )
        }
    }
}
