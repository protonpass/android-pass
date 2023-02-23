package proton.android.pass.presentation.navigation.drawer

import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.presentation.R
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider

@Composable
fun SignOutListItem(
    modifier: Modifier = Modifier,
    closeDrawerAction: () -> Unit,
    onClick: () -> Unit
) {
    NavigationDrawerListItem(
        modifier = modifier,
        title = stringResource(R.string.navigation_item_sign_out),
        closeDrawerAction = closeDrawerAction,
        isSelected = false,
        onClick = onClick,
        startContent = {
            Icon(
                painter = painterResource(R.drawable.ic_sign_out),
                contentDescription = null,
                tint = ProtonTheme.colors.iconWeak
            )
        }
    )
}

@Preview
@Composable
fun SignOutListItemPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            SignOutListItem(
                closeDrawerAction = {},
                onClick = {}
            )
        }
    }
}
