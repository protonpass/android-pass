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

package proton.android.pass.composecomponents.impl.item.details.sections.identity

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.common.api.Option
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldType
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.item.details.PassItemDetailsUiEvent
import proton.android.pass.composecomponents.impl.item.details.rows.addItemDetailsFieldRow
import proton.android.pass.composecomponents.impl.item.details.sections.shared.PassItemDetailsSection
import proton.android.pass.composecomponents.impl.item.details.sections.shared.addCustomFieldRows
import proton.android.pass.composecomponents.impl.utils.PassItemColors
import proton.android.pass.domain.ItemDiffs
import proton.android.pass.domain.ItemSection
import proton.android.pass.domain.TotpState
import proton.android.pass.domain.WorkDetailsContent

@Composable
internal fun PassIdentityItemDetailsWorkSection(
    modifier: Modifier = Modifier,
    workDetailsContent: WorkDetailsContent,
    workDetailTotps: ImmutableMap<Pair<Option<Int>, Int>, TotpState>,
    itemColors: PassItemColors,
    itemDiffs: ItemDiffs.Identity,
    onEvent: (PassItemDetailsUiEvent) -> Unit
) = with(workDetailsContent) {
    val rows = mutableListOf<@Composable () -> Unit>()

    if (hasCompany) {
        rows.addItemDetailsFieldRow(
            titleResId = R.string.item_details_identity_section_work_company_title,
            section = company,
            field = ItemDetailsFieldType.PlainCopyable.Company(company),
            itemColors = itemColors,
            itemDiffType = itemDiffs.company,
            onEvent = onEvent
        )
    }

    if (hasJobTitle) {
        rows.addItemDetailsFieldRow(
            titleResId = R.string.item_details_identity_section_work_occupation_title,
            section = jobTitle,
            field = ItemDetailsFieldType.PlainCopyable.Occupation(jobTitle),
            itemColors = itemColors,
            itemDiffType = itemDiffs.jobTitle,
            onEvent = onEvent
        )
    }

    if (hasWorkPhoneNumber) {
        rows.addItemDetailsFieldRow(
            titleResId = R.string.item_details_identity_section_work_phone_number_title,
            section = workPhoneNumber,
            field = ItemDetailsFieldType.PlainCopyable.PhoneNumber(workPhoneNumber),
            itemColors = itemColors,
            itemDiffType = itemDiffs.workPhoneNumber,
            onEvent = onEvent
        )
    }

    if (hasWorkEmail) {
        rows.addItemDetailsFieldRow(
            titleResId = R.string.item_details_identity_section_work_email_title,
            section = workEmail,
            field = ItemDetailsFieldType.PlainCopyable.Email(workEmail),
            itemColors = itemColors,
            itemDiffType = itemDiffs.workEmail,
            onEvent = onEvent
        )
    }

    if (hasPersonalWebsite) {
        rows.addItemDetailsFieldRow(
            titleResId = R.string.item_details_identity_section_work_personal_website_title,
            section = personalWebsite,
            field = ItemDetailsFieldType.PlainCopyable.Website(personalWebsite),
            itemColors = itemColors,
            itemDiffType = itemDiffs.personalWebsite,
            onEvent = onEvent
        )
    }

    if (hasCustomFields) {
        rows.addCustomFieldRows(
            customFields = customFields,
            customFieldSection = ItemSection.Identity.Work,
            customFieldTotps = workDetailTotps,
            itemColors = itemColors,
            itemDiffs = itemDiffs,
            onEvent = onEvent
        )
    }

    PassItemDetailsSection(
        modifier = modifier,
        title = stringResource(id = R.string.item_details_identity_section_work_title),
        sections = rows.toPersistentList()
    )
}
