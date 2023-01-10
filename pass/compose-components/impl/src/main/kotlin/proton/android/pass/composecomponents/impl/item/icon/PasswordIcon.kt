package proton.android.pass.composecomponents.impl.item.icon

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.presentation.R
import proton.android.pass.commonui.api.ThemePreviewProvider

@Composable
fun PasswordIcon(
    modifier: Modifier = Modifier
) {
    RoundedTintedIcon(
        modifier = modifier,
        color = ProtonTheme.colors.notificationSuccess,
        icon = R.drawable.ic_proton_lock
    )
}

@Preview
@Composable
fun PasswordIconPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    ProtonTheme(isDark = isDark) {
        Surface {
            PasswordIcon()
        }
    }
}
