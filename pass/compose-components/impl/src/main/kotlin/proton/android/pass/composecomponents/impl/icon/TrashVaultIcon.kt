package proton.android.pass.composecomponents.impl.icon

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider

@Composable
fun TrashVaultIcon(
    modifier: Modifier = Modifier,
    size: Int = 40,
    iconSize: Int = 20,
    onClick: (() -> Unit)? = null
) {
    VaultIcon(
        modifier = modifier,
        backgroundColor = PassTheme.colors.textDisabled,
        iconColor = PassTheme.colors.textWeak,
        iconSize = iconSize,
        size = size,
        icon = me.proton.core.presentation.R.drawable.ic_proton_trash,
        onClick = onClick
    )
}

@Preview
@Composable
fun TrashVaultIconPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            TrashVaultIcon()
        }
    }
}
