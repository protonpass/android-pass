package proton.android.pass.composecomponents.impl.form

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallWeak

@Composable
fun ProtonTextFieldLabel(
    modifier: Modifier = Modifier,
    text: String,
    isError: Boolean = false
) {
    Text(
        modifier = modifier,
        text = text,
        style = if (isError) {
            ProtonTheme.typography.defaultSmallWeak.copy(color = ProtonTheme.colors.notificationError)
        } else {
            ProtonTheme.typography.defaultSmallWeak
        }
    )
}
