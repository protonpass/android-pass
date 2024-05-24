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
import proton.android.pass.domain.AddressDetails
import proton.android.pass.domain.ContactDetails
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.PersonalDetails
import proton.android.pass.domain.WorkDetails

@Parcelize
@Immutable
data class IdentityItemFormState(
    val title: String,
    val uiPersonalDetails: UIPersonalDetails,
    val uiAddressDetails: UIAddressDetails,
    val uiContactDetails: UIContactDetails,
    val uiWorkDetails: UIWorkDetails
) : Parcelable {

    constructor(itemContents: ItemContents.Identity) : this(
        title = itemContents.title,
        uiPersonalDetails = UIPersonalDetails(itemContents.personalDetails),
        uiAddressDetails = UIAddressDetails(itemContents.addressDetails),
        uiContactDetails = UIContactDetails(itemContents.contactDetails),
        uiWorkDetails = UIWorkDetails(itemContents.workDetails)
    )

    fun validate(): Set<IdentityValidationErrors> {
        val mutableSet = mutableSetOf<IdentityValidationErrors>()
        if (title.isBlank()) mutableSet.add(IdentityValidationErrors.BlankTitle)
        return mutableSet.toSet()
    }

    fun toItemContents(): ItemContents = ItemContents.Identity(
        title = title,
        note = "",
        personalDetails = PersonalDetails(
            fullName = uiPersonalDetails.fullName,
            firstName = uiPersonalDetails.firstName,
            middleName = uiPersonalDetails.middleName,
            lastName = uiPersonalDetails.lastName,
            birthdate = uiPersonalDetails.birthdate,
            gender = uiPersonalDetails.gender,
            email = uiPersonalDetails.email,
            phoneNumber = uiPersonalDetails.phoneNumber
        ),
        addressDetails = AddressDetails(
            organization = uiAddressDetails.organization,
            streetAddress = uiAddressDetails.streetAddress,
            zipOrPostalCode = uiAddressDetails.zipOrPostalCode,
            city = uiAddressDetails.city,
            stateOrProvince = uiAddressDetails.stateOrProvince,
            countryOrRegion = uiAddressDetails.countryOrRegion,
            floor = uiAddressDetails.floor,
            county = uiAddressDetails.county

        ),
        contactDetails = ContactDetails(
            socialSecurityNumber = uiContactDetails.socialSecurityNumber,
            passportNumber = uiContactDetails.passportNumber,
            licenseNumber = uiContactDetails.licenseNumber,
            website = uiContactDetails.website,
            xHandle = uiContactDetails.xHandle,
            secondPhoneNumber = uiContactDetails.secondPhoneNumber,
            linkedin = uiContactDetails.linkedin,
            reddit = uiContactDetails.reddit,
            facebook = uiContactDetails.facebook,
            yahoo = uiContactDetails.yahoo,
            instagram = uiContactDetails.instagram
        ),
        workDetails = WorkDetails(
            company = uiWorkDetails.company,
            jobTitle = uiWorkDetails.jobTitle,
            personalWebsite = uiWorkDetails.personalWebsite,
            workPhoneNumber = uiWorkDetails.workPhoneNumber,
            workEmail = uiWorkDetails.workEmail
        )
    )

    companion object {
        val EMPTY = IdentityItemFormState(
            title = "",
            uiPersonalDetails = UIPersonalDetails.EMPTY,
            uiAddressDetails = UIAddressDetails.EMPTY,
            uiContactDetails = UIContactDetails.EMPTY,
            uiWorkDetails = UIWorkDetails.EMPTY
        )
    }
}

@Parcelize
@Immutable
data class UIPersonalDetails(
    val fullName: String,
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val birthdate: String,
    val gender: String,
    val email: String,
    val phoneNumber: String
) : Parcelable {

    constructor(personalDetails: PersonalDetails) : this(
        fullName = personalDetails.fullName,
        firstName = personalDetails.firstName,
        middleName = personalDetails.middleName,
        lastName = personalDetails.lastName,
        birthdate = personalDetails.birthdate,
        gender = personalDetails.gender,
        email = personalDetails.email,
        phoneNumber = personalDetails.phoneNumber
    )

    companion object {

        val EMPTY = UIPersonalDetails(
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
data class UIAddressDetails(
    val organization: String,
    val streetAddress: String,
    val zipOrPostalCode: String,
    val city: String,
    val stateOrProvince: String,
    val countryOrRegion: String,
    val floor: String,
    val county: String
) : Parcelable {

    constructor(addressDetails: AddressDetails) : this(
        organization = addressDetails.organization,
        streetAddress = addressDetails.streetAddress,
        zipOrPostalCode = addressDetails.zipOrPostalCode,
        city = addressDetails.city,
        stateOrProvince = addressDetails.stateOrProvince,
        countryOrRegion = addressDetails.countryOrRegion,
        floor = addressDetails.floor,
        county = addressDetails.county
    )

    companion object {

        val EMPTY = UIAddressDetails(
            organization = "",
            streetAddress = "",
            zipOrPostalCode = "",
            city = "",
            stateOrProvince = "",
            countryOrRegion = "",
            floor = "",
            county = ""
        )
    }
}

@Parcelize
@Immutable
data class UIContactDetails(
    val socialSecurityNumber: String,
    val passportNumber: String,
    val licenseNumber: String,
    val website: String,
    val xHandle: String,
    val secondPhoneNumber: String,
    val linkedin: String,
    val reddit: String,
    val facebook: String,
    val yahoo: String,
    val instagram: String
) : Parcelable {
    constructor(contactDetails: ContactDetails) : this(
        socialSecurityNumber = contactDetails.socialSecurityNumber,
        passportNumber = contactDetails.passportNumber,
        licenseNumber = contactDetails.licenseNumber,
        website = contactDetails.website,
        xHandle = contactDetails.xHandle,
        secondPhoneNumber = contactDetails.secondPhoneNumber,
        linkedin = contactDetails.linkedin,
        reddit = contactDetails.reddit,
        facebook = contactDetails.facebook,
        yahoo = contactDetails.yahoo,
        instagram = contactDetails.instagram
    )

    companion object {
        val EMPTY = UIContactDetails(
            socialSecurityNumber = "",
            passportNumber = "",
            licenseNumber = "",
            website = "",
            xHandle = "",
            secondPhoneNumber = "",
            linkedin = "",
            reddit = "",
            facebook = "",
            yahoo = "",
            instagram = ""
        )
    }

}

@Parcelize
@Immutable
data class UIWorkDetails(
    val company: String,
    val jobTitle: String,
    val personalWebsite: String,
    val workPhoneNumber: String,
    val workEmail: String
) : Parcelable {

    constructor(workDetails: WorkDetails) : this(
        company = workDetails.company,
        jobTitle = workDetails.jobTitle,
        personalWebsite = workDetails.personalWebsite,
        workPhoneNumber = workDetails.workPhoneNumber,
        workEmail = workDetails.workEmail
    )

    companion object {

        val EMPTY = UIWorkDetails(
            company = "",
            jobTitle = "",
            personalWebsite = "",
            workPhoneNumber = "",
            workEmail = ""
        )
    }
}

sealed interface IdentityValidationErrors {
    data object BlankTitle : IdentityValidationErrors
}
