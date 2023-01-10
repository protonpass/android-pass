package proton.android.pass.presentation.settings

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
import me.proton.core.compose.theme.default
import proton.android.pass.commonui.api.ThemePreviewProvider

@Composable
fun SettingPreferenceTitle(
    modifier: Modifier = Modifier,
    text: String
) {
    Text(
        modifier = modifier.fillMaxWidth(),
        fontWeight = FontWeight.W400,
        fontSize = 16.sp,
        color = ProtonTheme.colors.textNorm,
        style = ProtonTheme.typography.default,
        text = text
    )
}

@Preview
@Composable
fun SettingPreferenceTitlePreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    ProtonTheme(isDark = isDark) {
        Surface {
            SettingPreferenceTitle(text = "Some test")
        }
    }
}

