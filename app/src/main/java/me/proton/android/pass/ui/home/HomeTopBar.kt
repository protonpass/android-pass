package me.proton.android.pass.ui.home

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import me.proton.android.pass.R
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.presentation.components.SearchTopBar

@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
internal fun HomeTopBar(
    modifier: Modifier = Modifier,
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
            modifier = modifier,
            placeholder = R.string.placeholder_item_search,
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            onStopSearch = {
                onStopSearching()
            }
        )
    } else {
        IdleHomeTopBar(
            modifier = modifier,
            startSearchMode = { onEnterSearch() },
            onDrawerIconClick = onDrawerIconClick,
            onAddItemClick = onAddItemClick
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class)
@Preview(showBackground = true)
@Composable
fun HomeTopBarIdlePreview() {
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
fun HomeTopBarSearchPreview() {
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
