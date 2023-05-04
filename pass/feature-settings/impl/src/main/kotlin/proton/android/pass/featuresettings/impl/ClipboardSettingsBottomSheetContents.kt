package proton.android.pass.featuresettings.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.captionWeak
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetTitle
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.setting.SettingOption
import proton.android.pass.composecomponents.impl.setting.SettingToggle
import proton.android.pass.preferences.ClearClipboardPreference
import proton.android.pass.preferences.CopyTotpToClipboard
import proton.android.pass.preferences.value

@Composable
fun ClipboardBottomSheetContents(
    modifier: Modifier = Modifier,
    state: ClipboardSettingsUIState,
    onClearClipboardSettingClick: () -> Unit,
    onCopyTotpSettingClick: (Boolean) -> Unit,
) {
    Column(
        modifier = modifier
            .bottomSheet(horizontalPadding = PassTheme.dimens.bottomsheetHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BottomSheetTitle(title = stringResource(R.string.clipboard_bottomsheet_title))
        Column(
            modifier = Modifier.roundedContainerNorm()
        ) {
            val clearClipboardString = when (state.clearClipboardPreference) {
                ClearClipboardPreference.Never ->
                    stringResource(R.string.clipboard_option_clear_clipboard_never)
                ClearClipboardPreference.S60 ->
                    stringResource(R.string.clipboard_option_clear_clipboard_after_60_seconds)
                ClearClipboardPreference.S180 ->
                    stringResource(R.string.clipboard_option_clear_clipboard_after_180_seconds)
            }
            SettingOption(
                text = clearClipboardString,
                label = stringResource(R.string.clipboard_option_clear_clipboard_label),
                onClick = onClearClipboardSettingClick
            )
            Divider(color = PassTheme.colors.inputBorderNorm)
            SettingToggle(
                text = stringResource(R.string.clipboard_option_copy_totp_code),
                isChecked = state.isCopyTotpToClipboardEnabled.value(),
                onClick = onCopyTotpSettingClick,
            )
        }
        Text(
            text = stringResource(R.string.clipboard_option_copy_totp_code_hint),
            style = ProtonTheme.typography.captionWeak
        )
    }
}


@Preview
@Composable
fun ClipboardBottomSheetContentsPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            ClipboardBottomSheetContents(
                state = ClipboardSettingsUIState(
                    isCopyTotpToClipboardEnabled = CopyTotpToClipboard.Enabled,
                    clearClipboardPreference = ClearClipboardPreference.Never
                ),
                onClearClipboardSettingClick = {},
                onCopyTotpSettingClick = {},
            )
        }
    }
}
