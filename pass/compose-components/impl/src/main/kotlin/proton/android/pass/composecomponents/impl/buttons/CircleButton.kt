package proton.android.pass.composecomponents.impl.buttons

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider

@Composable
fun CircleButton(
    modifier: Modifier = Modifier,
    color: Color,
    contentPadding: PaddingValues = PaddingValues(),
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Button(
        modifier = modifier,
        contentPadding = contentPadding,
        colors = ButtonDefaults.buttonColors(backgroundColor = color),
        shape = CircleShape,
        onClick = onClick
    ) {
        content()
    }
}

@Preview
@Composable
fun CircleButtonTextPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            CircleButton(
                color = PassTheme.colors.accentGreenOpaque,
                content = {
                    Text(text = "A long Label")
                },
                onClick = {}
            )
        }
    }
}
