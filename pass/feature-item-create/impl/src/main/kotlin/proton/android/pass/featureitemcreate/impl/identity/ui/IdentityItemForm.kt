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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.toPersistentSet
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.isCollapsedSaver
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.TitleSection
import proton.android.pass.composecomponents.impl.labels.CollapsibleSectionHeader
import proton.android.pass.featureitemcreate.impl.R
import proton.android.pass.featureitemcreate.impl.identity.navigation.IdentityContentEvent
import proton.android.pass.featureitemcreate.impl.identity.navigation.IdentityContentEvent.OnFieldChange
import proton.android.pass.featureitemcreate.impl.identity.presentation.FieldChange
import proton.android.pass.featureitemcreate.impl.identity.presentation.IdentityItemFormState
import proton.android.pass.featureitemcreate.impl.identity.presentation.IdentityValidationErrors
import proton.android.pass.featureitemcreate.impl.identity.presentation.bottomsheets.AddressDetailsField
import proton.android.pass.featureitemcreate.impl.identity.presentation.bottomsheets.ContactDetailsField
import proton.android.pass.featureitemcreate.impl.identity.presentation.bottomsheets.ExtraField
import proton.android.pass.featureitemcreate.impl.identity.presentation.bottomsheets.PersonalDetailsField
import proton.android.pass.featureitemcreate.impl.identity.presentation.bottomsheets.WorkDetailsField

@Composable
fun IdentityItemForm(
    modifier: Modifier,
    identityItemFormState: IdentityItemFormState,
    validationErrors: PersistentSet<IdentityValidationErrors>,
    extraFields: PersistentSet<ExtraField>,
    enabled: Boolean,
    onEvent: (IdentityContentEvent) -> Unit
) {
    val isGroupCollapsed = rememberSaveable(saver = isCollapsedSaver()) {
        mutableStateListOf(IdentitySectionType.ContactDetails, IdentitySectionType.WorkDetails)
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        TitleSection(
            modifier = Modifier
                .padding(horizontal = Spacing.medium)
                .roundedContainerNorm()
                .padding(
                    start = Spacing.medium,
                    top = Spacing.medium,
                    end = Spacing.extraSmall,
                    bottom = Spacing.medium
                ),
            value = identityItemFormState.title,
            requestFocus = true,
            onTitleRequiredError = validationErrors.contains(IdentityValidationErrors.BlankTitle),
            enabled = enabled,
            isRounded = true,
            onChange = { onEvent(OnFieldChange(FieldChange.Title(it))) }
        )
        CollapsibleSectionHeader(
            sectionTitle = stringResource(R.string.identity_section_personal_details),
            isCollapsed = isGroupCollapsed.contains(IdentitySectionType.PersonalDetails),
            onClick = {
                if (isGroupCollapsed.contains(IdentitySectionType.PersonalDetails)) {
                    isGroupCollapsed.remove(IdentitySectionType.PersonalDetails)
                } else {
                    isGroupCollapsed.add(IdentitySectionType.PersonalDetails)
                }
            }
        )
        AnimatedVisibility(visible = !isGroupCollapsed.contains(IdentitySectionType.PersonalDetails)) {
            PersonalDetails(
                modifier = Modifier.padding(horizontal = Spacing.medium),
                enabled = enabled,
                uiPersonalDetails = identityItemFormState.uiPersonalDetails,
                extraFields = extraFields.filterIsInstance<PersonalDetailsField>()
                    .toPersistentSet(),
                onEvent = onEvent
            )
        }
        CollapsibleSectionHeader(
            sectionTitle = stringResource(R.string.identity_section_address_details),
            isCollapsed = isGroupCollapsed.contains(IdentitySectionType.AddressDetails),
            onClick = {
                if (isGroupCollapsed.contains(IdentitySectionType.AddressDetails)) {
                    isGroupCollapsed.remove(IdentitySectionType.AddressDetails)
                } else {
                    isGroupCollapsed.add(IdentitySectionType.AddressDetails)
                }
            }
        )
        AnimatedVisibility(visible = !isGroupCollapsed.contains(IdentitySectionType.AddressDetails)) {
            AddressDetails(
                modifier = Modifier.padding(horizontal = Spacing.medium),
                enabled = enabled,
                uiAddressDetails = identityItemFormState.uiAddressDetails,
                extraFields = extraFields.filterIsInstance<AddressDetailsField>().toPersistentSet(),
                onEvent = onEvent
            )
        }
        CollapsibleSectionHeader(
            sectionTitle = stringResource(R.string.identity_section_contact_details),
            isCollapsed = isGroupCollapsed.contains(IdentitySectionType.ContactDetails),
            onClick = {
                if (isGroupCollapsed.contains(IdentitySectionType.ContactDetails)) {
                    isGroupCollapsed.remove(IdentitySectionType.ContactDetails)
                } else {
                    isGroupCollapsed.add(IdentitySectionType.ContactDetails)
                }
            }
        )
        AnimatedVisibility(visible = !isGroupCollapsed.contains(IdentitySectionType.ContactDetails)) {
            ContactDetails(
                modifier = Modifier.padding(horizontal = Spacing.medium),
                enabled = enabled,
                uiContactDetails = identityItemFormState.uiContactDetails,
                extraFields = extraFields.filterIsInstance<ContactDetailsField>().toPersistentSet(),
                onEvent = onEvent
            )
        }
        CollapsibleSectionHeader(
            sectionTitle = stringResource(R.string.identity_section_work_details),
            isCollapsed = isGroupCollapsed.contains(IdentitySectionType.WorkDetails),
            onClick = {
                if (isGroupCollapsed.contains(IdentitySectionType.WorkDetails)) {
                    isGroupCollapsed.remove(IdentitySectionType.WorkDetails)
                } else {
                    isGroupCollapsed.add(IdentitySectionType.WorkDetails)
                }
            }
        )
        AnimatedVisibility(visible = !isGroupCollapsed.contains(IdentitySectionType.WorkDetails)) {
            WorkDetails(
                modifier = Modifier.padding(horizontal = Spacing.medium),
                enabled = enabled,
                uiWorkDetails = identityItemFormState.uiWorkDetails,
                extraFields = extraFields.filterIsInstance<WorkDetailsField>().toPersistentSet(),
                onEvent = onEvent
            )
        }
    }
}

