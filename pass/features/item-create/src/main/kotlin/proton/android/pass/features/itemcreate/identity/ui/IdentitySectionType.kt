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

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import proton.android.pass.common.api.Option

private const val PERSONAL_DETAILS_INDEX = 0
private const val ADDRESS_DETAILS_INDEX = 1
private const val CONTACT_DETAILS_INDEX = 2
private const val WORK_DETAILS_INDEX = 3
private const val EXTRA_SECTION_INDEX = 4

sealed interface IdentitySectionType {
    @Parcelize
    data object PersonalDetails : IdentitySectionType, Parcelable

    @Parcelize
    data object AddressDetails : IdentitySectionType, Parcelable

    @Parcelize
    data object ContactDetails : IdentitySectionType, Parcelable

    @Parcelize
    data object WorkDetails : IdentitySectionType, Parcelable

    @Parcelize
    data class ExtraSection(val index: Int) : IdentitySectionType, Parcelable

    companion object {
        fun IdentitySectionType.toIndex(): Int = when (this) {
            PersonalDetails -> PERSONAL_DETAILS_INDEX
            AddressDetails -> ADDRESS_DETAILS_INDEX
            ContactDetails -> CONTACT_DETAILS_INDEX
            WorkDetails -> WORK_DETAILS_INDEX
            is ExtraSection -> EXTRA_SECTION_INDEX
        }

        fun from(index: Int, sectionIndex: Option<Int>): IdentitySectionType = when (index) {
            PERSONAL_DETAILS_INDEX -> PersonalDetails
            ADDRESS_DETAILS_INDEX -> AddressDetails
            CONTACT_DETAILS_INDEX -> ContactDetails
            WORK_DETAILS_INDEX -> WorkDetails
            EXTRA_SECTION_INDEX -> ExtraSection(sectionIndex.value() ?: 0)
            else -> throw IllegalStateException("Unknown index $index")
        }
    }
}
