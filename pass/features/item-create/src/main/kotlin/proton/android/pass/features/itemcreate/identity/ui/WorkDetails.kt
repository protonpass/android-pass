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
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.RequestFocusLaunchedEffect
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.composecomponents.impl.utils.passItemColors
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.features.itemcreate.common.customfields.AddCustomFieldButton
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldEntry
import proton.android.pass.features.itemcreate.identity.navigation.IdentityContentEvent
import proton.android.pass.features.itemcreate.identity.navigation.IdentityContentEvent.OnCustomFieldOptions
import proton.android.pass.features.itemcreate.identity.navigation.IdentityContentEvent.OnFieldChange
import proton.android.pass.features.itemcreate.identity.presentation.FieldChange
import proton.android.pass.features.itemcreate.identity.presentation.UIWorkDetails
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.FocusedField
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.PersonalWebsite
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.WorkCustomField
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.WorkDetailsField
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.WorkEmail
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.WorkPhoneNumber
import proton.android.pass.features.itemcreate.identity.ui.IdentitySectionType.WorkDetails
import proton.android.pass.features.itemcreate.identity.ui.inputfields.CompanyInput
import proton.android.pass.features.itemcreate.identity.ui.inputfields.JobTitleInput
import proton.android.pass.features.itemcreate.identity.ui.inputfields.PersonalWebsiteInput
import proton.android.pass.features.itemcreate.identity.ui.inputfields.WorkEmailInput
import proton.android.pass.features.itemcreate.identity.ui.inputfields.WorkPhoneNumberInput

@Composable
internal fun WorkDetails(
    modifier: Modifier = Modifier,
    uiWorkDetails: UIWorkDetails,
    enabled: Boolean,
    extraFields: PersistentSet<WorkDetailsField>,
    focusedField: Option<FocusedField>,
    showAddWorkDetailsButton: Boolean,
    onEvent: (IdentityContentEvent) -> Unit
) {
    val field = focusedField.value()
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        Column(
            modifier = Modifier.roundedContainerNorm()
        ) {
            CompanyInput(
                value = uiWorkDetails.company,
                enabled = enabled,
                onChange = { onEvent(OnFieldChange(FieldChange.Company(it))) }
            )
            PassDivider()
            JobTitleInput(
                value = uiWorkDetails.jobTitle,
                enabled = enabled,
                onChange = { onEvent(OnFieldChange(FieldChange.JobTitle(it))) }
            )
            if (extraFields.contains(PersonalWebsite)) {
                PassDivider()
                PersonalWebsiteInput(
                    value = uiWorkDetails.personalWebsite,
                    enabled = enabled,
                    requestFocus = field?.extraField is PersonalWebsite,
                    onChange = { onEvent(OnFieldChange(FieldChange.PersonalWebsite(it))) },
                    onClearFocus = { onEvent(IdentityContentEvent.ClearLastAddedFieldFocus) }
                )
            }
            if (extraFields.contains(WorkPhoneNumber)) {
                PassDivider()
                WorkPhoneNumberInput(
                    value = uiWorkDetails.workPhoneNumber,
                    enabled = enabled,
                    requestFocus = field?.extraField is WorkPhoneNumber,
                    onChange = { onEvent(OnFieldChange(FieldChange.WorkPhoneNumber(it))) },
                    onClearFocus = { onEvent(IdentityContentEvent.ClearLastAddedFieldFocus) }
                )
            }
            if (extraFields.contains(WorkEmail)) {
                PassDivider()
                WorkEmailInput(
                    value = uiWorkDetails.workEmail,
                    enabled = enabled,
                    requestFocus = field?.extraField is WorkEmail,
                    onChange = { onEvent(OnFieldChange(FieldChange.WorkEmail(it))) },
                    onClearFocus = { onEvent(IdentityContentEvent.ClearLastAddedFieldFocus) }
                )
            }
        }
        uiWorkDetails.customFields.forEachIndexed { index, value ->
            val focusRequester = remember { FocusRequester() }
            CustomFieldEntry(
                modifier = Modifier.focusRequester(focusRequester),
                passItemColors = passItemColors(ItemCategory.Identity),
                entry = value,
                canEdit = enabled,
                isError = false,
                errorMessage = "",
                index = index,
                onValueChange = {
                    val fieldChange = FieldChange.CustomField(
                        sectionType = WorkDetails,
                        customFieldType = value.toCustomFieldType(),
                        index = index,
                        value = it
                    )
                    onEvent(OnFieldChange(fieldChange))
                },
                onClick = {
                    onEvent(
                        IdentityContentEvent.OnCustomFieldClick(
                            index = index,
                            customFieldType = value.toCustomFieldType(),
                            customExtraField = WorkCustomField
                        )
                    )
                },
                onFocusChange = { idx, isFocused ->
                    onEvent(IdentityContentEvent.OnCustomFieldFocused(idx, isFocused, WorkCustomField))
                },
                onOptionsClick = { onEvent(OnCustomFieldOptions(index, value.label, WorkCustomField)) }
            )
            RequestFocusLaunchedEffect(
                focusRequester = focusRequester,
                requestFocus = field?.extraField is WorkCustomField && field.index == index,
                callback = { onEvent(IdentityContentEvent.ClearLastAddedFieldFocus) }
            )
        }
        if (showAddWorkDetailsButton) {
            AddCustomFieldButton(
                passItemColors = passItemColors(ItemCategory.Identity),
                isEnabled = enabled,
                onClick = { onEvent(IdentityContentEvent.OnAddWorkField) }
            )
        }
    }
}

@Preview
@Composable
fun WorkDetailsPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            WorkDetails(
                uiWorkDetails = UIWorkDetails.EMPTY,
                enabled = true,
                extraFields = persistentSetOf(),
                focusedField = None,
                onEvent = {},
                showAddWorkDetailsButton = true
            )
        }
    }
}
