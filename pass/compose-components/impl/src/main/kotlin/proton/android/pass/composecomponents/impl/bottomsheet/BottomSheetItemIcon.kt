package proton.android.pass.composecomponents.impl.bottomsheet

import androidx.annotation.DrawableRes
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import proton.android.pass.commonui.api.PassTheme

@Composable
fun BottomSheetItemIcon(
    modifier: Modifier = Modifier,
    @DrawableRes iconId: Int,
    tint: Color = PassTheme.colors.textNorm
) {
    Icon(
        modifier = modifier,
        painter = painterResource(iconId),
        contentDescription = null,
        tint = tint
    )
}
