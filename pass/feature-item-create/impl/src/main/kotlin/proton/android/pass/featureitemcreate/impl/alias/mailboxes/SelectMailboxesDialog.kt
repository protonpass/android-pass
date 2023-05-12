package proton.android.pass.featureitemcreate.impl.alias.mailboxes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.composecomponents.impl.dialogs.NoPaddingDialog
import proton.android.pass.featureitemcreate.impl.alias.SelectedAliasMailboxUiModel

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun SelectMailboxesDialog(
    modifier: Modifier = Modifier,
    mailboxes: List<SelectedAliasMailboxUiModel>,
    canUpgrade: Boolean,
    color: Color,
    onMailboxesChanged: (List<SelectedAliasMailboxUiModel>) -> Unit,
    onDismiss: () -> Unit,
    onUpgrade: () -> Unit,
    viewModel: SelectMailboxesDialogViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.setMailboxes(mailboxes)
    }

    LaunchedEffect(canUpgrade) {
        viewModel.setCanUpgrade(canUpgrade)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    NoPaddingDialog(modifier = modifier, onDismissRequest = onDismiss) {
        SelectMailboxesDialogContent(
            state = uiState,
            color = color,
            onConfirm = { onMailboxesChanged(uiState.mailboxes) },
            onDismiss = onDismiss,
            onUpgrade = onUpgrade,
            onMailboxToggled = { viewModel.onMailboxChanged(it) }
        )
    }
}
