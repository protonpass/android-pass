package me.proton.core.pass.autofill.ui.autofill.select

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import me.proton.android.pass.ui.shared.TopBarTitleView
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.pass.presentation.R

@Composable
fun IdleSelectItemTopBar(
    startSearchMode: () -> Unit
) {
    ProtonTopAppBar(
        title = {
            TopBarTitleView(title = stringResource(id = R.string.title_items))
        },
        actions = {
            IconButton(onClick = {
                startSearchMode()
            }) {
                Icon(
                    painterResource(me.proton.core.presentation.R.drawable.ic_proton_magnifier),
                    contentDescription = stringResource(R.string.action_search),
                    tint = ProtonTheme.colors.iconNorm
                )
            }
        }
    )
}

@Preview
@Composable
fun IdleSelectItemTopBarPreview() {
    IdleSelectItemTopBar(
        startSearchMode = {}
    )
}
