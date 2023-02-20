package proton.android.pass.featureitemdetail.impl

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallInverted
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.buttons.LoadingCircleButton
import proton.android.pass.composecomponents.impl.container.Circle

@ExperimentalComposeUiApi
@Composable
internal fun ItemDetailTopBar(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
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
                modifier = Modifier.padding(12.dp, 4.dp),
                backgroundColor = color,
                onClick = { onUpClick() }
            ) {
                Icon(
                    painter = painterResource(me.proton.core.presentation.R.drawable.ic_arrow_back),
                    contentDescription = stringResource(R.string.navigate_back_icon_content_description),
                    tint = color
                )
            }
        },
        actions = {
            Row(
                modifier = Modifier
                    .height(48.dp)
                    .padding(12.dp, 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LoadingCircleButton(
                    color = color,
                    isLoading = isLoading,
                    text = {
                        Text(
                            text = stringResource(R.string.top_bar_edit_button_text),
                            style = ProtonTheme.typography.defaultSmallInverted
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_pencil),
                            contentDescription = stringResource(R.string.top_bar_edit_icon_content_description),
                            tint = ProtonTheme.colors.iconInverted
                        )
                    },
                    onClick = { onEditClick() }
                )
                AnimatedVisibility(visible = !isLoading) {
                    Circle(
                        backgroundColor = color,
                        onClick = { onOptionsClick() }
                    ) {
                        Icon(
                            painter = painterResource(
                                me.proton.core.presentation.R.drawable.ic_proton_three_dots_vertical
                            ),
                            contentDescription = stringResource(R.string.open_menu_icon_content_description),
                            tint = color
                        )
                    }
                }
            }
        }
    )
}

class ThemeAndAccentColorProvider :
    ThemePairPreviewProvider<ItemDetailTopBarPreview>(ItemDetailTopBarPreviewProvider())

@OptIn(ExperimentalComposeUiApi::class)
@Preview
@Composable
fun ItemDetailTopBarPreview(
    @PreviewParameter(ThemeAndAccentColorProvider::class) input: Pair<Boolean, ItemDetailTopBarPreview>
) {
    ProtonTheme(isDark = input.first) {
        Surface {
            ItemDetailTopBar(
                isLoading = input.second.isLoading,
                color = input.second.color,
                onUpClick = {},
                onEditClick = {},
                onOptionsClick = {}
            )
        }
    }
}

