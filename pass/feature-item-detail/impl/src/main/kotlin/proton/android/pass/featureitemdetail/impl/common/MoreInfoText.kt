package proton.android.pass.featureitemdetail.impl.common

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.overlineWeak

@Composable
fun MoreInfoText(modifier: Modifier = Modifier, text: String) {
    Text(
        modifier = modifier,
        text = text,
        style = ProtonTheme.typography.overlineWeak,
        maxLines = 1
    )
}
