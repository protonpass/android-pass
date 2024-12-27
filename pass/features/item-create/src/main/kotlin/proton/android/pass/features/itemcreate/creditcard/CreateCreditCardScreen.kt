package proton.android.pass.features.itemcreate.creditcard

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.commonui.api.toClassHolder
import proton.android.pass.composecomponents.impl.attachments.AttachmentContentEvent
import proton.android.pass.composecomponents.impl.dialogs.ConfirmCloseDialog
import proton.android.pass.domain.ShareId
import proton.android.pass.features.itemcreate.ItemSavedState
import proton.android.pass.features.itemcreate.R
import proton.android.pass.features.itemcreate.common.ItemSavedLaunchedEffect
import proton.android.pass.features.itemcreate.common.ShareError.EmptyShareList
import proton.android.pass.features.itemcreate.common.ShareError.SharesNotAvailable
import proton.android.pass.features.itemcreate.common.ShareUiState
import proton.android.pass.features.itemcreate.creditcard.BaseCreditCardNavigation.AddAttachment
import proton.android.pass.features.itemcreate.creditcard.BaseCreditCardNavigation.Close
import proton.android.pass.features.itemcreate.creditcard.BaseCreditCardNavigation.DeleteAllAttachments
import proton.android.pass.features.itemcreate.creditcard.BaseCreditCardNavigation.OpenAttachmentOptions
import proton.android.pass.features.itemcreate.creditcard.BaseCreditCardNavigation.OpenDraftAttachmentOptions
import proton.android.pass.features.itemcreate.creditcard.BaseCreditCardNavigation.Upgrade
import proton.android.pass.features.itemcreate.launchedeffects.InAppReviewTriggerLaunchedEffect
import proton.android.pass.features.itemcreate.login.PerformActionAfterKeyboardHide

@Composable
fun CreateCreditCardScreen(
    modifier: Modifier = Modifier,
    selectVault: ShareId?,
    viewModel: CreateCreditCardViewModel = hiltViewModel(),
    onNavigate: (BaseCreditCardNavigation) -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(selectVault) {
        if (selectVault != null) {
            viewModel.changeVault(selectVault)
        }
    }

    var actionAfterKeyboardHide by remember { mutableStateOf<(() -> Unit)?>(null) }
    PerformActionAfterKeyboardHide(
        action = actionAfterKeyboardHide,
        clearAction = { actionAfterKeyboardHide = null }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val uiState = state) {
        CreateCreditCardUiState.Error -> LaunchedEffect(Unit) {
            actionAfterKeyboardHide = { onNavigate(Close) }
        }

        CreateCreditCardUiState.Loading,
        CreateCreditCardUiState.NotInitialised -> {
        }

        is CreateCreditCardUiState.Success -> {
            var showConfirmDialog by rememberSaveable { mutableStateOf(false) }
            val onExit = {
                if (uiState.baseState.hasUserEditedContent) {
                    showConfirmDialog = !showConfirmDialog
                } else {
                    actionAfterKeyboardHide = { onNavigate(Close) }
                }
            }
            BackHandler(onBack = onExit)
            val (showVaultSelector, selectedVault) = when (val shares = uiState.shareUiState) {
                ShareUiState.Loading,
                ShareUiState.NotInitialised -> false to null

                is ShareUiState.Error -> {
                    if (shares.shareError == EmptyShareList || shares.shareError == SharesNotAvailable) {
                        LaunchedEffect(Unit) {
                            actionAfterKeyboardHide = { onNavigate(Close) }
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
                                actionAfterKeyboardHide =
                                    { onNavigate(CreateCreditCardNavigation.SelectVault(event.shareId)) }

                            is CreditCardContentEvent.OnAttachmentEvent ->
                                when (event.event) {
                                    AttachmentContentEvent.OnAddAttachment ->
                                        onNavigate(AddAttachment)

                                    is AttachmentContentEvent.OnAttachmentOpen -> {
                                        // open attachment
                                    }

                                    is AttachmentContentEvent.OnAttachmentOptions ->
                                        onNavigate(OpenAttachmentOptions(event.event.attachmentId))

                                    AttachmentContentEvent.OnDeleteAllAttachments ->
                                        onNavigate(
                                            DeleteAllAttachments(
                                                uiState.baseState.attachmentsState.allToUnlink
                                            )
                                        )

                                    is AttachmentContentEvent.OnDraftAttachmentOpen ->
                                        viewModel.openDraftAttachment(
                                            contextHolder = context.toClassHolder(),
                                            uri = event.event.uri,
                                            mimetype = event.event.mimetype
                                        )

                                    is AttachmentContentEvent.OnDraftAttachmentOptions ->
                                        onNavigate(OpenDraftAttachmentOptions(event.event.uri))
                                }
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
                        actionAfterKeyboardHide = { onNavigate(Close) }
                    }
                )
            }
            ItemSavedLaunchedEffect(
                isItemSaved = uiState.baseState.isItemSaved,
                selectedShareId = selectedVault?.vault?.shareId,
                onSuccess = { _, _, model ->
                    actionAfterKeyboardHide =
                        { onNavigate(CreateCreditCardNavigation.ItemCreated(model)) }
                }
            )
            InAppReviewTriggerLaunchedEffect(
                triggerCondition = uiState.baseState.isItemSaved is ItemSavedState.Success
            )
        }
    }
}
