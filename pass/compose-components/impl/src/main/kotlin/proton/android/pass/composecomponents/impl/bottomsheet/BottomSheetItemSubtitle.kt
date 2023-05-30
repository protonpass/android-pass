package proton.android.pass.composecomponents.impl.bottomsheet

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallWeak

@Composable
fun BottomSheetItemSubtitle(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = Color.Unspecified,
    maxLines: Int = 5
) {
    Text(
        modifier = modifier,
        text = text,
        style = ProtonTheme.typography.defaultSmallWeak,
        color = color,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
    )
}
