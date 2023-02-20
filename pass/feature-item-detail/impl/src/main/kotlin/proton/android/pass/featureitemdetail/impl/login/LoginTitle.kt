package proton.android.pass.featureitemdetail.impl.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.item.icon.LoginIcon
import proton.android.pass.featureitemdetail.impl.common.ItemTitleText

@Composable
fun LoginTitle(modifier: Modifier = Modifier, title: String) {
    Row(
        modifier = modifier.height(75.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LoginIcon(size = 60, text = title)
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
