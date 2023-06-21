package proton.android.pass.featureitemcreate.impl.creditcard

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.featureitemcreate.impl.common.CreateUpdateTopBar
import proton.android.pass.featureitemcreate.impl.creditcard.CreditCardContentEvent.Submit
import proton.android.pass.featureitemcreate.impl.creditcard.CreditCardContentEvent.Up
import proton.android.pass.featureitemcreate.impl.creditcard.CreditCardContentEvent.Upgrade
import proton.pass.domain.ShareId

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CreditCardContent(
    modifier: Modifier = Modifier,
    state: BaseCreditCardUiState,
    topBarActionName: String,
    titleSection: @Composable (ColumnScope.() -> Unit),
    onEvent: (CreditCardContentEvent) -> Unit,
    selectedShareId: ShareId?
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CreateUpdateTopBar(
                text = topBarActionName,
                isLoading = state.isLoading,
                actionColor = PassTheme.colors.cardInteractionNormMajor1,
                iconColor = PassTheme.colors.cardInteractionNormMajor2,
                iconBackgroundColor = PassTheme.colors.cardInteractionNormMinor1,
                onCloseClick = { onEvent(Up) },
                onActionClick = {
                    selectedShareId ?: return@CreateUpdateTopBar
                    onEvent(Submit(selectedShareId))
                },
                onUpgrade = { onEvent(Upgrade) }
            )
        }
    ) { padding ->
        CreditCardItemForm(
            modifier = Modifier.padding(padding),
            content = state.contents,
            enabled = !state.isLoading,
            titleSection = titleSection,
            onEvent = onEvent
        )
    }
}

sealed interface CreditCardContentEvent {
    object Up : CreditCardContentEvent
    object Upgrade : CreditCardContentEvent

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
    value class OnExpirationDateChange(val value: String) : CreditCardContentEvent

    @JvmInline
    value class OnNoteChange(val value: String) : CreditCardContentEvent
}


