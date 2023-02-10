package proton.android.pass.featureitemdetail.impl

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.topbar.TopBarTitleView
import proton.android.pass.composecomponents.impl.topbar.icon.ArrowBackIcon

@ExperimentalComposeUiApi
@Composable
internal fun ItemDetailTopBar(
    modifier: Modifier = Modifier,
    title: String,
    onUpClick: () -> Unit,
    onEditClick: () -> Unit
) {
    ProtonTopAppBar(
        modifier = modifier,
        title = { TopBarTitleView(title = title) },
        navigationIcon = { ArrowBackIcon(onUpClick = onUpClick) },
        actions = {
            IconButton(
                onClick = onEditClick
            ) {
                Icon(
                    painterResource(me.proton.core.presentation.R.drawable.ic_proton_pencil),
                    contentDescription = null,
                    tint = ProtonTheme.colors.iconNorm
                )
            }
        }
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Preview
@Composable
fun ItemDetailTopBarPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    ProtonTheme(isDark = isDark) {
        Surface {
            ItemDetailTopBar(
                title = "some item",
                onUpClick = {},
                onEditClick = {}
            )
        }
    }
}

