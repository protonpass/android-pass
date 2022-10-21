package me.proton.core.pass.autofill.ui.autofill.select

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import me.proton.core.pass.autofill.service.R
import me.proton.core.pass.presentation.components.SearchTopBar

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SelectItemTopAppBar(
    searchQuery: String,
    inSearchMode: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onEnterSearch: () -> Unit,
    onStopSearching: () -> Unit
) {
    if (inSearchMode) {
        SearchTopBar(
            placeholder = R.string.topbar_search_query,
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            onStopSearch = onStopSearching
        )
    } else {
        IdleSelectItemTopBar(
            startSearchMode = onEnterSearch
        )
    }
}
