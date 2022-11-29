package me.proton.pass.presentation.detail.note

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.pass.domain.Item

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun NoteDetail(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit,
    item: Item,
    viewModel: NoteDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(item) {
        viewModel.setItem(item)
    }

    val model by viewModel.viewState.collectAsStateWithLifecycle()
    Scaffold(
        topBar = topBar
    ) { padding ->
        NoteContent(
            modifier = modifier.padding(padding),
            model = model,
            onCopyToClipboard = { viewModel.onCopyToClipboard() }
        )
    }
}
