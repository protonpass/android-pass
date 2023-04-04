package proton.android.pass.composecomponents.impl.form

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.R

@Composable
fun SmallCrossIconButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = PassTheme.colors.textHint,
    onClick: () -> Unit
) {
    IconButton(
        modifier = modifier,
        enabled = enabled,
        onClick = { onClick() }
    ) {
        Icon(
            painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_cross_small),
            contentDescription = stringResource(R.string.small_cross_icon_content_description),
            tint = tint,
        )
    }
}
