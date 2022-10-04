package me.proton.android.pass.ui.detail.note

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import me.proton.android.pass.R
import me.proton.android.pass.ui.detail.login.Section
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.pass.domain.Item
import me.proton.core.pass.presentation.components.common.rememberFlowWithLifecycle

@Composable
fun NoteDetail(
    item: Item,
    modifier: Modifier = Modifier,
    viewModel: NoteDetailViewModel = hiltViewModel()
) {
    viewModel.setItem(item)

    val model by rememberFlowWithLifecycle(viewModel.viewState).collectAsState(initial = viewModel.initialViewState)
    NoteContentView(
        model = model,
        modifier = modifier
    )
}

@Composable
internal fun NoteContentView(
    model: NoteDetailViewModel.NoteUiModel,
    modifier: Modifier = Modifier
) {
    Column {
        Column(modifier = modifier.padding(horizontal = 16.dp)) {
            NoteRow(model)
        }
        Divider(
            modifier = Modifier.fillMaxWidth(),
            color = ProtonTheme.colors.separatorNorm
        )
    }
}

@Composable
internal fun NoteRow(
    model: NoteDetailViewModel.NoteUiModel
) {
    if (model.note.isNotEmpty()) {
        Section(
            title = me.proton.core.pass.presentation.R.string.field_note_title,
            content = model.note
        )
    }
}
