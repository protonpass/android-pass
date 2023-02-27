package proton.android.pass.composecomponents.impl.icon

import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import proton.android.pass.composecomponents.impl.R

@Composable
fun ForwardIcon(
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
) {
    Icon(
        modifier = modifier,
        painter = painterResource(R.drawable.ic_forward),
        contentDescription = stringResource(R.string.forward_icon_content_description),
        tint = tint
    )
}
