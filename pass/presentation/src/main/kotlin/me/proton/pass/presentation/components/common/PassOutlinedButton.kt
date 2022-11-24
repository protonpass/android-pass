package me.proton.pass.presentation.components.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.component.ProtonButton
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.commonui.api.ThemePreviewProvider

@Composable
fun PassOutlinedButton(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = ProtonTheme.colors.brandNorm,
    onClick: () -> Unit
) {
    ProtonButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = ButtonDefaults.MinHeight),
        shape = ProtonTheme.shapes.medium,
        border = BorderStroke(ButtonDefaults.OutlinedBorderSize, color),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = ProtonTheme.colors.backgroundNorm,
            contentColor = color
        ),
        elevation = null
    ) {
        Text(
            text = text,
            color = color
        )
    }
}

@Preview
@Composable
fun PassOutlinedButtonPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    ProtonTheme(isDark = isDark) {
        Surface {
            PassOutlinedButton(
                text = "This is an example button",
                onClick = {}
            )
        }
    }
}
