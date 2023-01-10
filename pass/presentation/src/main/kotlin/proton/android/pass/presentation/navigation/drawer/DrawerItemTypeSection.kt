package proton.android.pass.presentation.navigation.drawer

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.data.api.ItemCountSummary
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import me.proton.pass.presentation.R

private data class DrawerItemSectionSpec(
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
    val section: HomeSection,
    val startContent: @Composable () -> Unit = {},
    val countFn: (ItemCountSummary) -> Long,
    val isSelectedFn: (NavigationDrawerSection?) -> Boolean
)

private val horizontalSpacerWidth = 24.dp

private val Sections = listOf(
    DrawerItemSectionSpec(
        title = R.string.navigation_item_items,
        icon = me.proton.core.presentation.R.drawable.ic_proton_vault,
        section = HomeSection.Items,
        startContent = {},
        countFn = { it.total },
        isSelectedFn = { it == NavigationDrawerSection.Items }
    ),
    DrawerItemSectionSpec(
        title = R.string.navigation_item_logins,
        icon = me.proton.core.presentation.R.drawable.ic_proton_key,
        section = HomeSection.Logins,
        startContent = { Spacer(modifier = Modifier.width(horizontalSpacerWidth)) },
        countFn = { it.login },
        isSelectedFn = { it == NavigationDrawerSection.Logins }
    ),
    DrawerItemSectionSpec(
        title = R.string.navigation_item_aliases,
        icon = me.proton.core.presentation.R.drawable.ic_proton_alias,
        section = HomeSection.Aliases,
        startContent = { Spacer(modifier = Modifier.width(horizontalSpacerWidth)) },
        countFn = { it.alias },
        isSelectedFn = { it == NavigationDrawerSection.Aliases }
    ),
    DrawerItemSectionSpec(
        title = R.string.navigation_item_notes,
        icon = me.proton.core.presentation.R.drawable.ic_proton_note,
        section = HomeSection.Notes,
        startContent = { Spacer(modifier = Modifier.width(horizontalSpacerWidth)) },
        countFn = { it.note },
        isSelectedFn = { it == NavigationDrawerSection.Notes }
    )
)

@Composable
fun DrawerItemTypeSection(
    modifier: Modifier = Modifier,
    selectedSection: NavigationDrawerSection?,
    itemCount: ItemCountSummary,
    closeDrawerAction: () -> Unit,
    onSectionClick: (HomeSection) -> Unit
) {
    Column(modifier = modifier) {
        Sections.forEach {
            NavigationDrawerListItem(
                title = stringResource(it.title),
                icon = it.icon,
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
                selectedSection = NavigationDrawerSection.Items,
                itemCount = ItemCountSummary.Initial,
                closeDrawerAction = {},
                onSectionClick = {}
            )
        }
    }
}
