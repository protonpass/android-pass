package proton.android.pass.composecomponents.impl.buttons

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.captionStrongNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider

@Composable
fun TransparentTextButton(
    modifier: Modifier = Modifier,
    text: String,
    @DrawableRes icon: Int? = null,
    iconContentDescription: String? = null,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier,
        elevation = null,
        colors = ButtonDefaults.buttonColors(Color.Transparent),
        onClick = onClick
    ) {
        if (icon != null) {
            Icon(
                modifier = Modifier.size(16.dp),
                painter = painterResource(icon),
                contentDescription = iconContentDescription,
                tint = color
            )
            Spacer(modifier = Modifier.width(5.dp))
        }
        Text(
            text = text,
            style = ProtonTheme.typography.captionStrongNorm,
            color = color,
            fontSize = 14.sp
        )
    }
}

@Preview
@Composable
fun TransparentTextButtonPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            TransparentTextButton(
                text = "A button",
                icon = me.proton.core.presentation.compose.R.drawable.ic_proton_plus,
                iconContentDescription = null,
                color = PassTheme.colors.interactionNormMajor2,
                onClick = {}
            )
        }
    }
}
