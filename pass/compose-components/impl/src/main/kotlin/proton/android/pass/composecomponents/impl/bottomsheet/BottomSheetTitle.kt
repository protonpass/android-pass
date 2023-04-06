package proton.android.pass.composecomponents.impl.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.default
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider

@Composable
fun BottomSheetTitle(
    modifier: Modifier = Modifier,
    title: String
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = ProtonTheme.typography.default
        )
    }
}

@Preview
@Composable
fun BottomSheetTitlePreview(
    @PreviewParameter(ThemePreviewProvider::class)
    input: Boolean
) {
    PassTheme(isDark = input) {
        Surface {
            BottomSheetTitle(
                title = "Generate password"
            )
        }
    }
}
