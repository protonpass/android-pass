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
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.toClassHolder
import proton.android.pass.composecomponents.impl.attachments.AttachmentContentEvent
import proton.android.pass.composecomponents.impl.dialogs.ConfirmCloseDialog
import proton.android.pass.domain.CustomFieldType
import proton.android.pass.domain.ShareId
import proton.android.pass.features.itemcreate.ItemSavedState
import proton.android.pass.features.itemcreate.R
import proton.android.pass.features.itemcreate.common.ItemSavedLaunchedEffect
import proton.android.pass.features.itemcreate.common.ShareError.EmptyShareList
import proton.android.pass.features.itemcreate.common.ShareError.SharesNotAvailable
import proton.android.pass.features.itemcreate.common.ShareUiState
import proton.android.pass.features.itemcreate.common.UICustomFieldContent
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldEvent
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldIdentifier
import proton.android.pass.features.itemcreate.creditcard.BaseCreditCardNavigation.AddAttachment
import proton.android.pass.features.itemcreate.creditcard.BaseCreditCardNavigation.AddCustomField
import proton.android.pass.features.itemcreate.creditcard.BaseCreditCardNavigation.CloseScreen
import proton.android.pass.features.itemcreate.creditcard.BaseCreditCardNavigation.CustomFieldOptions
import proton.android.pass.features.itemcreate.creditcard.BaseCreditCardNavigation.DeleteAllAttachments
import proton.android.pass.features.itemcreate.creditcard.BaseCreditCardNavigation.OpenAttachmentOptions
import proton.android.pass.features.itemcreate.creditcard.BaseCreditCardNavigation.OpenDraftAttachmentOptions
import proton.android.pass.features.itemcreate.creditcard.BaseCreditCardNavigation.Upgrade
import proton.android.pass.features.itemcreate.creditcard.CreateCreditCardNavigation.SelectVault
import proton.android.pass.features.itemcreate.creditcard.CreditCardField.CustomField
import proton.android.pass.features.itemcreate.custom.createupdate.ui.DatePickerModal
import proton.android.pass.features.itemcreate.launchedeffects.InAppReviewTriggerLaunchedEffect
import proton.android.pass.features.itemcreate.login.PerformActionAfterKeyboardHide

