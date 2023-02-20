package proton.android.pass.composecomponents.impl.item.icon

import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassColors
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.container.Circle

@Composable
fun PasswordIcon(
    modifier: Modifier = Modifier,
    size: Int = 40,
) {
    Circle(
        modifier = modifier,
        backgroundColor = PassColors.RedAccent,
        size = size,
    ) {
        Icon(
            painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_key),
            contentDescription = stringResource(R.string.password_title_icon_content_description),
            tint = PassColors.RedAccent
        )
    }
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
