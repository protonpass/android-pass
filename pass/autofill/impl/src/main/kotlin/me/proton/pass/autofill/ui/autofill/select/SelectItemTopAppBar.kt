package me.proton.pass.autofill.ui.autofill.select

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import me.proton.pass.autofill.service.R
import me.proton.pass.presentation.components.SearchTopBar

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SelectItemTopAppBar(
    searchQuery: String,
    inSearchMode: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onEnterSearch: () -> Unit,
    onStopSearching: () -> Unit,
    onClose: () -> Unit
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
            startSearchMode = onEnterSearch,
            onClose = onClose
        )
    }
}
