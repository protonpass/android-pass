/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.featureitemcreate.impl.identity.presentation

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize

@Parcelize
@Immutable
data class IdentityItemFormState(
    val title: String,
    val personalDetails: PersonalDetails,
    val addressDetails: AddressDetails,
    val contactDetails: ContactDetails,
    val workDetails: WorkDetails
) : Parcelable {
    companion object {
        val EMPTY = IdentityItemFormState(
            title = "",
            personalDetails = PersonalDetails.EMPTY,
            addressDetails = AddressDetails.EMPTY,
            contactDetails = ContactDetails.EMPTY,
            workDetails = WorkDetails.EMPTY
        )
    }
}

@Parcelize
@Immutable
data class PersonalDetails(
    val fullName: String,
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val birthdate: String,
    val gender: String,
    val email: String,
    val phoneNumber: String
) : Parcelable {
    companion object {
        val EMPTY = PersonalDetails(
            fullName = "",
            firstName = "",
            middleName = "",
            lastName = "",
            birthdate = "",
            gender = "",
            email = "",
            phoneNumber = ""
        )
    }
}

@Parcelize
@Immutable
data class AddressDetails(
    val organization: String,
    val streetAddress: String,
    val zipOrPostalCode: String,
    val city: String,
    val stateOrProvince: String,
    val countryOrRegion: String
) : Parcelable {
    companion object {
        val EMPTY = AddressDetails(
            organization = "",
            streetAddress = "",
            zipOrPostalCode = "",
            city = "",
            stateOrProvince = "",
            countryOrRegion = ""
        )
    }
}

@Parcelize
@Immutable
data class ContactDetails(
    val socialSecurityNumber: String,
    val passportNumber: String,
    val licenseNumber: String,
    val website: String,
    val xHandle: String
) : Parcelable {
    companion object {
        val EMPTY = ContactDetails(
            socialSecurityNumber = "",
            passportNumber = "",
            licenseNumber = "",
            website = "",
            xHandle = ""
        )
    }

}

@Parcelize
@Immutable
data class WorkDetails(
    val company: String,
    val jobTitle: String
) : Parcelable {
    companion object {
        val EMPTY = WorkDetails(
            company = "",
            jobTitle = ""
        )
    }
}
