package proton.android.pass.composecomponents.impl.icon

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.extension.toBackgroundColor
import proton.android.pass.composecomponents.impl.extension.toIconColor
import proton.pass.domain.ShareColor

@Composable
fun AllVaultsIcon(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    VaultIcon(
        modifier = modifier,
        backgroundColor = ShareColor.Color1.toBackgroundColor(),
        iconColor = ShareColor.Color1.toIconColor(),
        icon = proton.android.pass.commonui.api.R.drawable.ic_vault_squares,
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
