package proton.android.pass.featurevault.impl.bottomsheet

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.extension.toColor
import proton.pass.domain.ShareColor

private const val ITEMS_PER_COLUMN = 5

@Composable
fun ColorSelectionSection(
    modifier: Modifier = Modifier,
    selected: ShareColor,
    onColorSelected: (ShareColor) -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        ShareColor.values().toList().chunked(ITEMS_PER_COLUMN).forEach { rowColors ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                rowColors.forEach { shareColor ->
                    ColorSelector(
                        color = shareColor.toColor(),
                        selectedColor = shareColor.toColor(true),
                        isSelected = shareColor == selected,
                        onClick = {
                            onColorSelected(shareColor)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ColorSelector(
    modifier: Modifier = Modifier,
    color: Color,
    selectedColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Canvas(
        modifier = modifier
            .size(54.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
    ) {
        if (isSelected) {
            drawCircle(color = selectedColor, style = Stroke(width = 20f))
        }
        drawCircle(color = color, radius = 54.dp.value)
    }
}

@Preview
@Composable
fun ColorSelectionSectionPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            ColorSelectionSection(
                selected = ShareColor.Color3,
                onColorSelected = {}
            )
        }
    }
}

