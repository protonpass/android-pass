package proton.android.pass.composecomponents.impl.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.proton.core.compose.component.ProtonButton
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider

@Composable
fun PassOutlinedButton(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = ProtonTheme.colors.brandNorm,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    ProtonButton(
        onClick = onClick,
        modifier = modifier,
        shape = ProtonTheme.shapes.medium,
        border = BorderStroke(ButtonDefaults.OutlinedBorderSize, color),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = ProtonTheme.colors.backgroundNorm,
            contentColor = color
        ),
        elevation = null,
        contentPadding = PaddingValues(horizontal = 36.dp, vertical = 12.dp),
        enabled = enabled
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.W400,
            fontSize = 16.sp,
            color = color
        )
    }
}

@Preview
@Composable
fun PassOutlinedButtonPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            PassOutlinedButton(
                text = "This is an example button",
                onClick = {}
            )
        }
    }
}
