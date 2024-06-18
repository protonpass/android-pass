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

package proton.android.pass.featureitemcreate.impl.identity.ui

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
import proton.android.pass.featureitemcreate.impl.common.customfields.AddMoreButton
import proton.android.pass.featureitemcreate.impl.common.customfields.CustomFieldEntry
import proton.android.pass.featureitemcreate.impl.identity.navigation.IdentityContentEvent
import proton.android.pass.featureitemcreate.impl.identity.navigation.IdentityContentEvent.OnCustomFieldOptions
import proton.android.pass.featureitemcreate.impl.identity.navigation.IdentityContentEvent.OnFieldChange
import proton.android.pass.featureitemcreate.impl.identity.presentation.FieldChange
import proton.android.pass.featureitemcreate.impl.identity.presentation.UIContactDetails
import proton.android.pass.featureitemcreate.impl.identity.presentation.bottomsheets.ContactCustomField
import proton.android.pass.featureitemcreate.impl.identity.presentation.bottomsheets.ContactDetailsField
import proton.android.pass.featureitemcreate.impl.identity.presentation.bottomsheets.Facebook
import proton.android.pass.featureitemcreate.impl.identity.presentation.bottomsheets.FocusedField
import proton.android.pass.featureitemcreate.impl.identity.presentation.bottomsheets.Instagram
import proton.android.pass.featureitemcreate.impl.identity.presentation.bottomsheets.Linkedin
import proton.android.pass.featureitemcreate.impl.identity.presentation.bottomsheets.Reddit
import proton.android.pass.featureitemcreate.impl.identity.presentation.bottomsheets.Yahoo
import proton.android.pass.featureitemcreate.impl.identity.ui.IdentitySectionType.ContactDetails
import proton.android.pass.featureitemcreate.impl.identity.ui.inputfields.FacebookInput
import proton.android.pass.featureitemcreate.impl.identity.ui.inputfields.InstagramInput
import proton.android.pass.featureitemcreate.impl.identity.ui.inputfields.LicenseNumberInput
import proton.android.pass.featureitemcreate.impl.identity.ui.inputfields.LinkedinInput
import proton.android.pass.featureitemcreate.impl.identity.ui.inputfields.PassportNumberInput
import proton.android.pass.featureitemcreate.impl.identity.ui.inputfields.RedditInput
import proton.android.pass.featureitemcreate.impl.identity.ui.inputfields.SecondPhoneNumberInput
import proton.android.pass.featureitemcreate.impl.identity.ui.inputfields.SocialSecurityNumberInput
import proton.android.pass.featureitemcreate.impl.identity.ui.inputfields.WebsiteInput
import proton.android.pass.featureitemcreate.impl.identity.ui.inputfields.XHandleInput
import proton.android.pass.featureitemcreate.impl.identity.ui.inputfields.YahooInput

