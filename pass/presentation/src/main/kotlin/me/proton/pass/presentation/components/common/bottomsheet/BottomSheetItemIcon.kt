package me.proton.pass.presentation.components.common.bottomsheet

import androidx.annotation.DrawableRes
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import me.proton.pass.presentation.R

@Composable
fun BottomSheetItemIcon(modifier: Modifier = Modifier, @DrawableRes iconId: Int) {
    Icon(
        modifier = modifier,
        imageVector = ImageVector.vectorResource(iconId),
        contentDescription = stringResource(id = R.string.bottomsheet_content_description_item_icon)
    )
}
