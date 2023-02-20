package proton.android.pass.composecomponents.impl.item.icon

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassColors
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.container.CircleTextIcon

@Composable
fun LoginIcon(
    modifier: Modifier = Modifier,
    text: String,
    size: Int = 40,
) {
    CircleTextIcon(
        modifier = modifier,
        text = text,
        color = PassColors.PurpleAccent,
        size = size
    )
}

@Preview
@Composable
fun LoginIconPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    ProtonTheme(isDark = isDark) {
        Surface {
            LoginIcon(text = "login text")
        }
    }
}
