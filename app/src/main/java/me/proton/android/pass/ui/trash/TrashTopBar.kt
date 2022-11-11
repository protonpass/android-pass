package me.proton.android.pass.ui.trash

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import me.proton.android.pass.ui.shared.HamburgerIcon
import me.proton.android.pass.ui.shared.TopBarTitleView
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.presentation.R


@ExperimentalMaterialApi
@Composable
internal fun TrashTopBar(
    onDrawerIconClick: () -> Unit,
    onClearTrashClick: () -> Unit
) {
    ProtonTopAppBar(
        title = { TopBarTitleView(title = stringResource(id = R.string.title_trash)) },
        navigationIcon = {
            HamburgerIcon(
                onClick = onDrawerIconClick
            )
        },
        actions = {
            IconButton(onClick = { onClearTrashClick() }) {
                Icon(
                    painterResource(me.proton.core.presentation.R.drawable.ic_proton_trash),
                    contentDescription = stringResource(R.string.action_empty_trash),
                    tint = ProtonTheme.colors.iconNorm
                )
            }
        }
    )
}
