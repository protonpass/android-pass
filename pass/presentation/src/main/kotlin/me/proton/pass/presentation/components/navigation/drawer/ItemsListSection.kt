package me.proton.pass.presentation.components.navigation.drawer

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.android.pass.data.api.ItemCountSummary
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.commonui.api.ThemePreviewProvider
import me.proton.pass.presentation.R

private data class ItemSection(
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
    val section: HomeSection,
    val startContent: @Composable () -> Unit = {},
    val countFn: (ItemCountSummary) -> Long,
    val isSelectedFn: (NavigationDrawerSection?) -> Boolean
)

private val horizontalSpacerWidth = 24.dp

private val Sections = listOf(
    ItemSection(
        title = R.string.navigation_item_items,
        icon = me.proton.core.presentation.R.drawable.ic_proton_vault,
        section = HomeSection.Items,
        startContent = {},
        countFn = { it.total },
        isSelectedFn = { it == NavigationDrawerSection.Items }
    ),
    ItemSection(
        title = R.string.navigation_item_logins,
        icon = me.proton.core.presentation.R.drawable.ic_proton_key,
        section = HomeSection.Logins,
        startContent = { Spacer(modifier = Modifier.width(horizontalSpacerWidth)) },
        countFn = { it.login },
        isSelectedFn = { it == NavigationDrawerSection.Logins }
    ),
    ItemSection(
        title = R.string.navigation_item_aliases,
        icon = me.proton.core.presentation.R.drawable.ic_proton_alias,
        section = HomeSection.Logins,
        startContent = { Spacer(modifier = Modifier.width(horizontalSpacerWidth)) },
        countFn = { it.alias },
        isSelectedFn = { it == NavigationDrawerSection.Aliases }
    ),
    ItemSection(
        title = R.string.navigation_item_notes,
        icon = me.proton.core.presentation.R.drawable.ic_proton_note,
        section = HomeSection.Notes,
        startContent = { Spacer(modifier = Modifier.width(horizontalSpacerWidth)) },
        countFn = { it.note },
        isSelectedFn = { it == NavigationDrawerSection.Notes }
    )
)

@Composable
fun ItemsListSection(
    modifier: Modifier = Modifier,
    selectedSection: NavigationDrawerSection?,
    itemCount: ItemCountSummary,
    closeDrawerAction: () -> Unit,
    onSectionClick: (HomeSection) -> Unit

) {
    Column(modifier = modifier) {
        Sections.forEach {
            ItemsListItem(
                title = stringResource(it.title),
                icon = it.icon,
                itemCount = it.countFn(itemCount),
                isSelected = it.isSelectedFn(selectedSection),
                closeDrawerAction = closeDrawerAction,
                startContent = it.startContent,
                onClick = { onSectionClick(it.section) }
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
            ItemsListSection(
                selectedSection = NavigationDrawerSection.Items,
                itemCount = ItemCountSummary.Initial,
                closeDrawerAction = {},
                onSectionClick = {}
            )
        }
    }
}
