package me.proton.pass.presentation.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.sp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmall
import me.proton.pass.commonui.api.ThemePreviewProvider

@Composable
fun SettingPreferenceSubtitle(
    modifier: Modifier = Modifier,
    text: String
) {
    Text(
        modifier = modifier.fillMaxWidth(),
        fontWeight = FontWeight.W400,
        fontSize = 14.sp,
        color = ProtonTheme.colors.textWeak,
        style = ProtonTheme.typography.defaultSmall,
        text = text
    )
}

@Preview
@Composable
fun SettingPreferenceSubtitlePreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    ProtonTheme(isDark = isDark) {
        Surface {
            SettingPreferenceSubtitle(text = "Some test")
        }
    }
}