@Composable
fun CreateCreditCardScreen(
    modifier: Modifier = Modifier,
    selectVault: ShareId?,
    navTotpUri: String? = null,
    navTotpIndex: Int? = null,
    canUseAttachments: Boolean,
    viewModel: CreateCreditCardViewModel = hiltViewModel(),
    onNavigate: (BaseCreditCardNavigation) -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(selectVault) {
        if (selectVault != null) {
            viewModel.changeVault(selectVault)
        }
    }
    LaunchedEffect(navTotpUri) {
        navTotpUri ?: return@LaunchedEffect
        viewModel.setTotp(navTotpUri, navTotpIndex ?: -1)
    }

    LaunchedEffect(Unit) {
        viewModel.duplicateContents(context)
    }

    var showDatePickerForField: Option<CustomFieldIdentifier> by remember { mutableStateOf(None) }

    var actionAfterKeyboardHide by remember { mutableStateOf<(() -> Unit)?>(null) }
    PerformActionAfterKeyboardHide(
        action = actionAfterKeyboardHide,
        clearAction = { actionAfterKeyboardHide = null }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val uiState = state) {
        CreateCreditCardUiState.Error -> LaunchedEffect(Unit) {
            actionAfterKeyboardHide = { onNavigate(CloseScreen) }
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
                    viewModel.clearDraftData()
                    actionAfterKeyboardHide = { onNavigate(CloseScreen) }
                }
            }
            BackHandler(onBack = onExit)
            val (showVaultSelector, selectedVault) = when (val shares = uiState.shareUiState) {
                ShareUiState.Loading,
                ShareUiState.NotInitialised -> false to null

                is ShareUiState.Error -> {
                    if (shares.shareError == EmptyShareList || shares.shareError == SharesNotAvailable) {
                        LaunchedEffect(Unit) {
                            actionAfterKeyboardHide = { onNavigate(CloseScreen) }
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
                    canUseAttachments = canUseAttachments,
                    topBarActionName = stringResource(id = R.string.title_create),
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

                            is CreditCardContentEvent.Submit -> viewModel.createItem()
                            CreditCardContentEvent.Up -> onExit()
                            CreditCardContentEvent.Upgrade -> onNavigate(Upgrade)
                            is CreditCardContentEvent.OnCVVFocusChange ->
                                viewModel.onCVVFocusChanged(it.isFocused)

                            is CreditCardContentEvent.OnPinFocusChange ->
                                viewModel.onPinFocusChanged(it.isFocused)

                            is CreditCardContentEvent.OnTitleChange ->
                                viewModel.onTitleChange(it.value)

                            is CreditCardContentEvent.OnVaultSelect ->
                                actionAfterKeyboardHide =
                                    { onNavigate(SelectVault(it.shareId)) }

                            is CreditCardContentEvent.OnAttachmentEvent ->
                                when (val event = it.event) {
                                    AttachmentContentEvent.OnAddAttachment ->
                                        onNavigate(AddAttachment)

                                    is AttachmentContentEvent.OnAttachmentOpen ->
                                        throw IllegalStateException("Cannot open attachment during create")

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

                            is CreditCardContentEvent.OnCustomFieldEvent ->
                                when (val event = it.event) {
                                    is CustomFieldEvent.OnAddField -> {
                                        actionAfterKeyboardHide = { onNavigate(AddCustomField) }
                                    }

                                    is CustomFieldEvent.OnFieldOptions -> {
                                        actionAfterKeyboardHide = {
                                            onNavigate(
                                                CustomFieldOptions(
                                                    currentValue = event.label,
                                                    index = event.field.index
                                                )
                                            )
                                        }
                                    }

                                    is CustomFieldEvent.OnValueChange -> {
                                        viewModel.onCustomFieldChange(event.field, event.value)
                                    }

                                    CustomFieldEvent.Upgrade -> {
                                        actionAfterKeyboardHide = { onNavigate(Upgrade) }
                                    }

                                    is CustomFieldEvent.FocusRequested ->
                                        viewModel.onFocusChange(
                                            field = CustomField(event.field),
                                            isFocused = event.isFocused
                                        )

                                    is CustomFieldEvent.OnFieldClick -> when (event.field.type) {
                                        CustomFieldType.Date -> {
                                            showDatePickerForField = Some(event.field)
                                        }
                                        else -> throw IllegalStateException("Unhandled action")
                                    }
                                }

                            is CreditCardContentEvent.OnScanTotp ->
                                actionAfterKeyboardHide =
                                    { onNavigate(BaseCreditCardNavigation.ScanTotp(it.index)) }
                            CreditCardContentEvent.PasteTotp -> viewModel.onPasteTotp()
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
                        viewModel.clearDraftData()
                        actionAfterKeyboardHide = { onNavigate(CloseScreen) }
                    }
                )
                showDatePickerForField.value()?.let { fieldIdentifier ->
                    val selectedDate = viewModel.creditCardItemFormState
                        .customFields[fieldIdentifier.index] as UICustomFieldContent.Date
                    DatePickerModal(
                        selectedDate = selectedDate.value,
                        onDateSelected = {
                            viewModel.onCustomFieldChange(fieldIdentifier, it.toString())
                        },
                        onDismiss = { showDatePickerForField = None }
                    )
                }
            }
            ItemSavedLaunchedEffect(
                isItemSaved = uiState.baseState.isItemSaved,
                selectedShareId = selectedVault?.vault?.shareId,
                onSuccess = { _, _, model ->
                    viewModel.clearDraftData()
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
