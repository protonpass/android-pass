/*
 * Copyright (c) 2024-2025 Proton AG
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

package proton.android.pass.features.itemcreate.common.customfields

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.PersistentSet
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.getOrElse
import proton.android.pass.commonui.api.RequestFocusLaunchedEffect
import proton.android.pass.composecomponents.impl.utils.passItemColors
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.features.itemcreate.R
import proton.android.pass.features.itemcreate.common.CustomFieldValidationError
import proton.android.pass.features.itemcreate.common.UICustomFieldContent

@Suppress("LongParameterList", "LongMethod")
internal fun LazyListScope.customFieldsList(
    modifier: Modifier = Modifier,
    customFields: List<UICustomFieldContent>,
    enabled: Boolean,
    errors: PersistentSet<CustomFieldValidationError>,
    isVisible: Boolean,
    canCreateCustomFields: Boolean,
    sectionIndex: Option<Int>,
    focusedField: Option<CustomFieldIdentifier>,
    itemCategory: ItemCategory,
    onEvent: (CustomFieldEvent) -> Unit
) {
    itemsIndexed(
        items = customFields,
        key = { index, _ -> "${sectionIndex.getOrElse { -1 }}/$index" }
    ) { index, entry ->
        val focusRequester = remember { FocusRequester() }
        val field = CustomFieldIdentifier(
            sectionIndex = sectionIndex,
            index = index,
            type = entry.toCustomFieldType()
        )
        AnimatedVisibility(
            modifier = modifier.fillMaxWidth(),
            visible = isVisible
        ) {
            CustomFieldEntry(
                modifier = Modifier.focusRequester(focusRequester),
                passItemColors = passItemColors(itemCategory),
                entry = entry,
                canEdit = enabled,
                showLeadingIcon = false,
                isError = errors.contains(CustomFieldValidationError.EmptyField(sectionIndex, index)) ||
                    errors.contains(CustomFieldValidationError.InvalidTotp(sectionIndex, index)),
                errorMessage = when {
                    errors.contains(CustomFieldValidationError.EmptyField(sectionIndex, index)) ->
                        stringResource(R.string.field_cannot_be_empty)

                    errors.contains(CustomFieldValidationError.InvalidTotp(sectionIndex, index)) ->
                        stringResource(R.string.create_login_invalid_totp)

                    else -> ""
                },
                index = index,
                onValueChange = { newValue ->
                    onEvent(CustomFieldEvent.OnValueChange(field, newValue))
                },
                onClick = {
                    onEvent(CustomFieldEvent.OnFieldClick(field))
                },
                onFocusChange = { _, isFocused ->
                    onEvent(CustomFieldEvent.FocusRequested(field, isFocused))
                },
                onOptionsClick = {
                    onEvent(CustomFieldEvent.OnFieldOptions(field, entry.label))
                }
            )
        }
        RequestFocusLaunchedEffect(
            focusRequester = focusRequester,
            requestFocus = field == focusedField.value()
        )
    }
    if (canCreateCustomFields) {
        item(key = "${sectionIndex.getOrElse { -1 }}/add_custom_field") {
            AnimatedVisibility(
                modifier = modifier.fillMaxWidth(),
                visible = isVisible
            ) {
                AddCustomFieldButton(
                    isEnabled = enabled,
                    passItemColors = passItemColors(itemCategory),
                    onClick = { onEvent(CustomFieldEvent.OnAddField(sectionIndex)) }
                )
            }
        }
    }
}
