package proton.android.pass.featurehome.impl

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassPalette
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.icon.VaultIcon
import proton.android.pass.composecomponents.impl.topbar.SearchTopBar
import proton.android.pass.composecomponents.impl.topbar.icon.NavigationIcon

@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
internal fun HomeTopBar(
    modifier: Modifier = Modifier,
    searchQuery: String,
    inSearchMode: Boolean,
    homeFilter: HomeItemTypeSelection,
    drawerIcon: @Composable () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onEnterSearch: () -> Unit,
    onStopSearching: () -> Unit
) {
    if (inSearchMode) {
        SearchTopBar(
            modifier = modifier,
            placeholder = stringResource(id = R.string.placeholder_item_search),
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            onStopSearch = {
                onStopSearching()
            }
        )
    } else {
        IdleHomeTopBar(
            modifier = modifier,
            homeFilter = homeFilter,
            drawerIcon = drawerIcon,
            startSearchMode = { onEnterSearch() }
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class)
@Preview
@Composable
fun HomeTopBarIdlePreview(
    @PreviewParameter(ThemePreviewProvider::class) isDarkMode: Boolean
) {
    PassTheme(isDark = isDarkMode) {
        Surface {
            HomeTopBar(
                searchQuery = "",
                homeFilter = HomeItemTypeSelection.AllItems,
                inSearchMode = false,
                onSearchQueryChange = {},
                onEnterSearch = {},
                onStopSearching = {},
                drawerIcon = {
                    NavigationIcon(onUpClick = {}) {
                        VaultIcon(
                            backgroundColor = PassPalette.Yellow16,
                            iconColor = PassPalette.Yellow100,
                            icon = me.proton.core.presentation.R.drawable.ic_proton_house
                        )
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class)
@Preview
@Composable
fun HomeTopBarSearchPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDarkMode: Boolean
) {
    PassTheme(isDark = isDarkMode) {
        Surface {
            HomeTopBar(
                searchQuery = "some search",
                homeFilter = HomeItemTypeSelection.AllItems,
                inSearchMode = true,
                onSearchQueryChange = {},
                onEnterSearch = {},
                onStopSearching = {},
                drawerIcon = {}
            )
        }
    }
}
