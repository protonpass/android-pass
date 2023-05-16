package proton.android.pass.composecomponents.impl.container

import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider

@Composable
fun CircleTextIcon(
    modifier: Modifier = Modifier,
    text: String,
    backgroundColor: Color,
    textColor: Color,
    size: Int = 40,
    shape: Shape
) {
    BoxedIcon(
        modifier = modifier,
        backgroundColor = backgroundColor,
        size = size,
        shape = shape
    ) {
        Text(
            text = text.filter { !it.isWhitespace() }.take(2).uppercase(),
            color = textColor,
            style = ProtonTheme.typography.defaultNorm,
            textAlign = TextAlign.Center
        )
    }
}

@Preview
@Composable
fun CircleTextIconPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            CircleTextIcon(
                text = "This is an example",
                backgroundColor = PassTheme.colors.loginInteractionNormMinor1,
                textColor = PassTheme.colors.loginInteractionNormMajor2,
                shape = PassTheme.shapes.squircleMediumShape
            )
        }
    }
}
