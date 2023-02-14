package proton.android.pass.featureitemdetail.impl.alias

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassColors
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.container.Circle
import proton.android.pass.featureitemdetail.impl.R
import proton.android.pass.featureitemdetail.impl.common.ItemTitleText

@Composable
fun AliasTitle(modifier: Modifier = Modifier, title: String) {
    Row(
        modifier = modifier.height(75.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Circle(backgroundColor = PassColors.GreenAccent, size = 60) {
            Icon(
                painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_alias),
                contentDescription = stringResource(R.string.alias_title_icon_content_description),
                tint = PassColors.GreenAccent
            )
        }
        ItemTitleText(text = title)
    }
}

@Preview
@Composable
fun AliasTitlePreview(
    @PreviewParameter(ThemePreviewProvider::class) input: Boolean
) {
    ProtonTheme(isDark = input) {
        Surface {
            AliasTitle(title = "A really long title to check if the element is multiline")
        }
    }
}
