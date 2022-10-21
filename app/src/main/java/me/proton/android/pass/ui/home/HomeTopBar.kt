package me.proton.android.pass.ui.home

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.tooling.preview.Preview
import me.proton.android.pass.R
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.pass.presentation.components.SearchTopBar

@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
internal fun HomeTopBar(
    searchQuery: String,
    inSearchMode: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onEnterSearch: () -> Unit,
    onStopSearching: () -> Unit,
    onDrawerIconClick: () -> Unit,
    onAddItemClick: () -> Unit
) {
    if (inSearchMode) {
        SearchTopBar(
            placeholder = R.string.placeholder_item_search,
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            onStopSearch = {
                onStopSearching()
            }
        )
    } else {
        IdleHomeTopBar(
            startSearchMode = { onEnterSearch() },
            onDrawerIconClick = onDrawerIconClick,
            onAddItemClick = onAddItemClick
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class)
@Preview(showBackground = true)
@Composable
fun Preview_HomeTopBar_idle() {
    ProtonTheme {
        HomeTopBar(
            searchQuery = "",
            inSearchMode = false,
            onSearchQueryChange = {},
            onEnterSearch = {},
            onStopSearching = {},
            onDrawerIconClick = {},
            onAddItemClick = {}
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class)
@Preview(showBackground = true)
@Composable
fun Preview_HomeTopBar_search() {
    ProtonTheme {
        HomeTopBar(
            searchQuery = "some search",
            inSearchMode = true,
            onSearchQueryChange = {},
            onEnterSearch = {},
            onStopSearching = {},
            onDrawerIconClick = {},
            onAddItemClick = {}
        )
    }
}
