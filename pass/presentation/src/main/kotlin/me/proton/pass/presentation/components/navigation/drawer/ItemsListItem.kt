package me.proton.pass.presentation.components.navigation.drawer

import androidx.annotation.DrawableRes
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.presentation.settings.ThemedBooleanPreviewProvider

@Composable
fun ItemsListItem(
    modifier: Modifier = Modifier,
    title: String,
    @DrawableRes icon: Int,
    itemCount: Long,
    isSelected: Boolean,
    closeDrawerAction: () -> Unit,
    onClick: () -> Unit,
    startContent: @Composable () -> Unit = {}
) {
    NavigationDrawerListItem(
        title = title,
        icon = icon,
        isSelected = isSelected,
        closeDrawerAction = closeDrawerAction,
        modifier = modifier,
        onClick = onClick,
        startContent = startContent,
        endContent = {
            Text(text = "$itemCount")
        }
    )
}

@Preview
@Composable
fun ItemsListItemPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    ProtonTheme(isDark = input.first) {
        Surface {
            ItemsListItem(
                title = "Some section",
                icon = me.proton.core.presentation.R.drawable.ic_proton_key,
                itemCount = 2,
                isSelected = input.second,
                closeDrawerAction = {},
                onClick = {}
            )
        }
    }
}
