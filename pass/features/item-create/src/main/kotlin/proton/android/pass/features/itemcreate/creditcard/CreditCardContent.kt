package proton.android.pass.features.itemcreate.creditcard

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import proton.android.pass.features.itemcreate.common.CreateUpdateTopBar
import proton.android.pass.features.itemcreate.common.CustomFieldValidationError
import proton.android.pass.features.itemcreate.creditcard.CreditCardContentEvent.Submit
import proton.android.pass.features.itemcreate.creditcard.CreditCardContentEvent.Up
import proton.android.pass.features.itemcreate.creditcard.CreditCardContentEvent.Upgrade

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun CreditCardContent(
    modifier: Modifier = Modifier,
    state: BaseCreditCardUiState,
    creditCardItemFormState: CreditCardItemFormState,
    topBarActionName: String,
    selectedShareId: ShareId?,
    selectedVault: Vault?,
    showVaultSelector: Boolean,
    canUseAttachments: Boolean,
    onEvent: (CreditCardContentEvent) -> Unit
) {
    Scaffold(
        modifier = modifier.systemBarsPadding(),
        topBar = {
            CreateUpdateTopBar(
                text = topBarActionName,
                isLoading = state.isLoading || state.attachmentsState.loadingDraftAttachments.isNotEmpty(),
                actionColor = PassTheme.colors.cardInteractionNormMajor1,
                iconColor = PassTheme.colors.cardInteractionNormMajor2,
                showUpgrade = !state.allowCreditCreditFreeUsers && !state.canPerformPaidAction,
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
            isFileAttachmentsEnabled = canUseAttachments,
            displayFileAttachmentsOnboarding = state.displayFileAttachmentsOnboarding,
            attachmentsState = state.attachmentsState,
            canUseCustomFields = state.canPerformPaidAction,
            customFieldValidationErrors = state.validationErrors
                .filterIsInstance<CustomFieldValidationError>()
                .toPersistentList(),
            focusedField = state.focusedField.value(),
            onEvent = onEvent
        )
    }
}


