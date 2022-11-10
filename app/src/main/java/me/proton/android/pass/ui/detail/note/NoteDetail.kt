package me.proton.android.pass.ui.detail.note

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.android.pass.ui.detail.login.Section
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.domain.Item

@Composable
fun NoteDetail(
    modifier: Modifier = Modifier,
    item: Item,
    viewModel: NoteDetailViewModel = hiltViewModel()
) {
    viewModel.setItem(item)

    val model by viewModel.viewState.collectAsStateWithLifecycle()
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
            title = me.proton.pass.presentation.R.string.field_note_title,
            content = model.note
        )
    }
}
