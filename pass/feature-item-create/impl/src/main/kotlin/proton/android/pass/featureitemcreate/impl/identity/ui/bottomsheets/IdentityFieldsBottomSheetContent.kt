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

package proton.android.pass.featureitemcreate.impl.identity.ui.bottomsheets

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
import proton.android.pass.featureitemcreate.impl.R
import proton.android.pass.featureitemcreate.impl.identity.presentation.bottomsheets.AddressDetailsField
import proton.android.pass.featureitemcreate.impl.identity.presentation.bottomsheets.ContactDetailsField
import proton.android.pass.featureitemcreate.impl.identity.presentation.bottomsheets.ExtraField
import proton.android.pass.featureitemcreate.impl.identity.presentation.bottomsheets.PersonalDetailsField
import proton.android.pass.featureitemcreate.impl.identity.presentation.bottomsheets.WorkDetailsField

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
            val titleId = when (it) {
                is PersonalDetailsField -> R.string.identity_section_personal_details
                is AddressDetailsField -> R.string.identity_section_address_details
                is ContactDetailsField -> R.string.identity_section_contact_details
                is WorkDetailsField -> R.string.identity_section_work_details
            }
            BottomSheetTitle(
                title = stringResource(titleId)
            )
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
                    PersonalDetailsField.FirstName -> R.string.identity_add_field_first_name
                    PersonalDetailsField.MiddleName -> R.string.identity_add_field_middle_name
                    PersonalDetailsField.LastName -> R.string.identity_add_field_last_name
                    PersonalDetailsField.Birthdate -> R.string.identity_add_field_birthdate
                    PersonalDetailsField.Gender -> R.string.identity_add_field_gender
                    AddressDetailsField.Floor -> R.string.identity_add_field_floor
                    AddressDetailsField.County -> R.string.identity_add_field_county
                    ContactDetailsField.Linkedin -> R.string.identity_add_field_linkedin
                    ContactDetailsField.Reddit -> R.string.identity_add_field_reddit
                    ContactDetailsField.Facebook -> R.string.identity_add_field_facebook
                    ContactDetailsField.Yahoo -> R.string.identity_add_field_yahoo
                    ContactDetailsField.Instagram -> R.string.identity_add_field_instagram
                    WorkDetailsField.PersonalWebsite -> R.string.identity_add_field_personal_website
                    WorkDetailsField.WorkPhoneNumber -> R.string.identity_add_field_work_phone_number
                    WorkDetailsField.WorkEmail -> R.string.identity_add_field_work_email_address
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
