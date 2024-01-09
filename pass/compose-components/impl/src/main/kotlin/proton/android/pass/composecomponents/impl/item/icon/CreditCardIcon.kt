package proton.android.pass.composecomponents.impl.item.icon

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.container.BoxedIcon

@Composable
fun CreditCardIcon(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: Int = 40,
    shape: Shape = PassTheme.shapes.squircleMediumShape,
    backgroundColor: Color = if (enabled) {
        PassTheme.colors.cardInteractionNormMinor1
    } else {
        PassTheme.colors.cardInteractionNormMinor2
    },
    foregroundColor: Color = if (enabled) {
        PassTheme.colors.cardInteractionNormMajor2
    } else {
        PassTheme.colors.cardInteractionNormMinor1
    }
) {
    BoxedIcon(
        modifier = modifier,
        backgroundColor = backgroundColor,
        size = size,
        shape = shape
    ) {
        Icon(
            modifier = Modifier.padding(4.dp),
            painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_credit_card),
            contentDescription = null,
            tint = foregroundColor
        )
    }
}

@Preview
@Composable
fun CreditCardIconPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            CreditCardIcon(shape = PassTheme.shapes.squircleMediumShape)
        }
    }
}
