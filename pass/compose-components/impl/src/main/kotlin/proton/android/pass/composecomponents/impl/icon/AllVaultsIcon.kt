package proton.android.pass.composecomponents.impl.icon

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.R

@Composable
fun AllVaultsIcon(
    modifier: Modifier = Modifier,
    size: Int = 40,
    iconSize: Int = 20,
    onClick: (() -> Unit)? = null
) {
    VaultIcon(
        modifier = modifier,
        backgroundColor = PassTheme.colors.loginInteractionNormMinor1,
        iconColor = PassTheme.colors.loginInteractionNormMajor1,
        icon = R.drawable.ic_brand_pass,
        size = size,
        iconSize = iconSize,
        onClick = onClick
    )
}

@Preview
@Composable
fun AllVaultsIconPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            AllVaultsIcon()
        }
    }
}
