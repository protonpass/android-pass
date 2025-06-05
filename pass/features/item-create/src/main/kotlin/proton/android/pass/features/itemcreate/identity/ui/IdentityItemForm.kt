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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.toPersistentSet
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.isCollapsedSaver
import proton.android.pass.composecomponents.impl.attachments.AttachmentSection
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.composecomponents.impl.form.TitleSection
import proton.android.pass.composecomponents.impl.labels.CollapsibleSectionHeader
import proton.android.pass.composecomponents.impl.utils.passItemColors
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.features.itemcreate.R
import proton.android.pass.features.itemcreate.attachments.banner.AttachmentBanner
import proton.android.pass.features.itemcreate.common.CommonFieldValidationError
import proton.android.pass.features.itemcreate.common.customfields.AddSectionButton
import proton.android.pass.features.itemcreate.identity.navigation.IdentityContentEvent
import proton.android.pass.features.itemcreate.identity.navigation.IdentityContentEvent.OnAttachmentEvent
import proton.android.pass.features.itemcreate.identity.navigation.IdentityContentEvent.OnExtraSectionOptions
import proton.android.pass.features.itemcreate.identity.navigation.IdentityContentEvent.OnFieldChange
import proton.android.pass.features.itemcreate.identity.presentation.FieldChange
import proton.android.pass.features.itemcreate.identity.presentation.IdentityItemFormState
import proton.android.pass.features.itemcreate.identity.presentation.IdentityUiState
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.AddressDetailsField
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.ContactDetailsField
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.PersonalDetailsField
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.WorkDetailsField
import proton.android.pass.features.itemcreate.identity.ui.IdentitySectionType.AddressDetails
import proton.android.pass.features.itemcreate.identity.ui.IdentitySectionType.ContactDetails
import proton.android.pass.features.itemcreate.identity.ui.IdentitySectionType.ExtraSection
import proton.android.pass.features.itemcreate.identity.ui.IdentitySectionType.PersonalDetails
import proton.android.pass.features.itemcreate.identity.ui.IdentitySectionType.WorkDetails

