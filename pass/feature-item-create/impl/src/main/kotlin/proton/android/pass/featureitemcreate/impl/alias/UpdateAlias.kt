package proton.android.pass.featureitemcreate.impl.alias

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.dialogs.ConfirmCloseDialog
import proton.android.pass.composecomponents.impl.form.TitleSection
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.featureitemcreate.impl.R

@OptIn(ExperimentalLifecycleComposeApi::class)
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun UpdateAlias(
    modifier: Modifier = Modifier,
    onNavigate: (UpdateAliasNavigation) -> Unit,
    viewModel: UpdateAliasViewModel = hiltViewModel()
) {
    val uiState by viewModel.updateAliasUiState.collectAsStateWithLifecycle()
    var showConfirmDialog by rememberSaveable { mutableStateOf(false) }
    val onExit = {
        if (uiState.baseAliasUiState.hasUserEditedContent) {
            showConfirmDialog = !showConfirmDialog
        } else {
            onNavigate(UpdateAliasNavigation.Close)
        }
    }
    BackHandler {
        onExit()
    }

    LaunchedEffect(uiState.baseAliasUiState.closeScreenEvent) {
        if (uiState.baseAliasUiState.closeScreenEvent is CloseScreenEvent.Close) {
            onNavigate(UpdateAliasNavigation.Close)
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        AliasContent(
            uiState = uiState.baseAliasUiState,
            selectedShareId = uiState.selectedShareId,
            topBarActionName = stringResource(id = R.string.action_save),
            isCreateMode = false,
            isEditAllowed = uiState.baseAliasUiState.isLoadingState == IsLoadingState.NotLoading,
            onUpClick = onExit,
            onAliasCreated = { shareId, itemId, _ ->
                onNavigate(UpdateAliasNavigation.Updated(shareId, itemId))
            },
            onSubmit = { viewModel.updateAlias() },
            onSuffixChange = {},
            onMailboxesChanged = { viewModel.onMailboxesChanged(it) },
            onNoteChange = { viewModel.onNoteChange(it) },
            onPrefixChange = {},
            onUpgrade = { onNavigate(UpdateAliasNavigation.Upgrade) },
            titleSection = {
                TitleSection(
                    modifier = Modifier
                        .roundedContainerNorm()
                        .padding(start = 16.dp, top = 16.dp, end = 4.dp, bottom = 16.dp),
                    value = uiState.baseAliasUiState.aliasItem.title,
                    requestFocus = true,
                    onTitleRequiredError = uiState.baseAliasUiState.errorList.contains(
                        AliasItemValidationErrors.BlankTitle
                    ),
                    enabled = uiState.baseAliasUiState.isLoadingState == IsLoadingState.NotLoading,
                    isRounded = true,
                    onChange = viewModel::onTitleChange
                )
            },
        )

        ConfirmCloseDialog(
            show = showConfirmDialog,
            onCancel = {
                showConfirmDialog = false
            },
            onConfirm = {
                showConfirmDialog = false
                onNavigate(UpdateAliasNavigation.Close)
            }
        )
    }
}
