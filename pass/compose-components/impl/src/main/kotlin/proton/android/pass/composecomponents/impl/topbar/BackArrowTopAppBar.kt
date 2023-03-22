package proton.android.pass.composecomponents.impl.topbar

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTypography
import proton.android.pass.composecomponents.impl.topbar.iconbutton.BackArrowCircleIconButton

@Composable
fun BackArrowTopAppBar(
    modifier: Modifier = Modifier,
    title: String,
    actions: (@Composable RowScope.() -> Unit) = { },
    onUpClick: () -> Unit
) {
    ProtonTopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = title,
                style = PassTypography.hero
            )
        },
        navigationIcon = {
            BackArrowCircleIconButton(
                modifier = Modifier.padding(12.dp, 4.dp),
                color = PassTheme.colors.accentBrandOpaque,
                onUpClick = onUpClick
            )
        },
        actions = actions
    )
}
