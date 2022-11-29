package me.proton.pass.presentation.detail.note

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.pass.domain.Item

@Composable
fun NoteDetail(
    modifier: Modifier = Modifier,
    item: Item,
    viewModel: NoteDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(item) {
        viewModel.setItem(item)
    }

    val model by viewModel.viewState.collectAsStateWithLifecycle()
    NoteContent(
        modifier = modifier,
        model = model,
        onCopyToClipboard = { viewModel.onCopyToClipboard() }
    )
}
