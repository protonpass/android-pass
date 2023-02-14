package proton.android.pass.featureitemdetail.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallInverted
import proton.android.pass.commonui.api.AccentColorPreviewProvider
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.container.Circle

@ExperimentalComposeUiApi
@Composable
internal fun ItemDetailTopBar(
    modifier: Modifier = Modifier,
    color: Color,
    onUpClick: () -> Unit,
    onEditClick: () -> Unit,
    onOptionsClick: () -> Unit
) {
    ProtonTopAppBar(
        modifier = modifier,
        title = { },
        navigationIcon = {
            Circle(
                modifier = Modifier.padding(4.dp),
                backgroundColor = color,
                onClick = { onUpClick() }
            ) {
                Icon(
                    painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_chevron_left),
                    contentDescription = null,
                    tint = color
                )
            }
        },
        actions = {
            Row(
                modifier = Modifier
                    .height(48.dp)
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .clickable { onEditClick() }
                        .background(color)
                        .padding(16.dp, 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_pencil),
                        contentDescription = stringResource(R.string.top_bar_edit_icon_content_description),
                        tint = ProtonTheme.colors.iconInverted
                    )
                    Text(
                        text = stringResource(R.string.top_bar_edit_button_text),
                        style = ProtonTheme.typography.defaultSmallInverted
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Circle(
                    backgroundColor = color,
                    onClick = { onOptionsClick() }
                ) {
                    Icon(
                        painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_three_dots_vertical),
                        contentDescription = null,
                        tint = color
                    )
                }
            }
        }
    )
}

class ThemeAndAccentColorProvider : ThemePairPreviewProvider<Color>(AccentColorPreviewProvider())

@OptIn(ExperimentalComposeUiApi::class)
@Preview
@Composable
fun ItemDetailTopBarPreview(
    @PreviewParameter(ThemeAndAccentColorProvider::class) input: Pair<Boolean, Color>
) {
    ProtonTheme(isDark = input.first) {
        Surface {
            ItemDetailTopBar(
                color = input.second,
                onUpClick = {},
                onEditClick = {},
                onOptionsClick = {}
            )
        }
    }
}

