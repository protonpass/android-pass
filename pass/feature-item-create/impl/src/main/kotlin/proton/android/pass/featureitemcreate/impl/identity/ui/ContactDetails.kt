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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.featureitemcreate.impl.identity.navigation.IdentityContentEvent
import proton.android.pass.featureitemcreate.impl.identity.presentation.UIContactDetails
import proton.android.pass.featureitemcreate.impl.identity.ui.inputfields.LicenseNumberInput
import proton.android.pass.featureitemcreate.impl.identity.ui.inputfields.PassportNumberInput
import proton.android.pass.featureitemcreate.impl.identity.ui.inputfields.SecondPhoneNumberInput
import proton.android.pass.featureitemcreate.impl.identity.ui.inputfields.SocialSecurityNumberInput
import proton.android.pass.featureitemcreate.impl.identity.ui.inputfields.WebsiteInput
import proton.android.pass.featureitemcreate.impl.identity.ui.inputfields.XHandleInput

@Composable
internal fun ContactDetails(
    modifier: Modifier = Modifier,
    uiContactDetails: UIContactDetails,
    enabled: Boolean,
    onEvent: (IdentityContentEvent) -> Unit
) {
    Column(
        modifier = modifier.roundedContainerNorm()
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
                onEvent = {}
            )
        }
    }
}
