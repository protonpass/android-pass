package proton.android.pass.featureitemdetail.impl.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.default
import proton.android.pass.commonui.api.PassColors
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.container.Circle
import proton.android.pass.featureitemdetail.impl.common.ItemTitleText

@Composable
fun LoginTitle(modifier: Modifier = Modifier, title: String) {
    Row(
        modifier = modifier.height(75.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Circle(backgroundColor = PassColors.PurpleAccent, size = 60) {
            Text(
                text = title.filter { !it.isWhitespace() }.take(2).uppercase(),
                color = PassColors.PurpleAccent,
                style = ProtonTheme.typography.default,
                textAlign = TextAlign.Center
            )
        }
        ItemTitleText(text = title)
    }
}

@Preview
@Composable
fun LoginTitlePreview(
    @PreviewParameter(ThemePreviewProvider::class) input: Boolean
) {
    ProtonTheme(isDark = input) {
        Surface {
            LoginTitle(title = "A really long title to check if the element is multiline")
        }
    }
}
