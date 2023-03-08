package proton.android.pass.composecomponents.impl.icon

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.composecomponents.impl.R

@Composable
fun VaultIcon(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    iconColor: Color,
    size: Int = 40,
    @DrawableRes icon: Int,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .applyIf(onClick != null, ifTrue = { clickable { onClick?.invoke() } })
            .size(size.dp)
            .background(backgroundColor)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(id = icon),
            contentDescription = stringResource(R.string.vault_selector_icon_content_description),
            tint = iconColor
        )
    }
}

@Suppress("MagicNumber")
@Preview
@Composable
fun VaultIconPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            VaultIcon(
                iconColor = Color(0xFFF7D775),
                backgroundColor = Color(0x10F7D775),
                icon = me.proton.core.presentation.R.drawable.ic_proton_house,
            )
        }
    }
}
