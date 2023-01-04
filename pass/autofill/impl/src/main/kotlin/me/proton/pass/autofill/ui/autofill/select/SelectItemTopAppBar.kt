package me.proton.pass.autofill.ui.autofill.select

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.res.stringResource
import me.proton.android.pass.composecomponents.impl.topbar.SearchTopBar
import me.proton.pass.autofill.service.R

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
            placeholder = stringResource(id = R.string.topbar_search_query),
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
