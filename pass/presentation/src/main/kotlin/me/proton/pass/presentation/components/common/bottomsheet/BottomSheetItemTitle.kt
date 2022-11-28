package me.proton.pass.presentation.components.common.bottomsheet

import androidx.annotation.StringRes
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.default

@Composable
fun BottomSheetItemTitle(
    modifier: Modifier = Modifier,
    @StringRes textId: Int,
    textcolor: Color = Color.Unspecified
) {
    Text(
        modifier = modifier,
        text = stringResource(id = textId),
        style = ProtonTheme.typography.default,
        color = textcolor
    )
}
