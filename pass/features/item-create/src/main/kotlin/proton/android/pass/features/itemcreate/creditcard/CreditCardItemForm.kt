/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.features.itemcreate.creditcard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.PersistentSet
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.composecomponents.impl.attachments.AttachmentSection
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.SimpleNoteSection
import proton.android.pass.composecomponents.impl.form.TitleSection
import proton.android.pass.composecomponents.impl.utils.passItemColors
import proton.android.pass.domain.ItemDiffs
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.features.itemcreate.creditcard.CreditCardContentEvent.OnAttachmentEvent
import proton.android.pass.features.itemcreate.creditcard.CreditCardContentEvent.OnCVVChange
import proton.android.pass.features.itemcreate.creditcard.CreditCardContentEvent.OnCVVFocusChange
import proton.android.pass.features.itemcreate.creditcard.CreditCardContentEvent.OnExpirationDateChange
import proton.android.pass.features.itemcreate.creditcard.CreditCardContentEvent.OnNameChange
import proton.android.pass.features.itemcreate.creditcard.CreditCardContentEvent.OnNoteChange
import proton.android.pass.features.itemcreate.creditcard.CreditCardContentEvent.OnNumberChange
import proton.android.pass.features.itemcreate.creditcard.CreditCardContentEvent.OnPinChange
import proton.android.pass.features.itemcreate.creditcard.CreditCardContentEvent.OnPinFocusChange
import proton.android.pass.features.itemcreate.creditcard.CreditCardContentEvent.OnTitleChange

@Composable
fun CreditCardItemForm(
    modifier: Modifier = Modifier,
    creditCardItemFormState: CreditCardItemFormState,
    enabled: Boolean,
    validationErrors: PersistentSet<CreditCardValidationErrors>,
    isFileAttachmentsEnabled: Boolean,
    attachmentsState: AttachmentsState,
    onEvent: (CreditCardContentEvent) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        TitleSection(
            modifier = Modifier.roundedContainerNorm()
                .padding(
                    start = Spacing.medium,
                    top = Spacing.medium,
                    end = Spacing.extraSmall,
                    bottom = Spacing.medium
                ),
            value = creditCardItemFormState.title,
            requestFocus = true,
            onTitleRequiredError = validationErrors
                .contains(CreditCardValidationErrors.BlankTitle),
            enabled = enabled,
            isRounded = true,
            onChange = { onEvent(OnTitleChange(it)) }
        )
        CardDetails(
            creditCardItemFormState = creditCardItemFormState,
            enabled = enabled,
            validationErrors = validationErrors,
            onNameChanged = { onEvent(OnNameChange(it)) },
            onNumberChanged = { onEvent(OnNumberChange(it)) },
            onCVVChanged = { onEvent(OnCVVChange(it)) },
            onPinChanged = { onEvent(OnPinChange(it)) },
            onExpirationDateChanged = { onEvent(OnExpirationDateChange(it)) },
            onCVVFocusChange = { onEvent(OnCVVFocusChange(it)) },
            onPinFocusChange = { onEvent(OnPinFocusChange(it)) }
        )
        SimpleNoteSection(
            value = creditCardItemFormState.note,
            enabled = enabled,
            onChange = { onEvent(OnNoteChange(it)) }
        )
        if (isFileAttachmentsEnabled) {
            AttachmentSection(
                attachmentsState = attachmentsState,
                isDetail = false,
                itemColors = passItemColors(ItemCategory.CreditCard),
                itemDiffs = ItemDiffs.None,
                onEvent = { onEvent(OnAttachmentEvent(it)) }
            )
        }
    }
}
