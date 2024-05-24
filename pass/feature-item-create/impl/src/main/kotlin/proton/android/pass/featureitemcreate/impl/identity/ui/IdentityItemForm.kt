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
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.isCollapsedSaver
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.TitleSection
import proton.android.pass.composecomponents.impl.labels.CollapsibleSectionHeader
import proton.android.pass.featureitemcreate.impl.R
import proton.android.pass.featureitemcreate.impl.identity.navigation.IdentityContentEvent
import proton.android.pass.featureitemcreate.impl.identity.presentation.IdentityItemFormState
import proton.android.pass.featureitemcreate.impl.identity.presentation.IdentityValidationErrors

@Composable
fun IdentityItemForm(
    modifier: Modifier,
    identityItemFormState: IdentityItemFormState,
    validationErrors: Set<IdentityValidationErrors>,
    enabled: Boolean,
    onEvent: (IdentityContentEvent) -> Unit
) {
    val isGroupCollapsed = rememberSaveable(saver = isCollapsedSaver()) {
        mutableStateListOf(Section.CONTACT_DETAILS, Section.WORK_DETAILS)
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
            onChange = { onEvent(IdentityContentEvent.OnTitleChange(it)) }
        )
        CollapsibleSectionHeader(
            sectionTitle = stringResource(R.string.identity_section_personal_details),
            isCollapsed = isGroupCollapsed.contains(Section.PERSONAL_DETAILS),
            onClick = {
                if (isGroupCollapsed.contains(Section.PERSONAL_DETAILS)) {
                    isGroupCollapsed.remove(Section.PERSONAL_DETAILS)
                } else {
                    isGroupCollapsed.add(Section.PERSONAL_DETAILS)
                }
            }
        )
        AnimatedVisibility(visible = !isGroupCollapsed.contains(Section.PERSONAL_DETAILS)) {
            PersonalDetails(
                modifier = Modifier.padding(horizontal = Spacing.medium),
                enabled = enabled,
                uiPersonalDetails = identityItemFormState.uiPersonalDetails,
                onEvent = onEvent
            )
        }
        CollapsibleSectionHeader(
            sectionTitle = stringResource(R.string.identity_section_address_details),
            isCollapsed = isGroupCollapsed.contains(Section.ADDRESS_DETAILS),
            onClick = {
                if (isGroupCollapsed.contains(Section.ADDRESS_DETAILS)) {
                    isGroupCollapsed.remove(Section.ADDRESS_DETAILS)
                } else {
                    isGroupCollapsed.add(Section.ADDRESS_DETAILS)
                }
            }
        )
        AnimatedVisibility(visible = !isGroupCollapsed.contains(Section.ADDRESS_DETAILS)) {
            AddressDetails(
                modifier = Modifier.padding(horizontal = Spacing.medium),
                enabled = enabled,
                uiAddressDetails = identityItemFormState.uiAddressDetails,
                onEvent = onEvent
            )
        }
        CollapsibleSectionHeader(
            sectionTitle = stringResource(R.string.identity_section_contact_details),
            isCollapsed = isGroupCollapsed.contains(Section.CONTACT_DETAILS),
            onClick = {
                if (isGroupCollapsed.contains(Section.CONTACT_DETAILS)) {
                    isGroupCollapsed.remove(Section.CONTACT_DETAILS)
                } else {
                    isGroupCollapsed.add(Section.CONTACT_DETAILS)
                }
            }
        )
        AnimatedVisibility(visible = !isGroupCollapsed.contains(Section.CONTACT_DETAILS)) {
            ContactDetails(
                modifier = Modifier.padding(horizontal = Spacing.medium),
                enabled = enabled,
                uiContactDetails = identityItemFormState.uiContactDetails,
                onEvent = onEvent
            )
        }
        CollapsibleSectionHeader(
            sectionTitle = stringResource(R.string.identity_section_work_details),
            isCollapsed = isGroupCollapsed.contains(Section.WORK_DETAILS),
            onClick = {
                if (isGroupCollapsed.contains(Section.WORK_DETAILS)) {
                    isGroupCollapsed.remove(Section.WORK_DETAILS)
                } else {
                    isGroupCollapsed.add(Section.WORK_DETAILS)
                }
            }
        )
        AnimatedVisibility(visible = !isGroupCollapsed.contains(Section.WORK_DETAILS)) {
            WorkDetails(
                modifier = Modifier.padding(horizontal = Spacing.medium),
                enabled = enabled,
                uiWorkDetails = identityItemFormState.uiWorkDetails,
                onEvent = onEvent
            )
        }
    }
}

enum class Section {
    PERSONAL_DETAILS,
    ADDRESS_DETAILS,
    CONTACT_DETAILS,
    WORK_DETAILS
}
