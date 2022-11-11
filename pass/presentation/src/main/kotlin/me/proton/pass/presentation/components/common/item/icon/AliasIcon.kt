package me.proton.pass.presentation.components.common.item.icon

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.presentation.R
import me.proton.pass.commonui.api.ThemePreviewProvider

@Composable
fun AliasIcon(
    modifier: Modifier = Modifier
) {
    RoundedTintedIcon(
        modifier = modifier,
        color = ProtonTheme.colors.textNorm,
        icon = R.drawable.ic_proton_alias
    )
}

@Preview
@Composable
fun AliasIconPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    ProtonTheme(isDark = isDark) {
        Surface {
            AliasIcon()
        }
    }
}
