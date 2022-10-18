package me.proton.android.pass.ui.detail.alias

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.android.pass.ui.detail.login.Section
import me.proton.android.pass.ui.detail.login.SectionTitle
import me.proton.android.pass.ui.shared.LoadingDialog
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.pass.domain.Item

@Composable
fun AliasDetail(
    item: Item,
    modifier: Modifier,
    viewModel: AliasDetailViewModel = hiltViewModel()
) {
    viewModel.setItem(item)
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()

    when (val state = viewState) {
        is AliasDetailViewModel.ViewState.Loading -> LoadingDialog()
        is AliasDetailViewModel.ViewState.Data -> {
            Column {
                Column(modifier = modifier.padding(horizontal = 16.dp)) {
                    AliasContentView(model = state.model)
                }
            }
        }
        is AliasDetailViewModel.ViewState.Error -> Text("Something went boom")
    }
}

@Composable
fun AliasContentView(
    model: AliasDetailViewModel.AliasUiModel
) {
    val title = stringResource(me.proton.core.pass.presentation.R.string.field_alias_title)
    val content = stringResource(me.proton.core.pass.presentation.R.string.field_copied_to_clipboard)
    val copiedToClipboardMessage = "$title $content"
    val clipboardManager = LocalClipboardManager.current
    val localContext = LocalContext.current
    Section(
        title = me.proton.core.pass.presentation.R.string.field_alias_title,
        content = model.alias,
        icon = me.proton.core.presentation.R.drawable.ic_proton_squares,
        onIconClick = {
            clipboardManager.setText(AnnotatedString(model.alias))
            Toast
                .makeText(localContext, copiedToClipboardMessage, Toast.LENGTH_SHORT)
                .show()
        }
    )

    Row(modifier = Modifier.padding(vertical = 12.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            SectionTitle(me.proton.core.pass.presentation.R.string.field_mailboxes_title)
            model.mailboxes.forEach {
                Text(
                    text = it.email,
                    color = ProtonTheme.colors.textWeak,
                    fontSize = 14.sp
                )
            }
        }
    }

    if (model.note.isNotEmpty()) {
        Section(
            title = me.proton.core.pass.presentation.R.string.field_note_title,
            content = model.note
        )
    }
}
