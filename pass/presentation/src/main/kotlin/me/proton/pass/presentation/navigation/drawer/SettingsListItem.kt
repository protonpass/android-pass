package me.proton.pass.presentation.navigation.drawer

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.presentation.R
import me.proton.pass.commonui.api.ThemedBooleanPreviewProvider

@Composable
fun SettingsListItem(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    closeDrawerAction: () -> Unit,
    onClick: () -> Unit
) {
    NavigationDrawerListItem(
        modifier = modifier,
        title = stringResource(R.string.navigation_item_settings),
        icon = R.drawable.ic_settings,
        isSelected = isSelected,
        closeDrawerAction = closeDrawerAction,
        onClick = onClick
    )
}

@Preview
@Composable
fun SettingsListItemPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    ProtonTheme(isDark = input.first) {
        Surface {
            SettingsListItem(
                isSelected = input.second,
                onClick = {},
                closeDrawerAction = {}
            )
        }
    }
}
