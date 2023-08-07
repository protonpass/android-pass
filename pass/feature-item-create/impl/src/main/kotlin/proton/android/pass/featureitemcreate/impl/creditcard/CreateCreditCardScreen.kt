package proton.android.pass.featureitemcreate.impl.creditcard

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.composecomponents.impl.dialogs.ConfirmCloseDialog
import proton.android.pass.composecomponents.impl.form.TitleVaultSelectionSection
import proton.android.pass.composecomponents.impl.keyboard.keyboardAsState
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.featureitemcreate.impl.R
import proton.android.pass.featureitemcreate.impl.common.ItemSavedLaunchedEffect
import proton.android.pass.featureitemcreate.impl.common.ShareError.EmptyShareList
import proton.android.pass.featureitemcreate.impl.common.ShareError.SharesNotAvailable
import proton.android.pass.featureitemcreate.impl.common.ShareUiState
import proton.android.pass.featureitemcreate.impl.creditcard.BaseCreditCardNavigation.Close
import proton.android.pass.featureitemcreate.impl.creditcard.BaseCreditCardNavigation.Upgrade
import proton.android.pass.featureitemcreate.impl.creditcard.CCCActionAfterHideKeyboard.SelectVault
import proton.android.pass.featureitemcreate.impl.creditcard.CreditCardValidationErrors.BlankTitle
import proton.android.pass.featureitemcreate.impl.launchedeffects.InAppReviewTriggerLaunchedEffect
import proton.pass.domain.ShareId

private enum class CCCActionAfterHideKeyboard {
    SelectVault
}

@Suppress("ComplexMethod")
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CreateCreditCardScreen(
    modifier: Modifier = Modifier,
    selectVault: ShareId?,
    viewModel: CreateCreditCardViewModel = hiltViewModel(),
    onNavigate: (BaseCreditCardNavigation) -> Unit,
) {
    LaunchedEffect(selectVault) {
        if (selectVault != null) {
            viewModel.changeVault(selectVault)
        }
    }
    val state by viewModel.state.collectAsStateWithLifecycle()

    val keyboardController = LocalSoftwareKeyboardController.current
    var actionWhenKeyboardDisappears by remember { mutableStateOf<CCCActionAfterHideKeyboard?>(null) }

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
                    creditCardFormItem = viewModel.creditCardFormItem,
                    selectedShareId = selectedVault?.vault?.shareId,
                    topBarActionName = stringResource(id = R.string.title_create_credit_card),
                    titleSection = {
                        TitleVaultSelectionSection(
                            titleValue = viewModel.creditCardFormItem.title,
                            showVaultSelector = showVaultSelector,
                            onTitleChanged = viewModel::onTitleChange,
                            onTitleRequiredError = uiState.baseState.validationErrors
                                .contains(BlankTitle),
                            enabled = !uiState.baseState.isLoading,
                            vaultName = selectedVault?.vault?.name,
                            vaultColor = selectedVault?.vault?.color,
                            vaultIcon = selectedVault?.vault?.icon,
                            onVaultClicked = {
                                actionWhenKeyboardDisappears = SelectVault
                                keyboardController?.hide()
                            }
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

                            is CreditCardContentEvent.Submit -> viewModel.createItem()
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
            AfterKeyboardDisappearsLaunchedEffect(
                actionWhenKeyboardDisappears = actionWhenKeyboardDisappears,
                onDisappear = {
                    when (it) {
                        SelectVault -> {
                            selectedVault ?: return@AfterKeyboardDisappearsLaunchedEffect
                            onNavigate(CreateCreditCardNavigation.SelectVault(selectedVault.vault.shareId))
                            actionWhenKeyboardDisappears = null // Clear flag
                        }
                    }
                }
            )
            ItemSavedLaunchedEffect(
                isItemSaved = uiState.baseState.isItemSaved,
                selectedShareId = selectedVault?.vault?.shareId,
                onSuccess = { _, _, model ->
                    onNavigate(CreateCreditCardNavigation.ItemCreated(model))
                }
            )
            InAppReviewTriggerLaunchedEffect(
                triggerCondition = uiState.baseState.isItemSaved is ItemSavedState.Success,
            )
        }
    }
}

@Composable
private fun AfterKeyboardDisappearsLaunchedEffect(
    actionWhenKeyboardDisappears: CCCActionAfterHideKeyboard?,
    onDisappear: (CCCActionAfterHideKeyboard) -> Unit
) {
    val keyboardState by keyboardAsState()
    LaunchedEffect(keyboardState, actionWhenKeyboardDisappears) {
        if (!keyboardState) {
            when (actionWhenKeyboardDisappears) {
                SelectVault -> onDisappear(SelectVault)
                null -> {}
            }
        }
    }
}
