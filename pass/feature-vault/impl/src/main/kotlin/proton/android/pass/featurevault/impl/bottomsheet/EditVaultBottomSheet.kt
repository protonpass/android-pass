package proton.android.pass.featurevault.impl.bottomsheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.commonui.api.bottomSheetPadding
import proton.android.pass.feature.vault.impl.R

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun EditVaultBottomSheet(
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
    viewModel: EditVaultViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onStart()
    }

    LaunchedEffect(state.isVaultCreatedEvent) {
        if (state.isVaultCreatedEvent == IsVaultCreatedEvent.Created) {
            onClose()
        }
    }

    VaultBottomSheetContent(
        modifier = modifier.bottomSheetPadding(),
        state = state,
        buttonText = stringResource(R.string.bottomsheet_edit_vault_button),
        onNameChange = { viewModel.onNameChange(it) },
        onIconChange = { viewModel.onIconChange(it) },
        onColorChange = { viewModel.onColorChange(it) },
        onClose = { onClose() },
        onButtonClick = { viewModel.onEditClick() }
    )
}