@Composable
internal fun ContactDetails(
    modifier: Modifier = Modifier,
    uiContactDetails: UIContactDetails,
    enabled: Boolean,
    extraFields: PersistentSet<ContactDetailsField>,
    focusedField: Option<FocusedField>,
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
                onChange = { onEvent(OnFieldChange(FieldChange.SocialSecurityNumber(it))) }
            )
            PassDivider()
            PassportNumberInput(
                value = uiContactDetails.passportNumber,
                enabled = enabled,
                onChange = { onEvent(OnFieldChange(FieldChange.PassportNumber(it))) }
            )
            PassDivider()
            LicenseNumberInput(
                value = uiContactDetails.licenseNumber,
                enabled = enabled,
                onChange = { onEvent(OnFieldChange(FieldChange.LicenseNumber(it))) }
            )
            PassDivider()
            WebsiteInput(
                value = uiContactDetails.website,
                enabled = enabled,
                onChange = { onEvent(OnFieldChange(FieldChange.Website(it))) }
            )
            PassDivider()
            XHandleInput(
                value = uiContactDetails.xHandle,
                enabled = enabled,
                onChange = { onEvent(OnFieldChange(FieldChange.XHandle(it))) }
            )
            PassDivider()
            SecondPhoneNumberInput(
                value = uiContactDetails.secondPhoneNumber,
                enabled = enabled,
                onChange = { onEvent(OnFieldChange(FieldChange.SecondPhoneNumber(it))) }
            )

            if (extraFields.contains(Linkedin)) {
                PassDivider()
                LinkedinInput(
                    value = uiContactDetails.linkedin,
                    enabled = enabled,
                    requestFocus = field?.extraField is Linkedin,
                    onChange = { onEvent(OnFieldChange(FieldChange.Linkedin(it))) },
                    onClearFocus = { onEvent(IdentityContentEvent.ClearLastAddedFieldFocus) }
                )
            }
            if (extraFields.contains(Reddit)) {
                PassDivider()
                RedditInput(
                    value = uiContactDetails.reddit,
                    enabled = enabled,
                    requestFocus = field?.extraField is Reddit,
                    onChange = { onEvent(OnFieldChange(FieldChange.Reddit(it))) },
                    onClearFocus = { onEvent(IdentityContentEvent.ClearLastAddedFieldFocus) }
                )
            }
            if (extraFields.contains(Facebook)) {
                PassDivider()
                FacebookInput(
                    value = uiContactDetails.facebook,
                    enabled = enabled,
                    requestFocus = field?.extraField is Facebook,
                    onChange = { onEvent(OnFieldChange(FieldChange.Facebook(it))) },
                    onClearFocus = { onEvent(IdentityContentEvent.ClearLastAddedFieldFocus) }
                )
            }
            if (extraFields.contains(Yahoo)) {
                PassDivider()
                YahooInput(
                    value = uiContactDetails.yahoo,
                    enabled = enabled,
                    requestFocus = field?.extraField is Yahoo,
                    onChange = { onEvent(OnFieldChange(FieldChange.Yahoo(it))) },
                    onClearFocus = { onEvent(IdentityContentEvent.ClearLastAddedFieldFocus) }
                )
            }
            if (extraFields.contains(Instagram)) {
                PassDivider()
                InstagramInput(
                    value = uiContactDetails.instagram,
                    enabled = enabled,
                    requestFocus = field?.extraField is Instagram,
                    onChange = { onEvent(OnFieldChange(FieldChange.Instagram(it))) },
                    onClearFocus = { onEvent(IdentityContentEvent.ClearLastAddedFieldFocus) }
                )
            }
        }
        uiContactDetails.customFields.forEachIndexed { index, value ->
            val focusRequester = remember { FocusRequester() }
            CustomFieldEntry(
                modifier = Modifier.focusRequester(focusRequester),
                entry = value,
                canEdit = enabled,
                isError = false,
                errorMessage = "",
                index = index,
                onValueChange = {
                    val fieldChange = FieldChange.CustomField(
                        sectionType = ContactDetails,
                        customFieldType = value.toCustomFieldType(),
                        index = index,
                        value = it
                    )
                    onEvent(OnFieldChange(fieldChange))
                },
                onFocusChange = { _, _ -> },
                onOptionsClick = { onEvent(OnCustomFieldOptions(index, value.label, ContactCustomField)) }
            )
            RequestFocusLaunchedEffect(
                focusRequester = focusRequester,
                requestFocus = field?.extraField is ContactCustomField && field.index == index,
                callback = { onEvent(IdentityContentEvent.ClearLastAddedFieldFocus) }
            )
        }
        AddMoreButton(onClick = { onEvent(IdentityContentEvent.OnAddContactDetailField) })
    }
}

@Preview
@Composable
fun ContactDetailsPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            ContactDetails(
                uiContactDetails = UIContactDetails.EMPTY,
                enabled = true,
                extraFields = persistentSetOf(),
                focusedField = None,
                onEvent = {}
            )
        }
    }
}
