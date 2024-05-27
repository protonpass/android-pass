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

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.featureitemcreate.impl.identity.navigation.IdentityContentEvent
import proton.android.pass.featureitemcreate.impl.identity.presentation.UIContactDetails
import proton.android.pass.featureitemcreate.impl.identity.presentation.bottomsheets.ContactDetailsField
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
    onEvent: (IdentityContentEvent) -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        Column(
            modifier = Modifier.roundedContainerNorm()
        ) {
            SocialSecurityNumberInput(
                value = uiContactDetails.socialSecurityNumber,
                enabled = enabled,
                onChange = { onEvent(IdentityContentEvent.OnSocialSecurityNumberChange(it)) }
            )
            PassDivider()
            PassportNumberInput(
                value = uiContactDetails.passportNumber,
                enabled = enabled,
                onChange = { onEvent(IdentityContentEvent.OnPassportNumberChange(it)) }
            )
            PassDivider()
            LicenseNumberInput(
                value = uiContactDetails.licenseNumber,
                enabled = enabled,
                onChange = { onEvent(IdentityContentEvent.OnLicenseNumberChange(it)) }
            )
            PassDivider()
            WebsiteInput(
                value = uiContactDetails.website,
                enabled = enabled,
                onChange = { onEvent(IdentityContentEvent.OnWebsiteChange(it)) }
            )
            PassDivider()
            XHandleInput(
                value = uiContactDetails.xHandle,
                enabled = enabled,
                onChange = { onEvent(IdentityContentEvent.OnXHandleChange(it)) }
            )
            PassDivider()
            SecondPhoneNumberInput(
                value = uiContactDetails.secondPhoneNumber,
                enabled = enabled,
                onChange = { onEvent(IdentityContentEvent.OnSecondPhoneNumberChange(it)) }
            )

            if (extraFields.contains(ContactDetailsField.Linkedin)) {
                PassDivider()
                LinkedinInput(
                    value = uiContactDetails.linkedin,
                    enabled = enabled,
                    onChange = { onEvent(IdentityContentEvent.OnLinkedinChange(it)) }
                )
            }
            if (extraFields.contains(ContactDetailsField.Reddit)) {
                PassDivider()
                RedditInput(
                    value = uiContactDetails.reddit,
                    enabled = enabled,
                    onChange = { onEvent(IdentityContentEvent.OnRedditChange(it)) }
                )
            }
            if (extraFields.contains(ContactDetailsField.Facebook)) {
                PassDivider()
                FacebookInput(
                    value = uiContactDetails.facebook,
                    enabled = enabled,
                    onChange = { onEvent(IdentityContentEvent.OnFacebookChange(it)) }
                )
            }
            if (extraFields.contains(ContactDetailsField.Yahoo)) {
                PassDivider()
                YahooInput(
                    value = uiContactDetails.yahoo,
                    enabled = enabled,
                    onChange = { onEvent(IdentityContentEvent.OnYahooChange(it)) }
                )
            }
            if (extraFields.contains(ContactDetailsField.Instagram)) {
                PassDivider()
                InstagramInput(
                    value = uiContactDetails.instagram,
                    enabled = enabled,
                    onChange = { onEvent(IdentityContentEvent.OnInstagramChange(it)) }
                )
            }
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
                onEvent = {}
            )
        }
    }
}
