package proton.android.pass.featureitemcreate.impl.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.composecomponents.impl.dialogs.NoPaddingDialog

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun EditCustomFieldNameDialog(
    modifier: Modifier = Modifier,
    onNavigate: (CustomFieldNameNavigation) -> Unit,
    viewModel: EditCustomFieldNameViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.event) {
        if (state.event == CustomFieldEvent.Close) {
            onNavigate(CustomFieldNameNavigation.Close)
        }
    }

    NoPaddingDialog(
        modifier = modifier,
        onDismissRequest = { onNavigate(CustomFieldNameNavigation.Close) }
    ) {
        CustomFieldNameDialogContent(
            value = state.value,
            canConfirm = state.canConfirm,
            onChange = viewModel::onNameChanged,
            onConfirm = viewModel::onSave,
            onCancel = { onNavigate(CustomFieldNameNavigation.Close) }
        )
    }
}
