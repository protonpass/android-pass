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
import proton.android.pass.features.itemcreate.identity.navigation.IdentityContentEvent.OnFocusChange
import proton.android.pass.features.itemcreate.identity.presentation.IdentityField
import proton.android.pass.features.itemcreate.identity.presentation.UIPersonalDetails
import proton.android.pass.features.itemcreate.identity.ui.IdentitySectionType.PersonalDetails
import proton.android.pass.features.itemcreate.identity.ui.inputfields.BirthdateInput
import proton.android.pass.features.itemcreate.identity.ui.inputfields.EmailInput
import proton.android.pass.features.itemcreate.identity.ui.inputfields.FirstNameInput
import proton.android.pass.features.itemcreate.identity.ui.inputfields.FullNameInput
import proton.android.pass.features.itemcreate.identity.ui.inputfields.GenderInput
import proton.android.pass.features.itemcreate.identity.ui.inputfields.LastNameInput
import proton.android.pass.features.itemcreate.identity.ui.inputfields.MiddleNameInput
import proton.android.pass.features.itemcreate.identity.ui.inputfields.PhoneNumberInput

@Composable
internal fun PersonalDetails(
    modifier: Modifier = Modifier,
    uiPersonalDetails: UIPersonalDetails,
    enabled: Boolean,
    extraFields: PersistentSet<IdentityField>,
    focusedField: Option<IdentityField>,
    showAddPersonalDetailsButton: Boolean,
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
            FullNameInput(
                value = uiPersonalDetails.fullName,
                enabled = enabled,
                onChange = { onEvent(OnFieldChange(IdentityField.FullName, it)) }
            )
            PassDivider()
            EmailInput(
                value = uiPersonalDetails.email,
                enabled = enabled,
                onChange = { onEvent(OnFieldChange(IdentityField.Email, it)) }
            )
            PassDivider()
            PhoneNumberInput(
                value = uiPersonalDetails.phoneNumber,
                enabled = enabled,
                onChange = { onEvent(OnFieldChange(IdentityField.PhoneNumber, it)) }
            )

            if (extraFields.contains(IdentityField.FirstName)) {
                PassDivider()
                FirstNameInput(
                    value = uiPersonalDetails.firstName,
                    enabled = enabled,
                    requestFocus = field is IdentityField.FirstName,
                    onChange = { onEvent(OnFieldChange(IdentityField.FirstName, it)) },
                    onFocusChange = { onEvent(OnFocusChange(IdentityField.FirstName, it)) }
                )
            }
            if (extraFields.contains(IdentityField.MiddleName)) {
                PassDivider()
                MiddleNameInput(
                    value = uiPersonalDetails.middleName,
                    enabled = enabled,
                    requestFocus = field is IdentityField.MiddleName,
                    onChange = { onEvent(OnFieldChange(IdentityField.MiddleName, it)) },
                    onFocusChange = { onEvent(OnFocusChange(IdentityField.MiddleName, it)) }
                )
            }
            if (extraFields.contains(IdentityField.LastName)) {
                PassDivider()
                LastNameInput(
                    value = uiPersonalDetails.lastName,
                    enabled = enabled,
                    requestFocus = field is IdentityField.LastName,
                    onChange = { onEvent(OnFieldChange(IdentityField.LastName, it)) },
                    onFocusChange = { onEvent(OnFocusChange(IdentityField.LastName, it)) }
                )
            }
            if (extraFields.contains(IdentityField.Birthdate)) {
                PassDivider()
                BirthdateInput(
                    value = uiPersonalDetails.birthdate,
                    enabled = enabled,
                    requestFocus = field is IdentityField.Birthdate,
                    onChange = { onEvent(OnFieldChange(IdentityField.Birthdate, it)) },
                    onFocusChange = { onEvent(OnFocusChange(IdentityField.Birthdate, it)) }
                )
            }
            if (extraFields.contains(IdentityField.Gender)) {
                PassDivider()
                GenderInput(
                    value = uiPersonalDetails.gender,
                    enabled = enabled,
                    requestFocus = field is IdentityField.Gender,
                    onChange = { onEvent(OnFieldChange(IdentityField.Gender, it)) },
                    onFocusChange = { onEvent(OnFocusChange(IdentityField.Gender, it)) }
                )
            }
        }
        uiPersonalDetails.customFields.forEachIndexed { index, entry ->
            val focusRequester = remember { FocusRequester() }
            val identityField = IdentityField.CustomField(
                sectionType = PersonalDetails,
                customFieldType = entry.toCustomFieldType(),
                index = index
            )
            CustomFieldEntry(
                modifier = Modifier.focusRequester(focusRequester),
                passItemColors = passItemColors(ItemCategory.Identity),
                entry = entry,
                canEdit = enabled,
                isError = false,
                errorMessage = "",
                index = index,
                onValueChange = {
                    onEvent(OnFieldChange(identityField, it))
                },
                onClick = {
                    onEvent(
                        IdentityContentEvent.OnCustomFieldClick(index, identityField)
                    )
                },
                onFocusChange = { idx, isFocused ->
                    onEvent(OnFocusChange(identityField, isFocused))
                },
                onOptionsClick = {
                    onEvent(OnCustomFieldOptions(index, entry.label, identityField))
                }
            )
            RequestFocusLaunchedEffect(
                focusRequester = focusRequester,
                requestFocus = field is IdentityField.CustomField &&
                    field.sectionType is PersonalDetails &&
                    field.index == index
            )
        }
        if (showAddPersonalDetailsButton) {
            AddCustomFieldButton(
                passItemColors = passItemColors(ItemCategory.Identity),
                isEnabled = enabled,
                onClick = { onEvent(IdentityContentEvent.OnAddPersonalDetailField) }
            )
        }
    }
}

@Preview
@Composable
fun PersonalDetailsPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            PersonalDetails(
                uiPersonalDetails = UIPersonalDetails.EMPTY,
                enabled = true,
                extraFields = persistentSetOf(),
                focusedField = None,
                showAddPersonalDetailsButton = true,
                onEvent = { }
            )
        }
    }
}
