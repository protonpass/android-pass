package me.proton.android.pass.ui.detail.alias

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.pass.domain.Item

@Composable
fun AliasDetail(
    item: Item,
    modifier: Modifier
) {
    val viewModel: AliasDetailViewModel = hiltViewModel()
    LaunchedEffect(item) {
        viewModel.setItem(item)
    }

    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
    AliasDetailContent(
        modifier = Modifier.padding(horizontal = 16.dp),
        state = viewState
    )
}
