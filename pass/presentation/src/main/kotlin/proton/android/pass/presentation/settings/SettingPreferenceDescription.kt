package proton.android.pass.presentation.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmall
import proton.android.pass.commonui.api.ThemePreviewProvider

@Composable
fun SettingPreferenceDescription(
    modifier: Modifier = Modifier,
    text: String
) {
    Text(
        modifier = modifier.fillMaxWidth(),
        color = ProtonTheme.colors.textHint,
        text = text,
        style = ProtonTheme.typography.defaultSmall
    )
}

@Preview
@Composable
fun SettingPreferenceDescriptionPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    ProtonTheme(isDark = isDark) {
        Surface {
            SettingPreferenceDescription(text = "Some test")
        }
    }
}

