package me.proton.pass.presentation.components.common

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme

@Composable
fun RoundedCornersContainer(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {

    var boxModifier = modifier.clip(RoundedCornerShape(12.dp))
    boxModifier = if (onClick != null) {
        boxModifier.clickable { onClick() }
    } else {
        boxModifier
    }

    Box(
        modifier = boxModifier
            .border(
                width = 1.dp,
                color = ProtonTheme.colors.separatorNorm,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        content()
    }
}
