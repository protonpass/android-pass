package proton.android.pass.featureitemcreate.impl.creditcard

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import proton.android.pass.featureitemcreate.impl.common.CreateUpdateTopBar
import proton.android.pass.featureitemcreate.impl.common.attachments.AttachmentContentEvent
import proton.android.pass.featureitemcreate.impl.creditcard.CreditCardContentEvent.Submit
import proton.android.pass.featureitemcreate.impl.creditcard.CreditCardContentEvent.Up
import proton.android.pass.featureitemcreate.impl.creditcard.CreditCardContentEvent.Upgrade

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CreditCardContent(
    modifier: Modifier = Modifier,
    state: BaseCreditCardUiState,
    creditCardItemFormState: CreditCardItemFormState,
    topBarActionName: String,
    selectedShareId: ShareId?,
    selectedVault: Vault?,
    showVaultSelector: Boolean,
    onEvent: (CreditCardContentEvent) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CreateUpdateTopBar(
                text = topBarActionName,
                isLoading = state.isLoading,
                actionColor = PassTheme.colors.cardInteractionNormMajor1,
                iconColor = PassTheme.colors.cardInteractionNormMajor2,
                showUpgrade = state.isDowngradedMode,
                iconBackgroundColor = PassTheme.colors.cardInteractionNormMinor1,
                selectedVault = selectedVault,
                showVaultSelector = showVaultSelector,
                onCloseClick = { onEvent(Up) },
                onActionClick = {
                    selectedShareId ?: return@CreateUpdateTopBar
                    onEvent(Submit(selectedShareId))
                },
                onUpgrade = { onEvent(Upgrade) },
                onVaultSelectorClick = {
                    selectedShareId ?: return@CreateUpdateTopBar
                    onEvent(CreditCardContentEvent.OnVaultSelect(selectedShareId))
                }
            )
        }
    ) { padding ->
        CreditCardItemForm(
            modifier = Modifier.padding(padding),
            creditCardItemFormState = creditCardItemFormState,
            enabled = !state.isLoading,
            validationErrors = state.validationErrors,
            isFileAttachmentsEnabled = state.isFileAttachmentsEnabled,
            attachmentList = emptyList(),
            onEvent = onEvent
        )
    }
}

sealed interface CreditCardContentEvent {
    data object Up : CreditCardContentEvent
    data object Upgrade : CreditCardContentEvent

    @JvmInline
    value class Submit(val shareId: ShareId) : CreditCardContentEvent

    @JvmInline
    value class OnNameChange(val value: String) : CreditCardContentEvent

    @JvmInline
    value class OnNumberChange(val value: String) : CreditCardContentEvent

    @JvmInline
    value class OnCVVChange(val value: String) : CreditCardContentEvent

    @JvmInline
    value class OnCVVFocusChange(val isFocused: Boolean) : CreditCardContentEvent

    @JvmInline
    value class OnPinChange(val value: String) : CreditCardContentEvent

    @JvmInline
    value class OnPinFocusChange(val isFocused: Boolean) : CreditCardContentEvent

    @JvmInline
    value class OnExpirationDateChange(val value: String) : CreditCardContentEvent

    @JvmInline
    value class OnNoteChange(val value: String) : CreditCardContentEvent

    @JvmInline
    value class OnTitleChange(val value: String) : CreditCardContentEvent

    @JvmInline
    value class OnVaultSelect(val shareId: ShareId) : CreditCardContentEvent

    @JvmInline
    value class OnAttachmentEvent(val event: AttachmentContentEvent) : CreditCardContentEvent
}


