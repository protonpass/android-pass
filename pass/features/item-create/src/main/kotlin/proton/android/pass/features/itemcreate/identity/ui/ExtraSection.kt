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

package proton.android.pass.features.itemcreate.identity.ui

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
import proton.android.pass.composecomponents.impl.utils.passItemColors
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.features.itemcreate.common.UIExtraSection
import proton.android.pass.features.itemcreate.common.customfields.AddCustomFieldButton
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldEntry
import proton.android.pass.features.itemcreate.identity.navigation.IdentityContentEvent
import proton.android.pass.features.itemcreate.identity.navigation.IdentityContentEvent.OnAddExtraSectionCustomField
import proton.android.pass.features.itemcreate.identity.navigation.IdentityContentEvent.OnCustomFieldClick
import proton.android.pass.features.itemcreate.identity.navigation.IdentityContentEvent.OnCustomFieldOptions
import proton.android.pass.features.itemcreate.identity.navigation.IdentityContentEvent.OnFieldChange
import proton.android.pass.features.itemcreate.identity.presentation.IdentityField
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.ExtraSectionCustomField
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.FocusedField

@Composable
fun ExtraSection(
    modifier: Modifier = Modifier,
    section: UIExtraSection,
    enabled: Boolean,
    sectionIndex: Int,
    focusedField: Option<FocusedField>,
    onEvent: (IdentityContentEvent) -> Unit
) {
    val field = focusedField.value()
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        section.customFields.forEachIndexed { index, value ->
            val focusRequester = remember { FocusRequester() }
            val customExtraField = ExtraSectionCustomField(value.toCustomFieldType(), sectionIndex)
            val identityField = IdentityField.CustomField(
                sectionType = IdentitySectionType.ExtraSection(sectionIndex),
                customFieldType = value.toCustomFieldType(),
                index = index
            )
            CustomFieldEntry(
                modifier = Modifier.focusRequester(focusRequester),
                passItemColors = passItemColors(ItemCategory.Identity),
                entry = value,
                canEdit = enabled,
                isError = false,
                errorMessage = "",
                index = index,
                onValueChange = {
                    onEvent(OnFieldChange(identityField, it))
                },
                onClick = {
                    onEvent(
                        OnCustomFieldClick(
                            index = index,
                            customExtraField = customExtraField
                        )
                    )
                },
                onFocusChange = { idx, isFocused ->
                    onEvent(IdentityContentEvent.OnFocusChange(identityField, isFocused))
                },
                onOptionsClick = {
                    onEvent(
                        OnCustomFieldOptions(
                            index = index,
                            label = value.label,
                            customExtraField = customExtraField
                        )
                    )
                }
            )
            RequestFocusLaunchedEffect(
                focusRequester = focusRequester,
                requestFocus = field?.extraField is ExtraSectionCustomField &&
                    field.extraField.index == sectionIndex &&
                    field.index == index
            )
        }
        AddCustomFieldButton(
            passItemColors = passItemColors(ItemCategory.Identity),
            isEnabled = enabled,
            onClick = { onEvent(OnAddExtraSectionCustomField(sectionIndex)) }
        )
    }
}
