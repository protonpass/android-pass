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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.toPersistentSet
import proton.android.pass.common.api.None
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.composecomponents.impl.attachments.AttachmentSection
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.SimpleNoteSection
import proton.android.pass.composecomponents.impl.form.TitleSection
import proton.android.pass.composecomponents.impl.utils.passItemColors
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.features.itemcreate.attachments.banner.AttachmentBanner
import proton.android.pass.features.itemcreate.common.CommonFieldValidationError
import proton.android.pass.features.itemcreate.common.CustomFieldValidationError
import proton.android.pass.features.itemcreate.common.ValidationError
import proton.android.pass.features.itemcreate.common.customfields.customFieldsList
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
internal fun CreditCardItemForm(
    modifier: Modifier = Modifier,
    creditCardItemFormState: CreditCardItemFormState,
    enabled: Boolean,
    validationErrors: PersistentSet<ValidationError>,
    displayFileAttachmentsOnboarding: Boolean,
    isFileAttachmentsEnabled: Boolean,
    isCustomTypeEnabled: Boolean,
    attachmentsState: AttachmentsState,
    canUseCustomFields: Boolean,
    focusedField: CreditCardField?,
    customFieldValidationErrors: ImmutableList<CustomFieldValidationError>,
    onEvent: (CreditCardContentEvent) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.medium)
    ) {
        item {
            AnimatedVisibility(isFileAttachmentsEnabled && displayFileAttachmentsOnboarding) {
                AttachmentBanner(Modifier.padding(bottom = Spacing.mediumSmall)) {
                    onEvent(CreditCardContentEvent.DismissAttachmentBanner)
                }
            }
        }

        item {
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
                    .contains(CommonFieldValidationError.BlankTitle),
                enabled = enabled,
                isRounded = true,
                onChange = { onEvent(OnTitleChange(it)) }
            )
        }
        item {
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
        }
        item {
            SimpleNoteSection(
                value = creditCardItemFormState.note,
                enabled = enabled,
                onChange = { onEvent(OnNoteChange(it)) }
            )
        }

        if (isCustomTypeEnabled) {
            customFieldsList(
                modifier = Modifier.padding(vertical = Spacing.extraSmall),
                customFields = creditCardItemFormState.customFields,
                enabled = enabled,
                errors = customFieldValidationErrors.toPersistentSet(),
                isVisible = true,
                canCreateCustomFields = canUseCustomFields,
                sectionIndex = None,
                focusedField = (focusedField as? CreditCardField.CustomField)?.field.toOption(),
                itemCategory = ItemCategory.CreditCard,
                onEvent = { onEvent(CreditCardContentEvent.OnCustomFieldEvent(it)) }
            )
        }

        if (isFileAttachmentsEnabled) {
            item {
                AttachmentSection(
                    attachmentsState = attachmentsState,
                    isDetail = false,
                    itemColors = passItemColors(ItemCategory.CreditCard),
                    onEvent = { onEvent(OnAttachmentEvent(it)) }
                )
            }
        }
    }
}
