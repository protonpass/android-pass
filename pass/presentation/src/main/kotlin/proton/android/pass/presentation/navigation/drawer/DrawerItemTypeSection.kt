package proton.android.pass.presentation.navigation.drawer

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.presentation.R
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.data.api.ItemCountSummary

private data class DrawerItemSectionSpec(
    @StringRes val title: Int,
    val section: SelectedItemTypes,
    val startContent: @Composable () -> Unit = {},
    val countFn: (ItemCountSummary) -> Long,
    val isSelectedFn: (NavigationDrawerSection?) -> Boolean
)

internal val horizontalSpacerWidth = 24.dp

private val Sections = listOf(
    DrawerItemSectionSpec(
        title = R.string.navigation_item_items,
        section = SelectedItemTypes.AllItems,
        startContent = {
            Icon(
                painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_vault),
                contentDescription = null,
                tint = ProtonTheme.colors.iconWeak
            )
        },
        countFn = { it.total },
        isSelectedFn = { it is NavigationDrawerSection.AllItems }
    ),
    DrawerItemSectionSpec(
        title = R.string.navigation_item_logins,
        section = SelectedItemTypes.Logins,
        startContent = {
            Spacer(modifier = Modifier.width(horizontalSpacerWidth))
            Icon(
                painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_key),
                contentDescription = null,
                tint = ProtonTheme.colors.iconWeak
            )
        },
        countFn = { it.login },
        isSelectedFn = { it is NavigationDrawerSection.Logins }
    ),
    DrawerItemSectionSpec(
        title = R.string.navigation_item_aliases,
        section = SelectedItemTypes.Aliases,
        startContent = {
            Spacer(modifier = Modifier.width(horizontalSpacerWidth))
            Icon(
                painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_alias),
                contentDescription = null,
                tint = ProtonTheme.colors.iconWeak
            )
        },
        countFn = { it.alias },
        isSelectedFn = { it is NavigationDrawerSection.Aliases }
    ),
    DrawerItemSectionSpec(
        title = R.string.navigation_item_notes,
        section = SelectedItemTypes.Notes,
        startContent = {
            Spacer(modifier = Modifier.width(horizontalSpacerWidth))
            Icon(
                painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_note),
                contentDescription = null,
                tint = ProtonTheme.colors.iconWeak
            )
        },
        countFn = { it.note },
        isSelectedFn = { it is NavigationDrawerSection.Notes }
    )
)

@Composable
fun DrawerItemTypeSection(
    modifier: Modifier = Modifier,
    selectedSection: NavigationDrawerSection?,
    itemCount: ItemCountSummary,
    closeDrawerAction: () -> Unit,
    onSectionClick: (SelectedItemTypes) -> Unit
) {
    Column(modifier = modifier) {
        Sections.forEach {
            NavigationDrawerListItem(
                title = stringResource(it.title),
                closeDrawerAction = closeDrawerAction,
                isSelected = it.isSelectedFn(selectedSection),
                onClick = { onSectionClick(it.section) },
                startContent = it.startContent,
                endContent = {
                    Text(text = "${it.countFn(itemCount)}")
                }
            )
        }
    }
}

@Preview
@Composable
fun ItemsListSectionPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    ProtonTheme(isDark = isDark) {
        Surface {
            DrawerItemTypeSection(
                selectedSection = NavigationDrawerSection.AllItems(),
                itemCount = ItemCountSummary.Initial,
                closeDrawerAction = {},
                onSectionClick = {}
            )
        }
    }
}
