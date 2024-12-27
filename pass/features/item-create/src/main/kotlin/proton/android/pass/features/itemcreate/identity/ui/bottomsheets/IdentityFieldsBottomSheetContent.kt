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
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.AddressCustomField
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.AddressDetailsField
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.Birthdate
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.ContactCustomField
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.ContactDetailsField
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.County
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.ExtraField
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.ExtraSectionCustomField
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.Facebook
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.FirstName
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.Floor
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.Gender
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.Instagram
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.LastName
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.Linkedin
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.MiddleName
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.PersonalCustomField
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.PersonalDetailsField
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.PersonalWebsite
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.Reddit
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.WorkCustomField
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.WorkDetailsField
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.WorkEmail
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.WorkPhoneNumber
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.Yahoo

@Composable
fun IdentityFieldsBottomSheetContent(
    modifier: Modifier = Modifier,
    fieldSet: ImmutableSet<ExtraField>,
    onFieldClick: (ExtraField) -> Unit
) {
    Column(modifier = modifier.bottomSheet(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.identity_bottomsheet_add_field),
            style = PassTheme.typography.body3Weak()
        )
        fieldSet.firstOrNull()?.let {
            val title = when (it) {
                is PersonalDetailsField ->
                    stringResource(R.string.identity_section_personal_details)
                is AddressDetailsField ->
                    stringResource(R.string.identity_section_address_details)
                is ContactDetailsField ->
                    stringResource(R.string.identity_section_contact_details)
                is WorkDetailsField ->
                    stringResource(R.string.identity_section_work_details)
                is ExtraSectionCustomField -> ""
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

internal fun extraOption(extraField: ExtraField, onClick: (ExtraField) -> Unit): BottomSheetItem =
    object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = {
                val titleResId = when (extraField) {
                    FirstName -> R.string.identity_add_field_first_name
                    MiddleName -> R.string.identity_add_field_middle_name
                    LastName -> R.string.identity_add_field_last_name
                    Birthdate -> R.string.identity_add_field_birthdate
                    Gender -> R.string.identity_add_field_gender
                    Floor -> R.string.identity_add_field_floor
                    County -> R.string.identity_add_field_county
                    Linkedin -> R.string.identity_add_field_linkedin
                    Reddit -> R.string.identity_add_field_reddit
                    Facebook -> R.string.identity_add_field_facebook
                    Yahoo -> R.string.identity_add_field_yahoo
                    Instagram -> R.string.identity_add_field_instagram
                    PersonalWebsite -> R.string.identity_add_field_personal_website
                    WorkPhoneNumber -> R.string.identity_add_field_work_phone_number
                    WorkEmail -> R.string.identity_add_field_work_email_address
                    is AddressCustomField,
                    is ContactCustomField,
                    is PersonalCustomField,
                    is WorkCustomField,
                    is ExtraSectionCustomField -> R.string.identity_add_field_custom_field
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
