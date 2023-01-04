package me.proton.pass.presentation.detail.alias

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.proton.android.pass.composecomponents.impl.loading.LoadingDialog
import me.proton.pass.presentation.detail.DetailNoteSection
import me.proton.pass.presentation.detail.DetailSnackbarMessages
import me.proton.android.pass.composecomponents.impl.uievents.IsLoadingState

@Composable
fun AliasDetailContent(
    modifier: Modifier = Modifier,
    state: AliasDetailUiState,
    emitSnackbarMessage: (DetailSnackbarMessages) -> Unit
) {
    if (state.isLoadingState == IsLoadingState.Loading) {
        LoadingDialog()
    }

    val model = state.model ?: return
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        AliasAddressSection(
            alias = model.alias,
            onAliasCopied = {
                emitSnackbarMessage(DetailSnackbarMessages.AliasCopiedToClipboard)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        AliasMailboxesSection(mailboxes = model.mailboxes)

        Spacer(modifier = Modifier.height(16.dp))

        DetailNoteSection(
            modifier = Modifier.padding(start = 16.dp),
            text = model.note
        )
    }
}

