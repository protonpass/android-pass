package me.proton.pass.presentation.navigation.drawer

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
fun SignOutListItem(
    modifier: Modifier = Modifier,
    closeDrawerAction: () -> Unit,
    onClick: () -> Unit
) {
    NavigationDrawerListItem(
        modifier = modifier,
        icon = R.drawable.ic_sign_out,
        title = stringResource(R.string.navigation_item_sign_out),
        closeDrawerAction = closeDrawerAction,
        isSelected = false,
        onClick = onClick
    )
}

@Preview
@Composable
fun SignOutListItemPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    ProtonTheme(isDark = isDark) {
        Surface {
            SignOutListItem(
                closeDrawerAction = {},
                onClick = {}
            )
        }
    }
}
