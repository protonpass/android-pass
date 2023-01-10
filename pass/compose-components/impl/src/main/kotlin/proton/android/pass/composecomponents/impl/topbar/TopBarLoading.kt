package proton.android.pass.composecomponents.impl.topbar

import androidx.compose.foundation.layout.size
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.composecomponents.impl.loading.Loading
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.ThemePreviewProvider

@Composable
fun TopBarLoading(
    modifier: Modifier = Modifier
) {
    Loading(
        modifier = modifier.size(20.dp),
        strokeWidth = 2.dp
    )
}

@Preview
@Composable
fun TopBarLoadingPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    ProtonTheme(isDark = isDark) {
        Surface {
            TopBarLoading()
        }
    }
}
