package me.proton.pass.presentation.components.navigation.drawer

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.commonui.api.ThemePreviewProvider
import me.proton.pass.presentation.R

@Composable
fun InternalDrawerItem(
    modifier: Modifier = Modifier,
    closeDrawerAction: () -> Unit,
    onClick: () -> Unit
) {
    NavigationDrawerListItem(
        title = stringResource(R.string.navigation_item_internal_drawer),
        icon = me.proton.core.presentation.R.drawable.ic_proton_cog_wheel,
        isSelected = false,
        closeDrawerAction = closeDrawerAction,
        modifier = modifier,
        onClick = onClick
    )
}

@Preview
@Composable
fun InternalDrawerItemPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    ProtonTheme(isDark = isDark) {
        Surface {
            InternalDrawerItem(
                closeDrawerAction = {},
                onClick = {}
            )
        }
    }
}
