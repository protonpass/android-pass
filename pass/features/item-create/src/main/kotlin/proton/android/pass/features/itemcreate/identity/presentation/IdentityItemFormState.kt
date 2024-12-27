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

package proton.android.pass.features.itemcreate.identity.presentation

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import proton.android.pass.domain.AddressDetailsContent
import proton.android.pass.domain.ContactDetailsContent
import proton.android.pass.domain.ExtraSectionContent
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.PersonalDetailsContent
import proton.android.pass.domain.WorkDetailsContent
import proton.android.pass.features.itemcreate.common.UICustomFieldContent

@Parcelize
@Immutable
data class IdentityItemFormState(
    val title: String,
    val uiPersonalDetails: UIPersonalDetails,
    val uiAddressDetails: UIAddressDetails,
    val uiContactDetails: UIContactDetails,
    val uiWorkDetails: UIWorkDetails,
    val uiExtraSections: List<UIExtraSection>
) : Parcelable {

    constructor(itemContents: ItemContents.Identity) : this(
        title = itemContents.title,
        uiPersonalDetails = UIPersonalDetails(itemContents.personalDetailsContent),
        uiAddressDetails = UIAddressDetails(itemContents.addressDetailsContent),
        uiContactDetails = UIContactDetails(itemContents.contactDetailsContent),
        uiWorkDetails = UIWorkDetails(itemContents.workDetailsContent),
        uiExtraSections = itemContents.extraSectionContentList.map(::UIExtraSection)
    )

    fun validate(): Set<IdentityValidationErrors> {
        val mutableSet = mutableSetOf<IdentityValidationErrors>()
        if (title.isBlank()) mutableSet.add(IdentityValidationErrors.BlankTitle)
        return mutableSet.toSet()
    }

    fun toItemContents(): ItemContents = ItemContents.Identity(
        title = title,
        note = "",
        personalDetailsContent = PersonalDetailsContent(
            fullName = uiPersonalDetails.fullName,
            firstName = uiPersonalDetails.firstName,
            middleName = uiPersonalDetails.middleName,
            lastName = uiPersonalDetails.lastName,
            birthdate = uiPersonalDetails.birthdate,
            gender = uiPersonalDetails.gender,
            email = uiPersonalDetails.email,
            phoneNumber = uiPersonalDetails.phoneNumber,
            customFields = uiPersonalDetails.customFields.map(UICustomFieldContent::toCustomFieldContent)
        ),
        addressDetailsContent = AddressDetailsContent(
            organization = uiAddressDetails.organization,
            streetAddress = uiAddressDetails.streetAddress,
            zipOrPostalCode = uiAddressDetails.zipOrPostalCode,
            city = uiAddressDetails.city,
            stateOrProvince = uiAddressDetails.stateOrProvince,
            countryOrRegion = uiAddressDetails.countryOrRegion,
            floor = uiAddressDetails.floor,
            county = uiAddressDetails.county,
            customFields = uiAddressDetails.customFields.map(UICustomFieldContent::toCustomFieldContent)
        ),
        contactDetailsContent = ContactDetailsContent(
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
            instagram = uiContactDetails.instagram,
            customFields = uiContactDetails.customFields.map(UICustomFieldContent::toCustomFieldContent)
        ),
        workDetailsContent = WorkDetailsContent(
            company = uiWorkDetails.company,
            jobTitle = uiWorkDetails.jobTitle,
            personalWebsite = uiWorkDetails.personalWebsite,
            workPhoneNumber = uiWorkDetails.workPhoneNumber,
            workEmail = uiWorkDetails.workEmail,
            customFields = uiWorkDetails.customFields.map(UICustomFieldContent::toCustomFieldContent)
        ),
        extraSectionContentList = uiExtraSections.map {
            ExtraSectionContent(
                title = it.title,
                customFields = it.customFields.map(UICustomFieldContent::toCustomFieldContent)
            )
        }
    )

    fun containsContactDetails(): Boolean {
        if (uiContactDetails.customFields.isNotEmpty()) return true
        val list = listOf(
            uiContactDetails.socialSecurityNumber,
            uiContactDetails.passportNumber,
            uiContactDetails.licenseNumber,
            uiContactDetails.website,
            uiContactDetails.xHandle,
            uiContactDetails.secondPhoneNumber,
            uiContactDetails.linkedin,
            uiContactDetails.reddit,
            uiContactDetails.facebook,
            uiContactDetails.yahoo,
            uiContactDetails.instagram
        )
        return list.any { it.isNotBlank() }
    }

    fun containsWorkDetails(): Boolean {
        if (uiWorkDetails.customFields.isNotEmpty()) return true
        val list = listOf(
            uiWorkDetails.company,
            uiWorkDetails.jobTitle,
            uiWorkDetails.personalWebsite,
            uiWorkDetails.workPhoneNumber,
            uiWorkDetails.workEmail
        )
        return list.any { it.isNotBlank() }
    }

    companion object {
        val EMPTY = IdentityItemFormState(
            title = "",
            uiPersonalDetails = UIPersonalDetails.EMPTY,
            uiAddressDetails = UIAddressDetails.EMPTY,
            uiContactDetails = UIContactDetails.EMPTY,
            uiWorkDetails = UIWorkDetails.EMPTY,
            uiExtraSections = emptyList()
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
    val phoneNumber: String,
    val customFields: List<UICustomFieldContent>
) : Parcelable {

    constructor(personalDetailsContent: PersonalDetailsContent) : this(
        fullName = personalDetailsContent.fullName,
        firstName = personalDetailsContent.firstName,
        middleName = personalDetailsContent.middleName,
        lastName = personalDetailsContent.lastName,
        birthdate = personalDetailsContent.birthdate,
        gender = personalDetailsContent.gender,
        email = personalDetailsContent.email,
        phoneNumber = personalDetailsContent.phoneNumber,
        customFields = personalDetailsContent.customFields.map(UICustomFieldContent.Companion::from)
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
            phoneNumber = "",
            customFields = emptyList()
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
    val county: String,
    val customFields: List<UICustomFieldContent>
) : Parcelable {

    constructor(addressDetailsContent: AddressDetailsContent) : this(
        organization = addressDetailsContent.organization,
        streetAddress = addressDetailsContent.streetAddress,
        zipOrPostalCode = addressDetailsContent.zipOrPostalCode,
        city = addressDetailsContent.city,
        stateOrProvince = addressDetailsContent.stateOrProvince,
        countryOrRegion = addressDetailsContent.countryOrRegion,
        floor = addressDetailsContent.floor,
        county = addressDetailsContent.county,
        customFields = addressDetailsContent.customFields.map(UICustomFieldContent.Companion::from)
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
            county = "",
            customFields = emptyList()
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
    val instagram: String,
    val customFields: List<UICustomFieldContent>
) : Parcelable {
    constructor(contactDetailsContent: ContactDetailsContent) : this(
        socialSecurityNumber = contactDetailsContent.socialSecurityNumber,
        passportNumber = contactDetailsContent.passportNumber,
        licenseNumber = contactDetailsContent.licenseNumber,
        website = contactDetailsContent.website,
        xHandle = contactDetailsContent.xHandle,
        secondPhoneNumber = contactDetailsContent.secondPhoneNumber,
        linkedin = contactDetailsContent.linkedin,
        reddit = contactDetailsContent.reddit,
        facebook = contactDetailsContent.facebook,
        yahoo = contactDetailsContent.yahoo,
        instagram = contactDetailsContent.instagram,
        customFields = contactDetailsContent.customFields.map(UICustomFieldContent.Companion::from)
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
            instagram = "",
            customFields = emptyList()
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
    val workEmail: String,
    val customFields: List<UICustomFieldContent>
) : Parcelable {

    constructor(workDetailsContent: WorkDetailsContent) : this(
        company = workDetailsContent.company,
        jobTitle = workDetailsContent.jobTitle,
        personalWebsite = workDetailsContent.personalWebsite,
        workPhoneNumber = workDetailsContent.workPhoneNumber,
        workEmail = workDetailsContent.workEmail,
        customFields = workDetailsContent.customFields.map(UICustomFieldContent.Companion::from)
    )

    companion object {

        val EMPTY = UIWorkDetails(
            company = "",
            jobTitle = "",
            personalWebsite = "",
            workPhoneNumber = "",
            workEmail = "",
            customFields = emptyList()
        )
    }
}

@Parcelize
@Immutable
data class UIExtraSection(
    val title: String,
    val customFields: List<UICustomFieldContent>
) : Parcelable {

    constructor(extraSectionContent: ExtraSectionContent) : this(
        title = extraSectionContent.title,
        customFields = extraSectionContent.customFields.map(UICustomFieldContent.Companion::from)
    )
}

sealed interface IdentityValidationErrors {
    data object BlankTitle : IdentityValidationErrors
}
