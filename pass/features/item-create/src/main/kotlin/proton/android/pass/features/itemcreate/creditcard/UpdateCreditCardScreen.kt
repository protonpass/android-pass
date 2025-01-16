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
import proton.android.pass.features.itemcreate.ItemSavedState
import proton.android.pass.features.itemcreate.R
import proton.android.pass.features.itemcreate.common.ItemSavedLaunchedEffect
import proton.android.pass.features.itemcreate.creditcard.BaseCreditCardNavigation.AddAttachment
import proton.android.pass.features.itemcreate.creditcard.BaseCreditCardNavigation.CloseScreen
import proton.android.pass.features.itemcreate.creditcard.BaseCreditCardNavigation.DeleteAllAttachments
import proton.android.pass.features.itemcreate.creditcard.BaseCreditCardNavigation.OpenAttachmentOptions
import proton.android.pass.features.itemcreate.creditcard.BaseCreditCardNavigation.OpenDraftAttachmentOptions
import proton.android.pass.features.itemcreate.creditcard.BaseCreditCardNavigation.Upgrade
import proton.android.pass.features.itemcreate.launchedeffects.InAppReviewTriggerLaunchedEffect
import proton.android.pass.features.itemcreate.login.PerformActionAfterKeyboardHide

@Suppress("ComplexMethod")
@Composable
fun UpdateCreditCardScreen(
    modifier: Modifier = Modifier,
    viewModel: UpdateCreditCardViewModel = hiltViewModel(),
    onNavigate: (BaseCreditCardNavigation) -> Unit
) {
    val context = LocalContext.current
    var actionAfterKeyboardHide by remember { mutableStateOf<(() -> Unit)?>(null) }
    PerformActionAfterKeyboardHide(
        action = actionAfterKeyboardHide,
        clearAction = { actionAfterKeyboardHide = null }
    )

    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val uiState = state) {
        UpdateCreditCardUiState.Error -> LaunchedEffect(Unit) {
            actionAfterKeyboardHide = { onNavigate(CloseScreen) }
        }

        UpdateCreditCardUiState.Loading,
        UpdateCreditCardUiState.NotInitialised -> {
        }

        is UpdateCreditCardUiState.Success -> {
            var showConfirmDialog by rememberSaveable { mutableStateOf(false) }
            val onExit = {
                if (uiState.baseState.hasUserEditedContent) {
                    showConfirmDialog = !showConfirmDialog
                } else {
                    viewModel.onClose()
                    actionAfterKeyboardHide = { onNavigate(CloseScreen) }
                }
            }
            BackHandler(onBack = onExit)

            Box(modifier = modifier.fillMaxSize()) {
                CreditCardContent(
                    state = uiState.baseState,
                    creditCardItemFormState = viewModel.creditCardItemFormState,
                    topBarActionName = stringResource(id = R.string.action_save),
                    selectedVault = null,
                    showVaultSelector = false,
                    selectedShareId = uiState.selectedShareId,
                    onEvent = {
                        when (it) {
                            is CreditCardContentEvent.OnCVVChange ->
                                viewModel.onCVVChanged(it.value)

                            is CreditCardContentEvent.OnExpirationDateChange ->
                                viewModel.onExpirationDateChanged(it.value)

                            is CreditCardContentEvent.OnNameChange ->
                                viewModel.onNameChanged(it.value)

                            is CreditCardContentEvent.OnNoteChange ->
                                viewModel.onNoteChanged(it.value)

                            is CreditCardContentEvent.OnNumberChange ->
                                viewModel.onNumberChanged(it.value)

                            is CreditCardContentEvent.OnPinChange ->
                                viewModel.onPinChanged(it.value)

                            is CreditCardContentEvent.Submit -> viewModel.update()
                            CreditCardContentEvent.Up -> onExit()
                            CreditCardContentEvent.Upgrade ->
                                actionAfterKeyboardHide = { onNavigate(Upgrade) }

                            is CreditCardContentEvent.OnCVVFocusChange ->
                                viewModel.onCVVFocusChanged(it.isFocused)

                            is CreditCardContentEvent.OnPinFocusChange ->
                                viewModel.onPinFocusChanged(it.isFocused)

                            is CreditCardContentEvent.OnTitleChange ->
                                viewModel.onTitleChange(it.value)

                            is CreditCardContentEvent.OnVaultSelect -> {}
                            is CreditCardContentEvent.OnAttachmentEvent ->
                                when (val event = it.event) {
                                    AttachmentContentEvent.OnAddAttachment ->
                                        onNavigate(AddAttachment)

                                    is AttachmentContentEvent.OnAttachmentOpen ->
                                        viewModel.openAttachment(
                                            contextHolder = context.toClassHolder(),
                                            attachment = event.attachment
                                        )

                                    is AttachmentContentEvent.OnAttachmentOptions ->
                                        onNavigate(
                                            OpenAttachmentOptions(
                                                shareId = event.shareId,
                                                itemId = event.itemId,
                                                attachmentId = event.attachmentId
                                            )
                                        )

                                    AttachmentContentEvent.OnDeleteAllAttachments ->
                                        onNavigate(
                                            DeleteAllAttachments(
                                                uiState.baseState.attachmentsState.allToUnlink
                                            )
                                        )

                                    is AttachmentContentEvent.OnDraftAttachmentOpen ->
                                        viewModel.openDraftAttachment(
                                            contextHolder = context.toClassHolder(),
                                            uri = event.uri,
                                            mimetype = event.mimetype
                                        )

                                    is AttachmentContentEvent.OnDraftAttachmentOptions ->
                                        onNavigate(OpenDraftAttachmentOptions(event.uri))

                                    is AttachmentContentEvent.OnDraftAttachmentRetry ->
                                        viewModel.retryUploadDraftAttachment(event.metadata)

                                    AttachmentContentEvent.UpsellAttachments ->
                                        onNavigate(BaseCreditCardNavigation.UpsellAttachments)
                                }

                            CreditCardContentEvent.DismissAttachmentBanner ->
                                viewModel.dismissFileAttachmentsOnboardingBanner()
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
                        actionAfterKeyboardHide = { onNavigate(CloseScreen) }
                    }
                )
            }
            ItemSavedLaunchedEffect(
                isItemSaved = uiState.baseState.isItemSaved,
                selectedShareId = uiState.selectedShareId,
                onSuccess = { shareId, itemId, _ ->
                    actionAfterKeyboardHide =
                        { onNavigate(UpdateCreditCardNavigation.ItemUpdated(shareId, itemId)) }
                }
            )
            InAppReviewTriggerLaunchedEffect(
                triggerCondition = uiState.baseState.isItemSaved is ItemSavedState.Success
            )
        }
    }
}
