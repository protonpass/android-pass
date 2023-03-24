package proton.android.pass.composecomponents.impl.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTypography
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.buttons.CircleButton

@Composable
fun BottomSheetCancelConfirm(
    modifier: Modifier = Modifier,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        CircleButton(
            modifier = Modifier.weight(1f),
            color = PassTheme.colors.textDisabled,
            onClick = onCancel
        ) {
            Text(
                text = stringResource(R.string.bottomsheet_cancel_button),
                style = PassTypography.body3RegularWeak
            )
        }
        CircleButton(
            modifier = Modifier.weight(1f),
            color = PassTheme.colors.loginInteractionNormMajor1,
            onClick = onConfirm
        ) {
            Text(
                text = stringResource(R.string.bottomsheet_confirm_button),
                style = PassTypography.body3RegularInverted
            )
        }
    }
}

@Preview
@Composable
fun BottomsheetCancelConfirmPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            BottomSheetCancelConfirm(onCancel = {}, onConfirm = {})
        }
    }
}

