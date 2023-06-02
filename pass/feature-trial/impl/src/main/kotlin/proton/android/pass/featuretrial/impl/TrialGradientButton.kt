package proton.android.pass.featuretrial.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider

@Suppress("MagicNumber")
@Composable
fun TrialGradientButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFC183CE),
                        Color(0xFF714DFF)
                    )
                )
            )
            .clickable { onClick() }
            .padding(vertical = 16.dp),
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = text,
            color = PassTheme.colors.signalNorm,
            style = ProtonTheme.typography.defaultNorm,
            textAlign = TextAlign.Center
        )
    }
}

@Preview
@Composable
fun TrialGradientButtonPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            TrialGradientButton(
                text = "Some text in the button",
                onClick = {}
            )
        }
    }
}
