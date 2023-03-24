package proton.android.pass.composecomponents.impl.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.presentation.R
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider

@Composable
fun CircleIconButton(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    IconButton(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor),
        onClick = onClick,
    ) { content() }
}

@Preview
@Composable
fun CircleIconButtonPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            CircleIconButton(
                backgroundColor = PassTheme.colors.aliasInteractionNormMajor1,
                onClick = {},
                content = {
                    Icon(
                        painter = painterResource(R.drawable.ic_proton_arrows_rotate),
                        contentDescription = null,
                        tint = PassTheme.colors.loginInteractionNormMajor1
                    )
                }
            )
        }
    }
}
