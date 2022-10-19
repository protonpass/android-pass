package me.proton.android.pass.ui.trash

import androidx.compose.foundation.clickable
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import me.proton.android.pass.ui.shared.TopBarTitleView
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.pass.presentation.R


@ExperimentalMaterialApi
@Composable
internal fun TrashTopBar(
    onDrawerIconClick: () -> Unit,
    onClearTrashClick: () -> Unit
) {
    ProtonTopAppBar(
        title = { TopBarTitleView(title = stringResource(id = R.string.title_trash)) },
        navigationIcon = {
            Icon(
                Icons.Default.Menu,
                modifier = Modifier.clickable { onDrawerIconClick() },
                contentDescription = null
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
