package proton.android.pass.composecomponents.impl.icon

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.extension.toColor
import proton.pass.domain.ShareColor

@Composable
fun AllVaultsIcon(
    modifier: Modifier = Modifier,
    size: Int = 40,
    iconSize: Int = 20,
    onClick: (() -> Unit)? = null
) {
    VaultIcon(
        modifier = modifier,
        backgroundColor = ShareColor.Color1.toColor(true),
        iconColor = ShareColor.Color1.toColor(),
        icon = proton.android.pass.commonui.api.R.drawable.ic_vault_squares,
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
