package me.proton.android.pass.ui.detail

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import me.proton.android.pass.ui.detail.login.LoginDetail
import me.proton.android.pass.ui.detail.note.NoteDetail
import me.proton.android.pass.ui.shared.CrossBackIcon
import me.proton.android.pass.ui.shared.TopBarTitleView
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.pass.domain.Item
import me.proton.core.pass.domain.ItemType
import me.proton.core.pass.presentation.components.common.rememberFlowWithLifecycle

@ExperimentalComposeUiApi
@Composable
fun ItemDetailScreen(
    onUpClick: () -> Unit,
    shareId: String,
    itemId: String,
    viewModel: ItemDetailViewModel = hiltViewModel()
) {

    viewModel.setContent(shareId, itemId)

    val viewState by rememberFlowWithLifecycle(flow = viewModel.state).collectAsState(initial = viewModel.initialState)
    val topBarTitle = when (val state = viewState) {
        is ItemDetailViewModel.State.Loading -> ""
        is ItemDetailViewModel.State.Content -> state.model.name
        is ItemDetailViewModel.State.Error -> ""
    }

    Scaffold(
        topBar = {
            ProtonTopAppBar(
                title = { TopBarTitleView(topBarTitle) },
                navigationIcon = { CrossBackIcon(onUpClick = onUpClick) },
            )
        }
    ) { padding ->
        ItemDetailContent(viewState, modifier = Modifier.padding(padding))
    }
}

@Composable
private fun ItemDetailContent(
    viewState: ItemDetailViewModel.State,
    modifier: Modifier = Modifier
) {
    when (val state = viewState) {
        is ItemDetailViewModel.State.Content -> ItemDetail(state.model.item, modifier)
        is ItemDetailViewModel.State.Loading -> {}
        is ItemDetailViewModel.State.Error -> {}
    }
}

@Composable
private fun ItemDetail(
    item: Item,
    modifier: Modifier = Modifier
) {
    when (item.itemType) {
        is ItemType.Login -> LoginDetail(item, modifier)
        is ItemType.Note -> NoteDetail(item, modifier)
    }
}
