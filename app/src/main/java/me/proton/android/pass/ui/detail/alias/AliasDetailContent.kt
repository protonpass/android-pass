package me.proton.android.pass.ui.detail.alias

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.proton.android.pass.ui.detail.login.Section
import me.proton.android.pass.ui.detail.login.SectionTitle
import me.proton.android.pass.ui.shared.LoadingDialog
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.presentation.R
import me.proton.pass.presentation.uievents.IsLoadingState

@Composable
fun AliasDetailContent(
    modifier: Modifier = Modifier,
    state: AliasDetailUiState
) {
    Column(modifier = modifier) {
        if (state.isLoadingState == IsLoadingState.Loading) {
            LoadingDialog()
        }

        if (state.model != null) {
            val model = state.model
            val title = stringResource(R.string.field_alias_title)
            val content = stringResource(R.string.field_copied_to_clipboard)
            val copiedToClipboardMessage = "$title $content"
            val clipboardManager = LocalClipboardManager.current
            val localContext = LocalContext.current
            Section(
                title = R.string.field_alias_title,
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
                    SectionTitle(R.string.field_mailboxes_title)
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
                    title = R.string.field_note_title,
                    content = model.note
                )
            }
        }
    }
}

