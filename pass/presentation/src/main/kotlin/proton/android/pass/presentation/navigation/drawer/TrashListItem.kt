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
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider

@Composable
fun TrashListItem(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    closeDrawerAction: () -> Unit,
    onClick: () -> Unit
) {
    NavigationDrawerListItem(
        modifier = modifier,
        title = stringResource(R.string.navigation_item_trash),
        isSelected = isSelected,
        closeDrawerAction = closeDrawerAction,
        onClick = onClick,
        startContent = {
            Icon(
                painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_trash),
                contentDescription = null,
                tint = ProtonTheme.colors.iconWeak
            )
        }
    )
}

@Preview
@Composable
fun TrashListItemPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    ProtonTheme(isDark = input.first) {
        Surface {
            TrashListItem(
                isSelected = input.second,
                onClick = {},
                closeDrawerAction = {}
            )
        }
    }
}
