package proton.android.pass.composecomponents.impl.icon

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.R

@Composable
fun VaultIcon(
    modifier: Modifier = Modifier,
    color: Color,
    size: Int = 40,
    @DrawableRes icon: Int,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .size(size.dp)
            .background(color.copy(alpha = 0.4f))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(id = icon),
            contentDescription = stringResource(R.string.vault_selector_icon_content_description),
            tint = color
        )
    }
}

@Preview
@Composable
fun VaultIconPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            VaultIcon(
                color = PassTheme.colors.accentYellowNorm,
                icon = me.proton.core.presentation.R.drawable.ic_proton_house,
            )
        }
    }
}
