package proton.android.pass.composecomponents.impl.topbar.iconbutton

import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.container.Circle

@Composable
fun BackArrowCircleIconButton(
    modifier: Modifier = Modifier,
    color: Color,
    onUpClick: () -> Unit
) {
    Circle(
        modifier = modifier,
        backgroundColor = color,
        onClick = onUpClick
    ) {
        Icon(
            painter = painterResource(me.proton.core.presentation.R.drawable.ic_arrow_back),
            contentDescription = stringResource(R.string.navigate_back_icon_content_description),
            tint = color
        )
    }
}
