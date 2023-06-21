package proton.android.pass.featureitemcreate.impl.creditcard

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.dialogs.ConfirmCloseDialog
import proton.android.pass.composecomponents.impl.form.TitleSection
import proton.android.pass.featureitemcreate.impl.R
import proton.android.pass.featureitemcreate.impl.common.ItemSavedLaunchedEffect
import proton.android.pass.featureitemcreate.impl.creditcard.BaseCreditCardNavigation.Close
import proton.android.pass.featureitemcreate.impl.creditcard.BaseCreditCardNavigation.Upgrade
import proton.android.pass.featureitemcreate.impl.creditcard.CreditCardValidationErrors.BlankTitle

@Suppress("ComplexMethod")
@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun UpdateCreditCardScreen(
    modifier: Modifier = Modifier,
    viewModel: UpdateCreditCardViewModel = hiltViewModel(),
    onNavigate: (BaseCreditCardNavigation) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val uiState = state) {
        UpdateCreditCardUiState.Error -> LaunchedEffect(Unit) { onNavigate(Close) }

        UpdateCreditCardUiState.Loading,
        UpdateCreditCardUiState.NotInitialised -> {
        }

        is UpdateCreditCardUiState.Success -> {
            var showConfirmDialog by rememberSaveable { mutableStateOf(false) }
            val onExit = {
                if (uiState.baseState.hasUserEditedContent) {
                    showConfirmDialog = !showConfirmDialog
                } else {
                    onNavigate(Close)
                }
            }
            BackHandler(onBack = onExit)

            Box(modifier = modifier.fillMaxSize()) {
                CreditCardContent(
                    state = uiState.baseState,
                    selectedShareId = uiState.selectedShareId,
                    topBarActionName = stringResource(id = R.string.action_save),
                    titleSection = {
                        TitleSection(
                            modifier = Modifier
                                .roundedContainerNorm()
                                .padding(start = 16.dp, top = 16.dp, end = 4.dp, bottom = 16.dp),
                            value = uiState.baseState.contents.title,
                            requestFocus = true,
                            onTitleRequiredError = uiState.baseState.validationErrors
                                .contains(BlankTitle),
                            enabled = !uiState.baseState.isLoading,
                            isRounded = true,
                            onChange = viewModel::onTitleChange
                        )
                    },
                    onEvent = { event ->
                        when (event) {
                            is CreditCardContentEvent.OnCVVChange ->
                                viewModel.onCVVChanged(event.value)

                            is CreditCardContentEvent.OnExpirationDateChange ->
                                viewModel.onExpirationDateChanged(event.value)

                            is CreditCardContentEvent.OnNameChange ->
                                viewModel.onNameChanged(event.value)

                            is CreditCardContentEvent.OnNoteChange ->
                                viewModel.onNoteChanged(event.value)

                            is CreditCardContentEvent.OnNumberChange ->
                                viewModel.onNumberChanged(event.value)

                            is CreditCardContentEvent.OnPinChange ->
                                viewModel.onPinChanged(event.value)

                            is CreditCardContentEvent.Submit -> viewModel.update()
                            CreditCardContentEvent.Up -> onExit()
                            CreditCardContentEvent.Upgrade -> onNavigate(Upgrade)
                            is CreditCardContentEvent.OnCVVFocusChange ->
                                viewModel.onCVVFocusChanged(event.isFocused)
                            is CreditCardContentEvent.OnPinFocusChange ->
                                viewModel.onPinFocusChanged(event.isFocused)
                        }
                    }
                )
                ConfirmCloseDialog(
                    show = showConfirmDialog,
                    onCancel = {
                        showConfirmDialog = false
                    },
                    onConfirm = {
                        showConfirmDialog = false
                        onNavigate(Close)
                    }
                )
            }
            ItemSavedLaunchedEffect(
                isItemSaved = uiState.baseState.isItemSaved,
                selectedShareId = uiState.selectedShareId,
                onSuccess = { shareId, itemId, _ ->
                    onNavigate(UpdateCreditCardNavigation.ItemUpdated(shareId, itemId))
                }
            )
        }
    }
}
