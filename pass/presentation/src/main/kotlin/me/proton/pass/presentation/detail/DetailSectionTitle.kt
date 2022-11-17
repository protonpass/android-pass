package me.proton.pass.presentation.detail

import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.sp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.commonui.api.ThemePreviewProvider

@Composable
fun DetailSectionTitle(
    modifier: Modifier = Modifier,
    text: String
) {
    Text(
        modifier = modifier,
        text = text,
        fontWeight = FontWeight.W500,
        fontSize = 14.sp,
        color = ProtonTheme.colors.textNorm
    )
}

@Preview
@Composable
fun DetailSectionTitlePreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    ProtonTheme(isDark = isDark) {
        Surface {
            DetailSectionTitle(
                text = "This is a section title"
            )
        }
    }
}
