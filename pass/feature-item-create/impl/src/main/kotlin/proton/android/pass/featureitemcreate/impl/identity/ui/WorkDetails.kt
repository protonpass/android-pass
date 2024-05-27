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
import proton.android.pass.featureitemcreate.impl.identity.presentation.UIWorkDetails
import proton.android.pass.featureitemcreate.impl.identity.presentation.bottomsheets.WorkDetailsField
import proton.android.pass.featureitemcreate.impl.identity.ui.inputfields.CompanyInput
import proton.android.pass.featureitemcreate.impl.identity.ui.inputfields.JobTitleInput
import proton.android.pass.featureitemcreate.impl.identity.ui.inputfields.PersonalWebsiteInput
import proton.android.pass.featureitemcreate.impl.identity.ui.inputfields.WorkEmailInput
import proton.android.pass.featureitemcreate.impl.identity.ui.inputfields.WorkPhoneNumberInput

@Composable
internal fun WorkDetails(
    modifier: Modifier = Modifier,
    uiWorkDetails: UIWorkDetails,
    enabled: Boolean,
    extraFields: PersistentSet<WorkDetailsField>,
    onEvent: (IdentityContentEvent) -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        Column(
            modifier = Modifier.roundedContainerNorm()
        ) {
            CompanyInput(
                value = uiWorkDetails.company,
                enabled = enabled,
                onChange = { onEvent(IdentityContentEvent.OnCompanyChange(it)) }
            )
            PassDivider()
            JobTitleInput(
                value = uiWorkDetails.jobTitle,
                enabled = enabled,
                onChange = { onEvent(IdentityContentEvent.OnJobTitleChange(it)) }
            )
            if (extraFields.contains(WorkDetailsField.PersonalWebsite)) {
                PassDivider()
                PersonalWebsiteInput(
                    value = uiWorkDetails.personalWebsite,
                    enabled = enabled,
                    onChange = { onEvent(IdentityContentEvent.OnPersonalWebsiteChange(it)) }
                )
            }
            if (extraFields.contains(WorkDetailsField.WorkPhoneNumber)) {
                PassDivider()
                WorkPhoneNumberInput(
                    value = uiWorkDetails.workPhoneNumber,
                    enabled = enabled,
                    onChange = { onEvent(IdentityContentEvent.OnWorkPhoneNumberChange(it)) }
                )
            }
            if (extraFields.contains(WorkDetailsField.WorkEmail)) {
                PassDivider()
                WorkEmailInput(
                    value = uiWorkDetails.workEmail,
                    enabled = enabled,
                    onChange = { onEvent(IdentityContentEvent.OnWorkEmailChange(it)) }
                )
            }
        }
        AddMoreButton(onClick = { onEvent(IdentityContentEvent.OnAddWorkField) })
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
                onEvent = {}
            )
        }
    }
}
