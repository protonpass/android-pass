package proton.android.pass.autofill.ui.autofill.select

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.autofill.service.R
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.topbar.TopBarTitleView
import proton.android.pass.composecomponents.impl.topbar.iconbutton.ArrowBackIconButton

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun IdleSelectItemTopBar(
    modifier: Modifier = Modifier,
    startSearchMode: () -> Unit,
    onClose: () -> Unit
) {
    ProtonTopAppBar(
        modifier = modifier,
        navigationIcon = { ArrowBackIconButton(onUpClick = onClose) },
        title = {
            TopBarTitleView(title = stringResource(id = R.string.select_item_title_items))
        },
        actions = {
            IconButton(onClick = {
                startSearchMode()
            }) {
                Icon(
                    painterResource(me.proton.core.presentation.R.drawable.ic_proton_magnifier),
                    contentDescription = stringResource(R.string.select_item_content_description_action_search),
                    tint = ProtonTheme.colors.iconNorm
                )
            }
        }
    )
}

@Preview
@Composable
fun IdleSelectItemTopBarPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            IdleSelectItemTopBar(
                startSearchMode = {},
                onClose = {}
            )
        }
    }
}