@Composable
fun IdentityItemForm(
    modifier: Modifier,
    identityItemFormState: IdentityItemFormState,
    identityUiState: IdentityUiState,
    canUseAttachments: Boolean,
    onEvent: (IdentityContentEvent) -> Unit
) {
    val isGroupCollapsed = rememberSaveable(saver = isCollapsedSaver()) {
        mutableStateListOf(ContactDetails, WorkDetails)
    }
    LaunchedEffect(identityUiState.hasReceivedItem()) {
        if (identityUiState.hasReceivedItem()) {
            if (identityItemFormState.containsContactDetails()) {
                isGroupCollapsed.remove(ContactDetails)
            }
            if (identityItemFormState.containsWorkDetails()) {
                isGroupCollapsed.remove(WorkDetails)
            }
        }
    }
    val enabled = remember(identityUiState) { !identityUiState.getSubmitLoadingState().value() }
    val extraFields = remember(identityUiState) { identityUiState.getExtraFields() }
    val focusedField = remember(identityUiState) { identityUiState.getFocusedField() }
    val canUseCustomFields = remember(identityUiState) { identityUiState.getCanUseCustomFields() }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        AnimatedVisibility(
            modifier = Modifier.fillMaxWidth(),
            visible = identityUiState.showFileAttachmentsBanner()
        ) {
            AttachmentBanner(
                modifier = Modifier
                    .padding(horizontal = Spacing.medium)
                    .padding(vertical = Spacing.small)
            ) {
                onEvent(IdentityContentEvent.DismissAttachmentBanner)
            }
        }
        TitleSection(
            modifier = Modifier
                .padding(horizontal = Spacing.medium)
                .padding(vertical = Spacing.small)
                .roundedContainerNorm()
                .padding(
                    start = Spacing.medium,
                    top = Spacing.medium,
                    end = Spacing.extraSmall,
                    bottom = Spacing.medium
                ),
            value = identityItemFormState.title,
            requestFocus = true,
            onTitleRequiredError = identityUiState.getValidationErrors()
                .contains(CommonFieldValidationError.BlankTitle),
            enabled = enabled,
            isRounded = true,
            onChange = { onEvent(OnFieldChange(FieldChange.Title(it))) }
        )
        CollapsibleSectionHeader(
            sectionTitle = stringResource(R.string.identity_section_personal_details),
            isCollapsed = isGroupCollapsed.contains(PersonalDetails),
            onClick = {
                if (isGroupCollapsed.contains(PersonalDetails)) {
                    isGroupCollapsed.remove(PersonalDetails)
                } else {
                    isGroupCollapsed.add(PersonalDetails)
                }
            }
        )
        AnimatedVisibility(visible = !isGroupCollapsed.contains(PersonalDetails)) {
            PersonalDetails(
                modifier = Modifier.padding(horizontal = Spacing.medium),
                enabled = enabled,
                uiPersonalDetails = identityItemFormState.uiPersonalDetails,
                extraFields = extraFields.filterIsInstance<PersonalDetailsField>()
                    .toPersistentSet(),
                showAddPersonalDetailsButton = identityUiState.showAddPersonalDetailsButton(),
                focusedField = focusedField,
                onEvent = onEvent
            )
        }
        CollapsibleSectionHeader(
            sectionTitle = stringResource(R.string.identity_section_address_details),
            isCollapsed = isGroupCollapsed.contains(AddressDetails),
            onClick = {
                if (isGroupCollapsed.contains(AddressDetails)) {
                    isGroupCollapsed.remove(AddressDetails)
                } else {
                    isGroupCollapsed.add(AddressDetails)
                }
            }
        )
        AnimatedVisibility(visible = !isGroupCollapsed.contains(AddressDetails)) {
            AddressDetails(
                modifier = Modifier.padding(horizontal = Spacing.medium),
                enabled = enabled,
                uiAddressDetails = identityItemFormState.uiAddressDetails,
                extraFields = extraFields.filterIsInstance<AddressDetailsField>().toPersistentSet(),
                focusedField = focusedField,
                showAddAddressDetailsButton = identityUiState.showAddAddressDetailsButton(),
                onEvent = onEvent
            )
        }
        CollapsibleSectionHeader(
            sectionTitle = stringResource(R.string.identity_section_contact_details),
            isCollapsed = isGroupCollapsed.contains(ContactDetails),
            onClick = {
                if (isGroupCollapsed.contains(ContactDetails)) {
                    isGroupCollapsed.remove(ContactDetails)
                } else {
                    isGroupCollapsed.add(ContactDetails)
                }
            }
        )
        AnimatedVisibility(visible = !isGroupCollapsed.contains(ContactDetails)) {
            ContactDetails(
                modifier = Modifier.padding(horizontal = Spacing.medium),
                enabled = enabled,
                uiContactDetails = identityItemFormState.uiContactDetails,
                extraFields = extraFields.filterIsInstance<ContactDetailsField>().toPersistentSet(),
                focusedField = focusedField,
                showAddContactDetailsButton = identityUiState.showAddContactDetailsButton(),
                onEvent = onEvent
            )
        }
        CollapsibleSectionHeader(
            sectionTitle = stringResource(R.string.identity_section_work_details),
            isCollapsed = isGroupCollapsed.contains(WorkDetails),
            onClick = {
                if (isGroupCollapsed.contains(WorkDetails)) {
                    isGroupCollapsed.remove(WorkDetails)
                } else {
                    isGroupCollapsed.add(WorkDetails)
                }
            }
        )
        AnimatedVisibility(visible = !isGroupCollapsed.contains(WorkDetails)) {
            WorkDetails(
                modifier = Modifier.padding(horizontal = Spacing.medium),
                enabled = enabled,
                uiWorkDetails = identityItemFormState.uiWorkDetails,
                extraFields = extraFields.filterIsInstance<WorkDetailsField>().toPersistentSet(),
                focusedField = focusedField,
                showAddWorkDetailsButton = identityUiState.showAddWorkDetailsButton(),
                onEvent = onEvent
            )
        }

        identityItemFormState.uiExtraSections.forEachIndexed { sectionIndex, section ->
            CollapsibleSectionHeader(
                sectionTitle = section.title,
                isCollapsed = isGroupCollapsed.contains(ExtraSection(sectionIndex)),
                onClick = {
                    val section = ExtraSection(sectionIndex)
                    if (isGroupCollapsed.contains(section)) {
                        isGroupCollapsed.remove(section)
                    } else {
                        isGroupCollapsed.add(ExtraSection(sectionIndex))
                    }
                },
                onOptionsClick = {
                    onEvent(OnExtraSectionOptions(sectionIndex, section.title))
                }
            )
            AnimatedVisibility(visible = !isGroupCollapsed.contains(ExtraSection(sectionIndex))) {
                ExtraSection(
                    modifier = Modifier.padding(horizontal = Spacing.medium),
                    section = section,
                    enabled = enabled,
                    sectionIndex = sectionIndex,
                    focusedField = focusedField,
                    onEvent = onEvent
                )
            }
        }
        if (canUseCustomFields) {
            Column(
                modifier = Modifier.padding(
                    vertical = Spacing.small,
                    horizontal = Spacing.medium
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                PassDivider()
                AddSectionButton(
                    passItemColors = passItemColors(ItemCategory.Identity),
                    isEnabled = enabled,
                    onClick = { onEvent(IdentityContentEvent.OnAddExtraSection) }
                )
            }
        }

        if (identityUiState.showFileAttachments() && canUseAttachments) {
            AttachmentSection(
                modifier = Modifier
                    .padding(horizontal = Spacing.medium)
                    .padding(bottom = Spacing.small),
                attachmentsState = identityUiState.getAttachmentsState(),
                isDetail = false,
                itemColors = passItemColors(ItemCategory.Identity),
                onEvent = { onEvent(OnAttachmentEvent(it)) }
            )
        }
    }
}
