package me.proton.android.pass.composecomponents.impl.item.icon

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun RoundedTintedIcon(
    modifier: Modifier = Modifier,
    color: Color,
    @DrawableRes icon: Int
) {
    Icon(
        painter = painterResource(icon),
        contentDescription = null,
        tint = color,
        modifier = modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color = color.copy(alpha = 0.2f))
            .padding(8.dp)
    )
}
