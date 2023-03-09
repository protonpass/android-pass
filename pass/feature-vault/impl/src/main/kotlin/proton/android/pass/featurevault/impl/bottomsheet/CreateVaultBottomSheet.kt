package proton.android.pass.featurevault.impl.bottomsheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.commonui.api.bottomSheetPadding

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun CreateVaultBottomSheet(
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
    viewModel: CreateVaultViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isVaultCreatedEvent) {
        if (state.isVaultCreatedEvent == IsVaultCreatedEvent.Created) {
            onClose()
        }
    }

    CreateVaultBottomSheetContent(
        modifier = modifier.bottomSheetPadding(),
        state = state,
        onNameChange = { viewModel.onNameChange(it) },
        onIconChange = { viewModel.onIconChange(it) },
        onColorChange = { viewModel.onColorChange(it) },
        onClose = onClose,
        onCreateClick = {
            viewModel.onCreateClick()
        }
    )
}
