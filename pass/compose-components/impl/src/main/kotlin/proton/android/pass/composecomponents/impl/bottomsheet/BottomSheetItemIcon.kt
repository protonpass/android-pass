package proton.android.pass.composecomponents.impl.bottomsheet

import androidx.annotation.DrawableRes
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import proton.android.pass.composecomponents.impl.R

@Composable
fun BottomSheetItemIcon(
    modifier: Modifier = Modifier,
    @DrawableRes iconId: Int,
    tint: Color = LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
) {
    Icon(
        modifier = modifier,
        imageVector = ImageVector.vectorResource(iconId),
        contentDescription = stringResource(id = R.string.bottomsheet_content_description_item_icon),
        tint = tint
    )
}
