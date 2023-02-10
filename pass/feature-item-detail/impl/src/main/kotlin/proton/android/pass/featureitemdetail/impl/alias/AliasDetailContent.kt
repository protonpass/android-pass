package proton.android.pass.featureitemdetail.impl.alias

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import proton.android.pass.composecomponents.impl.loading.LoadingDialog
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.featureitemdetail.impl.DetailNoteSection
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages

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

