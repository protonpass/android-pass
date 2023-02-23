package proton.android.pass.composecomponents.impl.item.icon

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
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
        color = PassTheme.colors.accentPurpleOpaque,
        size = size
    )
}

@Preview
@Composable
fun LoginIconPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            LoginIcon(text = "login text")
        }
    }
}
