package proton.android.pass.featureitemcreate.impl.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallInverted
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.buttons.LoadingCircleButton
import proton.android.pass.composecomponents.impl.container.Circle
import proton.android.pass.featureitemcreate.impl.R

@ExperimentalComposeUiApi
@Composable
internal fun CreateUpdateTopBar(
    modifier: Modifier = Modifier,
    text: String,
    isLoading: Boolean,
    opaqueColor: Color,
    weakestColor: Color,
    onCloseClick: () -> Unit,
    onActionClick: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    ProtonTopAppBar(
        modifier = modifier,
        title = { },
        navigationIcon = {
            Circle(
                modifier = Modifier.padding(12.dp, 4.dp),
                backgroundColor = weakestColor,
                onClick = {
                    keyboardController?.hide()
                    onCloseClick()
                }
            ) {
                Icon(
                    painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_cross_small),
                    contentDescription = stringResource(R.string.close_scree_icon_content_description),
                    tint = opaqueColor
                )
            }
        },
        actions = {
            LoadingCircleButton(
                modifier = Modifier.padding(12.dp, 4.dp),
                color = opaqueColor,
                isLoading = isLoading,
                text = {
                    Text(
                        text = text,
                        style = ProtonTheme.typography.defaultSmallInverted
                    )
                },
                onClick = {
                    keyboardController?.hide()
                    onActionClick()
                }
            )
        }
    )
}

class ThemeAndCreateUpdateTopBarProvider :
    ThemePairPreviewProvider<CreateUpdateTopBarPreview>(CreateUpdateTopBarPreviewProvider())

@OptIn(ExperimentalComposeUiApi::class)
@Preview
@Composable
fun CreateUpdateTopBarPreview(
    @PreviewParameter(ThemeAndCreateUpdateTopBarProvider::class) input: Pair<Boolean, CreateUpdateTopBarPreview>
) {
    PassTheme(isDark = input.first) {
        Surface {
            CreateUpdateTopBar(
                text = "Save",
                isLoading = input.second.isLoading,
                opaqueColor = input.second.opaqueColor,
                weakestColor = input.second.weakestColor,
                onCloseClick = {},
                onActionClick = {}
            )
        }
    }
}
