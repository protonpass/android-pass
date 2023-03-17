package proton.android.pass.composecomponents.impl.bottomsheet

import androidx.annotation.DrawableRes
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.R

@Composable
fun BottomSheetItemIcon(
    modifier: Modifier = Modifier,
    @DrawableRes iconId: Int,
    tint: Color = PassTheme.colors.textWeak
) {
    Icon(
        modifier = modifier,
        painter = painterResource(iconId),
        contentDescription = stringResource(id = R.string.bottomsheet_content_description_item_icon),
        tint = tint
    )
}
