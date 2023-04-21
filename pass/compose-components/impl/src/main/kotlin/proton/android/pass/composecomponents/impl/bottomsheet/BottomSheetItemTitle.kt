package proton.android.pass.composecomponents.impl.bottomsheet

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme

@Composable
fun BottomSheetItemTitle(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = PassTheme.colors.textNorm
) {
    Text(
        modifier = modifier,
        text = text,
        style = ProtonTheme.typography.defaultNorm,
        color = color
    )
}
