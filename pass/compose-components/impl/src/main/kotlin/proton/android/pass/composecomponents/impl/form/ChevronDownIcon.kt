package proton.android.pass.composecomponents.impl.form

import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import proton.android.pass.commonui.api.PassTheme
import me.proton.core.presentation.R as CoreR

@Composable
fun ChevronDownIcon(
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color = PassTheme.colors.textHint
) {
    Icon(
        modifier = modifier,
        painter = painterResource(CoreR.drawable.ic_proton_chevron_down),
        contentDescription = contentDescription,
        tint = tint,
    )
}
