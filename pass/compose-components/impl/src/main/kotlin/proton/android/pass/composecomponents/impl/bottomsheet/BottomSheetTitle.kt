package proton.android.pass.composecomponents.impl.bottomsheet

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider

data class BottomSheetTitleButton(
    val title: String,
    val onClick: () -> Unit,
    val enabled: Boolean
)

@Composable
fun BottomSheetTitle(
    modifier: Modifier = Modifier,
    title: String,
    button: BottomSheetTitleButton? = null,
    showDivider: Boolean = true
) {
    Row(
        modifier = modifier.height(40.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            modifier = Modifier.weight(1.0f),
            fontWeight = FontWeight.W500
        )
        if (button != null) {
            IconButton(
                onClick = button.onClick,
                enabled = button.enabled,
                modifier = Modifier.padding(end = 10.dp)
            ) {
                val textColor = if (button.enabled) {
                    ProtonTheme.colors.brandNorm
                } else {
                    ProtonTheme.colors.interactionDisabled
                }
                Text(
                    text = button.title,
                    color = textColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W500
                )
            }
        }
    }
    if (showDivider) {
        Divider(modifier = Modifier.fillMaxWidth())
    }
}

class ThemeAndBottomSheetProvider :
    ThemePairPreviewProvider<BottomSheetTitleButton?>(BottomSheetTitlePreviewProvider())

@Preview
@Composable
fun BottomSheetTitlePreview(
    @PreviewParameter(ThemeAndBottomSheetProvider::class)
    input: Pair<Boolean, BottomSheetTitleButton?>
) {
    ProtonTheme(isDark = input.first) {
        Surface {
            BottomSheetTitle(
                title = "Generate Password",
                button = input.second
            )
        }
    }
}
