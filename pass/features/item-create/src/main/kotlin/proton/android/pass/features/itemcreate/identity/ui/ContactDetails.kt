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
import proton.android.pass.features.itemcreate.common.UIHiddenState
import proton.android.pass.features.itemcreate.common.customfields.AddCustomFieldButton
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldEntry
import proton.android.pass.features.itemcreate.identity.navigation.IdentityContentEvent
import proton.android.pass.features.itemcreate.identity.navigation.IdentityContentEvent.OnCustomFieldOptions
import proton.android.pass.features.itemcreate.identity.navigation.IdentityContentEvent.OnFieldChange
import proton.android.pass.features.itemcreate.identity.navigation.IdentityContentEvent.OnFocusChange
import proton.android.pass.features.itemcreate.identity.presentation.IdentityField
import proton.android.pass.features.itemcreate.identity.presentation.UIContactDetails
import proton.android.pass.features.itemcreate.identity.ui.IdentitySectionType.ContactDetails
import proton.android.pass.features.itemcreate.identity.ui.inputfields.FacebookInput
import proton.android.pass.features.itemcreate.identity.ui.inputfields.InstagramInput
import proton.android.pass.features.itemcreate.identity.ui.inputfields.LicenseNumberInput
import proton.android.pass.features.itemcreate.identity.ui.inputfields.LinkedinInput
import proton.android.pass.features.itemcreate.identity.ui.inputfields.PassportNumberInput
import proton.android.pass.features.itemcreate.identity.ui.inputfields.RedditInput
import proton.android.pass.features.itemcreate.identity.ui.inputfields.SecondPhoneNumberInput
import proton.android.pass.features.itemcreate.identity.ui.inputfields.SocialSecurityNumberInput
import proton.android.pass.features.itemcreate.identity.ui.inputfields.WebsiteInput
import proton.android.pass.features.itemcreate.identity.ui.inputfields.XHandleInput
import proton.android.pass.features.itemcreate.identity.ui.inputfields.YahooInput

@Composable
internal fun ContactDetails(
    modifier: Modifier = Modifier,
    uiContactDetails: UIContactDetails,
    enabled: Boolean,
    extraFields: PersistentSet<IdentityField>,
    focusedField: Option<IdentityField>,
    showAddContactDetailsButton: Boolean,
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
            SocialSecurityNumberInput(
                value = uiContactDetails.socialSecurityNumber,
                enabled = enabled,
                onChange = { newSocialSecurityNumber ->
                    OnFieldChange(IdentityField.SocialSecurityNumber, newSocialSecurityNumber)
                        .also(onEvent)
                },
                onFocusChange = { isFocused ->
                    OnFocusChange(IdentityField.SocialSecurityNumber, isFocused)
                        .also(onEvent)
                }
            )
            PassDivider()
            PassportNumberInput(
                value = uiContactDetails.passportNumber,
                enabled = enabled,
                onChange = { onEvent(OnFieldChange(IdentityField.PassportNumber, it)) }
            )
            PassDivider()
            LicenseNumberInput(
                value = uiContactDetails.licenseNumber,
                enabled = enabled,
                onChange = { onEvent(OnFieldChange(IdentityField.LicenseNumber, it)) }
            )
            PassDivider()
            WebsiteInput(
                value = uiContactDetails.website,
                enabled = enabled,
                onChange = { onEvent(OnFieldChange(IdentityField.Website, it)) }
            )
            PassDivider()
            XHandleInput(
                value = uiContactDetails.xHandle,
                enabled = enabled,
                onChange = { onEvent(OnFieldChange(IdentityField.XHandle, it)) }
            )
            PassDivider()
            SecondPhoneNumberInput(
                value = uiContactDetails.secondPhoneNumber,
                enabled = enabled,
                onChange = { onEvent(OnFieldChange(IdentityField.SecondPhoneNumber, it)) }
            )

            if (extraFields.contains(IdentityField.Linkedin)) {
                PassDivider()
                LinkedinInput(
                    value = uiContactDetails.linkedin,
                    enabled = enabled,
                    requestFocus = field is IdentityField.Linkedin,
                    onChange = { onEvent(OnFieldChange(IdentityField.Linkedin, it)) },
                    onFocusChange = { onEvent(OnFocusChange(IdentityField.Linkedin, it)) }
                )
            }
            if (extraFields.contains(IdentityField.Reddit)) {
                PassDivider()
                RedditInput(
                    value = uiContactDetails.reddit,
                    enabled = enabled,
                    requestFocus = field is IdentityField.Reddit,
                    onChange = { onEvent(OnFieldChange(IdentityField.Reddit, it)) },
                    onFocusChange = { onEvent(OnFocusChange(IdentityField.Reddit, it)) }
                )
            }
            if (extraFields.contains(IdentityField.Facebook)) {
                PassDivider()
                FacebookInput(
                    value = uiContactDetails.facebook,
                    enabled = enabled,
                    requestFocus = field is IdentityField.Facebook,
                    onChange = { onEvent(OnFieldChange(IdentityField.Facebook, it)) },
                    onFocusChange = { onEvent(OnFocusChange(IdentityField.Facebook, it)) }
                )
            }
            if (extraFields.contains(IdentityField.Yahoo)) {
                PassDivider()
                YahooInput(
                    value = uiContactDetails.yahoo,
                    enabled = enabled,
                    requestFocus = field is IdentityField.Yahoo,
                    onChange = { onEvent(OnFieldChange(IdentityField.Yahoo, it)) },
                    onFocusChange = { onEvent(OnFocusChange(IdentityField.Yahoo, it)) }
                )
            }
            if (extraFields.contains(IdentityField.Instagram)) {
                PassDivider()
                InstagramInput(
                    value = uiContactDetails.instagram,
                    enabled = enabled,
                    requestFocus = field is IdentityField.Instagram,
                    onChange = { onEvent(OnFieldChange(IdentityField.Instagram, it)) },
                    onFocusChange = { onEvent(OnFocusChange(IdentityField.Instagram, it)) }
                )
            }
        }
        uiContactDetails.customFields.forEachIndexed { index, entry ->
            val focusRequester = remember { FocusRequester() }
            val identityField = IdentityField.CustomField(
                sectionType = ContactDetails,
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
                    onEvent(IdentityContentEvent.OnCustomFieldClick(index, identityField))
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
                    field.sectionType is ContactDetails &&
                    field.index == index
            )
        }
        if (showAddContactDetailsButton) {
            AddCustomFieldButton(
                passItemColors = passItemColors(ItemCategory.Identity),
                isEnabled = enabled,
                onClick = { onEvent(IdentityContentEvent.OnAddContactDetailField) }
            )
        }
    }
}

@Preview
@Composable
fun ContactDetailsPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            ContactDetails(
                uiContactDetails = UIContactDetails(
                    socialSecurityNumber = UIHiddenState.Empty(""),
                    passportNumber = "",
                    licenseNumber = "",
                    website = "",
                    xHandle = "",
                    secondPhoneNumber = "",
                    linkedin = "",
                    reddit = "",
                    facebook = "",
                    yahoo = "",
                    instagram = "",
                    customFields = emptyList()
                ),
                enabled = true,
                extraFields = persistentSetOf(),
                focusedField = None,
                onEvent = {},
                showAddContactDetailsButton = true
            )
        }
    }
}
