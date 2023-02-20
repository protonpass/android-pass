package proton.android.pass.composecomponents.impl.form

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultWeak

@Composable
fun ProtonTextFieldPlaceHolder(
    modifier: Modifier = Modifier,
    text: String,
    textStyle: TextStyle = ProtonTheme.typography.defaultWeak
) {
    Text(
        modifier = modifier,
        text = text,
        style = textStyle
    )
}
