package proton.android.pass.composecomponents.impl.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTypography
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.buttons.LoadingCircleButton
import proton.android.pass.composecomponents.impl.buttons.UpgradeButton

@Composable
fun BottomSheetCancelConfirm(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    showUpgrade: Boolean = false,
    confirmText: String = stringResource(R.string.bottomsheet_confirm_button),
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    onUpgradeClick: () -> Unit = {}
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        // Even if we don't display the loading in the Cancel button, we reuse the same component
        // so it has the exact same height
        LoadingCircleButton(
            modifier = Modifier.weight(1f),
            isLoading = false,
            color = PassTheme.colors.textDisabled,
            onClick = { if (!isLoading) onCancel() },
            buttonHeight = 26.dp,
            text = {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.bottomsheet_cancel_button),
                    style = PassTypography.body3RegularWeak,
                    textAlign = TextAlign.Center
                )
            }
        )
        if (showUpgrade) {
            UpgradeButton(
                modifier = Modifier
                    .weight(1f)
                    .height(45.dp),
                color = PassTheme.colors.loginInteractionNormMajor1,
                onUpgradeClick = onUpgradeClick
            )
        } else {
            LoadingCircleButton(
                modifier = Modifier.weight(1f),
                isLoading = isLoading,
                color = PassTheme.colors.loginInteractionNormMajor1,
                onClick = onConfirm,
                buttonHeight = 26.dp,
                text = {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = confirmText,
                        style = PassTypography.body3RegularInverted,
                        textAlign = TextAlign.Center,
                        color = PassTheme.colors.textInvert
                    )
                }
            )
        }
    }

}

@Preview
@Composable
fun BottomSheetCancelConfirmPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    PassTheme(isDark = input.first) {
        Surface {
            BottomSheetCancelConfirm(
                isLoading = input.second,
                onCancel = {},
                onConfirm = {}
            )
        }
    }
}
