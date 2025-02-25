/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.features.itemcreate.custom.createupdate.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import proton.android.pass.common.api.Option
import proton.android.pass.commonui.api.RequestFocusLaunchedEffect
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.features.itemcreate.common.UICustomFieldContent
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldEntry

@Composable
fun CustomFieldsList(
    modifier: Modifier = Modifier,
    customFields: List<UICustomFieldContent>,
    enabled: Boolean,
    sectionIndex: Option<Int>,
    focusedField: Option<CustomField>,
    onEvent: (ItemContentEvent) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        customFields.forEachIndexed { index, value ->
            val focusRequester = remember { FocusRequester() }
            CustomFieldEntry(
                modifier = Modifier.focusRequester(focusRequester),
                entry = value,
                canEdit = enabled,
                isError = false,
                errorMessage = "",
                index = index,
                onValueChange = {
                    val fieldChange = CustomField(
                        index = index,
                        value = it,
                        sectionIndex = sectionIndex
                    )
                    onEvent(ItemContentEvent.OnCustomFieldChange(fieldChange))
                },
                onFocusChange = { idx, isFocused ->
                    onEvent(ItemContentEvent.OnCustomFieldFocused(idx, isFocused, sectionIndex))
                },
                onOptionsClick = {
                    onEvent(
                        ItemContentEvent.OnCustomFieldOptions(
                            index = index,
                            label = value.label,
                            sectionIndex = sectionIndex
                        )
                    )
                }
            )

            RequestFocusLaunchedEffect(
                focusRequester = focusRequester,
                requestFocus = focusedField.value()?.let {
                    it.sectionIndex == sectionIndex && it.index == index
                } ?: false,
                callback = { onEvent(ItemContentEvent.ClearLastAddedFieldFocus) }
            )
        }
        AddCustomFieldButton(
            isEnabled = enabled,
            onClick = { onEvent(ItemContentEvent.OnAddCustomField(sectionIndex)) }
        )
    }
}
