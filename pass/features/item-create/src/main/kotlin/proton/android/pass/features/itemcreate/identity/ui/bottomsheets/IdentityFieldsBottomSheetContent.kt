/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.features.itemcreate.identity.ui.bottomsheets

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.body3Weak
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetTitle
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.features.itemcreate.R
import proton.android.pass.features.itemcreate.identity.presentation.IdentityField
import proton.android.pass.features.itemcreate.identity.presentation.section
import proton.android.pass.features.itemcreate.identity.ui.IdentitySectionType

@Composable
fun IdentityFieldsBottomSheetContent(
    modifier: Modifier = Modifier,
    fieldSet: ImmutableSet<IdentityField>,
    onFieldClick: (IdentityField) -> Unit
) {
    Column(modifier = modifier.bottomSheet(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.identity_bottomsheet_add_field),
            style = PassTheme.typography.body3Weak()
        )
        fieldSet.firstOrNull()?.let {
            val title = when (it.section()) {
                is IdentitySectionType.PersonalDetails ->
                    stringResource(R.string.identity_section_personal_details)
                is IdentitySectionType.AddressDetails ->
                    stringResource(R.string.identity_section_address_details)
                is IdentitySectionType.ContactDetails ->
                    stringResource(R.string.identity_section_contact_details)
                is IdentitySectionType.WorkDetails ->
                    stringResource(R.string.identity_section_work_details)
                is IdentitySectionType.ExtraSection -> ""
            }
            if (title.isNotBlank()) {
                BottomSheetTitle(
                    title = title
                )
            }
        }
        BottomSheetItemList(
            items = fieldSet.map { extraOption(it, onFieldClick) }.withDividers().toPersistentList()
        )
    }
}

internal fun extraOption(extraField: IdentityField, onClick: (IdentityField) -> Unit): BottomSheetItem =
    object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = {
                val titleResId = when (extraField) {
                    IdentityField.FirstName -> R.string.identity_add_field_first_name
                    IdentityField.MiddleName -> R.string.identity_add_field_middle_name
                    IdentityField.LastName -> R.string.identity_add_field_last_name
                    IdentityField.Birthdate -> R.string.identity_add_field_birthdate
                    IdentityField.Gender -> R.string.identity_add_field_gender
                    IdentityField.Floor -> R.string.identity_add_field_floor
                    IdentityField.County -> R.string.identity_add_field_county
                    IdentityField.Linkedin -> R.string.identity_add_field_linkedin
                    IdentityField.Reddit -> R.string.identity_add_field_reddit
                    IdentityField.Facebook -> R.string.identity_add_field_facebook
                    IdentityField.Yahoo -> R.string.identity_add_field_yahoo
                    IdentityField.Instagram -> R.string.identity_add_field_instagram
                    IdentityField.PersonalWebsite -> R.string.identity_add_field_personal_website
                    IdentityField.WorkPhoneNumber -> R.string.identity_add_field_work_phone_number
                    IdentityField.WorkEmail -> R.string.identity_add_field_work_email_address
                    is IdentityField.CustomField -> R.string.identity_add_field_custom_field
                    else -> throw IllegalStateException("Unknown extra field: $extraField")
                }
                BottomSheetItemTitle(
                    text = stringResource(titleResId),
                    color = PassTheme.colors.textNorm
                )
            }
        override val subtitle: (@Composable () -> Unit)?
            get() = null
        override val leftIcon: (@Composable () -> Unit)?
            get() = null
        override val endIcon: (@Composable () -> Unit)?
            get() = null
        override val onClick: () -> Unit = { onClick(extraField) }
        override val isDivider = false
    }
