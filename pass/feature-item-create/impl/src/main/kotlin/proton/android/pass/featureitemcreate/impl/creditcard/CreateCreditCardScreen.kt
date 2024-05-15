package proton.android.pass.featureitemcreate.impl.creditcard

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.composecomponents.impl.dialogs.ConfirmCloseDialog
import proton.android.pass.domain.ShareId
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.featureitemcreate.impl.R
import proton.android.pass.featureitemcreate.impl.common.ItemSavedLaunchedEffect
import proton.android.pass.featureitemcreate.impl.common.ShareError.EmptyShareList
import proton.android.pass.featureitemcreate.impl.common.ShareError.SharesNotAvailable
import proton.android.pass.featureitemcreate.impl.common.ShareUiState
import proton.android.pass.featureitemcreate.impl.creditcard.BaseCreditCardNavigation.Close
import proton.android.pass.featureitemcreate.impl.creditcard.BaseCreditCardNavigation.Upgrade
import proton.android.pass.featureitemcreate.impl.launchedeffects.InAppReviewTriggerLaunchedEffect

@Composable
fun CreateCreditCardScreen(
    modifier: Modifier = Modifier,
    selectVault: ShareId?,
    viewModel: CreateCreditCardViewModel = hiltViewModel(),
    onNavigate: (BaseCreditCardNavigation) -> Unit
) {
    LaunchedEffect(selectVault) {
        if (selectVault != null) {
            viewModel.changeVault(selectVault)
        }
    }
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val uiState = state) {
        CreateCreditCardUiState.Error -> LaunchedEffect(Unit) { onNavigate(Close) }

        CreateCreditCardUiState.Loading,
        CreateCreditCardUiState.NotInitialised -> {
        }

        is CreateCreditCardUiState.Success -> {
            var showConfirmDialog by rememberSaveable { mutableStateOf(false) }
            val onExit = {
                if (uiState.baseState.hasUserEditedContent) {
                    showConfirmDialog = !showConfirmDialog
                } else {
                    onNavigate(Close)
                }
            }
            BackHandler(onBack = onExit)
            val (showVaultSelector, selectedVault) = when (val shares = uiState.shareUiState) {
                ShareUiState.Loading,
                ShareUiState.NotInitialised -> false to null

                is ShareUiState.Error -> {
                    if (shares.shareError == EmptyShareList || shares.shareError == SharesNotAvailable) {
                        LaunchedEffect(Unit) {
                            onNavigate(Close)
                        }
                    }
                    false to null
                }

                is ShareUiState.Success -> (shares.vaultList.size > 1) to shares.currentVault
            }
            Box(modifier = modifier.fillMaxSize()) {
                CreditCardContent(
                    state = uiState.baseState,
                    creditCardItemFormState = viewModel.creditCardItemFormState,
                    selectedVault = selectedVault?.vault,
                    showVaultSelector = showVaultSelector,
                    selectedShareId = selectedVault?.vault?.shareId,
                    topBarActionName = stringResource(id = R.string.title_create),
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

                            is CreditCardContentEvent.Submit -> viewModel.createItem()
                            CreditCardContentEvent.Up -> onExit()
                            CreditCardContentEvent.Upgrade -> onNavigate(Upgrade)
                            is CreditCardContentEvent.OnCVVFocusChange ->
                                viewModel.onCVVFocusChanged(event.isFocused)

                            is CreditCardContentEvent.OnPinFocusChange ->
                                viewModel.onPinFocusChanged(event.isFocused)

                            is CreditCardContentEvent.OnTitleChange ->
                                viewModel.onTitleChange(event.value)

                            is CreditCardContentEvent.OnVaultSelect ->
                                onNavigate(CreateCreditCardNavigation.SelectVault(event.shareId))

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
                selectedShareId = selectedVault?.vault?.shareId,
                onSuccess = { _, _, model ->
                    onNavigate(CreateCreditCardNavigation.ItemCreated(model))
                }
            )
            InAppReviewTriggerLaunchedEffect(
                triggerCondition = uiState.baseState.isItemSaved is ItemSavedState.Success
            )
        }
    }
}
