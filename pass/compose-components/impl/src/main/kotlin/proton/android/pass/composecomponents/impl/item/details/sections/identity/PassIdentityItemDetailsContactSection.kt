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
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldType
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.item.details.PassItemDetailsUiEvent
import proton.android.pass.composecomponents.impl.item.details.rows.PassItemDetailsHiddenFieldRow
import proton.android.pass.composecomponents.impl.item.details.rows.addItemDetailsFieldRow
import proton.android.pass.composecomponents.impl.item.details.sections.shared.PassItemDetailsSection
import proton.android.pass.composecomponents.impl.item.details.sections.shared.addCustomFieldRows
import proton.android.pass.composecomponents.impl.utils.PassItemColors
import proton.android.pass.domain.ContactDetailsContent
import proton.android.pass.domain.ItemDiffs
import proton.android.pass.domain.ItemSection

private const val HIDDEN_SSN_TEXT_LENGTH = 9

@Composable
internal fun PassIdentityItemDetailsContactSection(
    modifier: Modifier = Modifier,
    contactDetailsContent: ContactDetailsContent,
    itemColors: PassItemColors,
    itemDiffs: ItemDiffs.Identity,
    onEvent: (PassItemDetailsUiEvent) -> Unit
) = with(contactDetailsContent) {
    val rows = mutableListOf<@Composable () -> Unit>()

    if (hasSocialSecurityNumber) {
        rows.add {
            PassItemDetailsHiddenFieldRow(
                title = stringResource(
                    id = R.string.item_details_identity_section_contact_social_security_number_title
                ),
                hiddenState = socialSecurityNumber,
                hiddenTextLength = HIDDEN_SSN_TEXT_LENGTH,
                itemColors = itemColors,
                itemDiffType = itemDiffs.socialSecurityNumber,
                icon = null,
                onClick = {
                    PassItemDetailsUiEvent.OnFieldClick(
                        field = ItemDetailsFieldType.HiddenCopyable.SocialSecurityNumber(socialSecurityNumber)
                    ).also(onEvent)
                },
                onToggle = { isVisible ->
                    PassItemDetailsUiEvent.OnHiddenFieldToggle(
                        isVisible = isVisible,
                        hiddenState = socialSecurityNumber,
                        fieldType = ItemDetailsFieldType.HiddenCopyable.SocialSecurityNumber(socialSecurityNumber),
                        fieldSection = ItemSection.Identity.SocialSecurityNumber
                    ).also(onEvent)
                }
            )
        }
    }

    if (hasPassportNumber) {
        rows.addItemDetailsFieldRow(
            titleResId = R.string.item_details_identity_section_contact_passport_number_title,
            section = passportNumber,
            field = ItemDetailsFieldType.PlainCopyable.PassportNumber(passportNumber),
            itemColors = itemColors,
            itemDiffType = itemDiffs.passportNumber,
            onEvent = onEvent
        )
    }

    if (hasLicenseNumber) {
        rows.addItemDetailsFieldRow(
            titleResId = R.string.item_details_identity_section_contact_license_number_title,
            section = licenseNumber,
            field = ItemDetailsFieldType.PlainCopyable.LicenseNumber(licenseNumber),
            itemColors = itemColors,
            itemDiffType = itemDiffs.licenseNumber,
            onEvent = onEvent
        )
    }

    if (hasWebsite) {
        rows.addItemDetailsFieldRow(
            titleResId = R.string.item_details_identity_section_contact_website_title,
            section = website,
            field = ItemDetailsFieldType.PlainCopyable.Website(website),
            itemColors = itemColors,
            itemDiffType = itemDiffs.website,
            onEvent = onEvent
        )
    }

    if (hasSecondPhoneNumber) {
        rows.addItemDetailsFieldRow(
            titleResId = R.string.item_details_identity_section_contact_secondary_phone_number_title,
            section = secondPhoneNumber,
            field = ItemDetailsFieldType.PlainCopyable.PhoneNumber(secondPhoneNumber),
            itemColors = itemColors,
            itemDiffType = itemDiffs.secondPhoneNumber,
            onEvent = onEvent
        )
    }

    if (hasLinkedin) {
        rows.addItemDetailsFieldRow(
            titleResId = R.string.item_details_identity_section_contact_linkedin_title,
            section = linkedin,
            field = ItemDetailsFieldType.PlainCopyable.LinkedIn(linkedin),
            itemColors = itemColors,
            itemDiffType = itemDiffs.linkedin,
            onEvent = onEvent
        )
    }

    if (hasXHandle) {
        rows.addItemDetailsFieldRow(
            titleResId = R.string.item_details_identity_section_contact_x_handle_title,
            section = xHandle,
            field = ItemDetailsFieldType.PlainCopyable.XHandle(xHandle),
            itemColors = itemColors,
            itemDiffType = itemDiffs.xHandle,
            onEvent = onEvent
        )
    }

    if (hasInstagram) {
        rows.addItemDetailsFieldRow(
            titleResId = R.string.item_details_identity_section_contact_instagram_title,
            section = instagram,
            field = ItemDetailsFieldType.PlainCopyable.Instagram(instagram),
            itemColors = itemColors,
            itemDiffType = itemDiffs.instagram,
            onEvent = onEvent
        )
    }

    if (hasFacebook) {
        rows.addItemDetailsFieldRow(
            titleResId = R.string.item_details_identity_section_contact_facebook_title,
            section = facebook,
            field = ItemDetailsFieldType.PlainCopyable.Facebook(facebook),
            itemColors = itemColors,
            itemDiffType = itemDiffs.facebook,
            onEvent = onEvent
        )
    }

    if (hasReddit) {
        rows.addItemDetailsFieldRow(
            titleResId = R.string.item_details_identity_section_contact_reddit_title,
            section = reddit,
            field = ItemDetailsFieldType.PlainCopyable.Reddit(reddit),
            itemColors = itemColors,
            itemDiffType = itemDiffs.reddit,
            onEvent = onEvent
        )
    }

    if (hasYahoo) {
        rows.addItemDetailsFieldRow(
            titleResId = R.string.item_details_identity_section_contact_yahoo_title,
            section = yahoo,
            field = ItemDetailsFieldType.PlainCopyable.Yahoo(yahoo),
            itemColors = itemColors,
            itemDiffType = itemDiffs.yahoo,
            onEvent = onEvent
        )
    }

    if (hasCustomFields) {
        rows.addCustomFieldRows(
            customFields = customFields,
            customFieldSection = ItemSection.Identity.Contact,
            customFieldTotps = persistentMapOf(),
            itemColors = itemColors,
            itemDiffs = itemDiffs,
            onEvent = onEvent
        )
    }

    PassItemDetailsSection(
        modifier = modifier,
        title = stringResource(id = R.string.item_details_identity_section_contact_title),
        sections = rows.toPersistentList()
    )
}
