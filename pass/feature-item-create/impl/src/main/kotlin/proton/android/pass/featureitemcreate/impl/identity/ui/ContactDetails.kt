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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.featureitemcreate.impl.identity.presentation.ContactDetails
import proton.android.pass.featureitemcreate.impl.identity.navigation.IdentityContentEvent
import proton.android.pass.featureitemcreate.impl.identity.ui.inputfields.LicenseNumberInput
import proton.android.pass.featureitemcreate.impl.identity.ui.inputfields.PassportNumberInput
import proton.android.pass.featureitemcreate.impl.identity.ui.inputfields.SocialSecurityNumberInput
import proton.android.pass.featureitemcreate.impl.identity.ui.inputfields.WebsiteInput
import proton.android.pass.featureitemcreate.impl.identity.ui.inputfields.XHandleInput

@Composable
internal fun ContactDetails(
    modifier: Modifier = Modifier,
    contactDetails: ContactDetails,
    onEvent: (IdentityContentEvent) -> Unit
) {
    Column(
        modifier = modifier.roundedContainerNorm()
    ) {
        SocialSecurityNumberInput(
            value = contactDetails.socialSecurityNumber,
            enabled = true,
            onChange = { onEvent(IdentityContentEvent.OnFullNameChange(it)) }
        )
        PassDivider()
        PassportNumberInput(
            value = contactDetails.passportNumber,
            enabled = true,
            onChange = { onEvent(IdentityContentEvent.OnEmailChange(it)) }
        )
        PassDivider()
        LicenseNumberInput(
            value = contactDetails.licenseNumber,
            enabled = true,
            onChange = { onEvent(IdentityContentEvent.OnEmailChange(it)) }
        )
        PassDivider()
        WebsiteInput(
            value = contactDetails.website,
            enabled = true,
            onChange = { onEvent(IdentityContentEvent.OnEmailChange(it)) }
        )
        PassDivider()
        XHandleInput(
            value = contactDetails.xHandle,
            enabled = true,
            onChange = { onEvent(IdentityContentEvent.OnEmailChange(it)) }
        )
    }
}
