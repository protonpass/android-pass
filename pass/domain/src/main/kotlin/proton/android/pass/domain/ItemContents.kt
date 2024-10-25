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

package proton.android.pass.domain

import androidx.compose.runtime.Stable
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.crypto.common.keystore.EncryptedString
import proton.android.pass.domain.entity.PackageInfo

@Serializable
sealed interface CustomFieldContent {
    val label: String

    @Serializable
    data class Text(override val label: String, val value: String) : CustomFieldContent

    @Serializable
    data class Hidden(override val label: String, val value: HiddenState) : CustomFieldContent

    @Serializable
    data class Totp(override val label: String, val value: HiddenState) : CustomFieldContent
}

@Stable
@Serializable
sealed class HiddenState {
    abstract val encrypted: EncryptedString

    @Stable
    @Serializable
    data class Empty(override val encrypted: EncryptedString) : HiddenState()

    @Stable
    @Serializable
    data class Concealed(override val encrypted: EncryptedString) : HiddenState()

    @Stable
    @Serializable
    data class Revealed(
        override val encrypted: EncryptedString,
        val clearText: String
    ) : HiddenState()
}

@Serializable
enum class CreditCardType {
    Other,
    Visa,
    MasterCard,
    AmericanExpress
}

@Serializable
@JvmInline
value class PasskeyId(val value: String)

@Serializable
data class PasskeyCreationData(
    val osName: String,
    val osVersion: String,
    val deviceName: String,
    val appVersion: String
)

@Serializable
data class Passkey(
    val id: PasskeyId,
    val domain: String,
    val rpId: String?,
    val rpName: String,
    val userName: String,
    val userDisplayName: String,
    val userId: ByteArray,
    val note: String,
    val createTime: Instant,
    val contents: ByteArray,
    val userHandle: ByteArray?,
    val credentialId: ByteArray,
    val creationData: PasskeyCreationData?
)

@Stable
@Serializable
sealed class ItemContents {

    abstract val title: String
    abstract val note: String

    @Stable
    @Serializable
    data class Login(
        override val title: String,
        override val note: String,
        val itemEmail: String,
        val itemUsername: String,
        val password: HiddenState,
        val urls: List<String>,
        val packageInfoSet: Set<PackageInfo>,
        val primaryTotp: HiddenState,
        val customFields: List<CustomFieldContent>,
        val passkeys: List<Passkey>
    ) : ItemContents() {

        val displayUsername: String = itemUsername.ifEmpty { itemEmail }

        val websiteUrl: String? = urls.firstOrNull()

        val packageName: String? = packageInfoSet
            .minByOrNull { packageInfo -> packageInfo.packageName.value }
            ?.packageName
            ?.value
        val hasPasskeys: Boolean = passkeys.isNotEmpty()
        val hasSinglePasskey: Boolean = passkeys.size == 1
        val hasPrimaryTotp: Boolean = primaryTotp !is HiddenState.Empty

        companion object {

            fun create(password: HiddenState, primaryTotp: HiddenState) = Login(
                title = "",
                itemEmail = "",
                itemUsername = "",
                password = password,
                urls = listOf(""),
                packageInfoSet = emptySet(),
                primaryTotp = primaryTotp,
                note = "",
                customFields = emptyList(),
                passkeys = emptyList()
            )
        }
    }

    @Stable
    @Serializable
    data class Note(
        override val title: String,
        override val note: String
    ) : ItemContents()

    @Stable
    @Serializable
    data class Alias(
        override val title: String,
        override val note: String,
        val aliasEmail: String,
        private val isDisabled: Boolean? = null
    ) : ItemContents() {

        val isEnabled: Boolean = isDisabled != true

    }

    @Stable
    @Serializable
    data class CreditCard(
        override val title: String,
        override val note: String,
        val cardHolder: String,
        @SerialName("CreditCardType")
        val type: CreditCardType,
        val number: String,
        val cvv: HiddenState,
        val pin: HiddenState,
        val expirationDate: String
    ) : ItemContents() {
        companion object {
            fun default(cvv: HiddenState, pin: HiddenState) = CreditCard(
                title = "",
                cardHolder = "",
                type = CreditCardType.Other,
                number = "",
                cvv = cvv,
                pin = pin,
                expirationDate = "",
                note = ""
            )
        }
    }

    @Stable
    @Serializable
    data class Identity(
        override val title: String,
        override val note: String,
        val personalDetailsContent: PersonalDetailsContent,
        val addressDetailsContent: AddressDetailsContent,
        val contactDetailsContent: ContactDetailsContent,
        val workDetailsContent: WorkDetailsContent,
        val extraSectionContentList: List<ExtraSectionContent>
    ) : ItemContents()

    @Serializable
    data class Unknown(
        override val title: String,
        override val note: String
    ) : ItemContents()

}

@Serializable
data class PersonalDetailsContent(
    val fullName: String,
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val birthdate: String,
    val gender: String,
    val email: String,
    val phoneNumber: String,
    val customFields: List<CustomFieldContent>
) {

    val hasFullName: Boolean by lazy { fullName.isNotBlank() }

    val hasFirstName: Boolean by lazy { firstName.isNotBlank() }

    val hasMiddleName: Boolean by lazy { middleName.isNotBlank() }

    val hasLastName: Boolean by lazy { lastName.isNotBlank() }

    val hasBirthdate: Boolean by lazy { birthdate.isNotBlank() }

    val hasGender: Boolean by lazy { gender.isNotBlank() }

    val hasEmail: Boolean by lazy { email.isNotBlank() }

    val hasPhoneNumber: Boolean by lazy { phoneNumber.isNotBlank() }

    val hasCustomFields: Boolean by lazy { customFields.isNotEmpty() }

    val hasPersonalDetails: Boolean by lazy {
        listOf(
            hasFullName,
            hasFirstName,
            hasMiddleName,
            hasLastName,
            hasBirthdate,
            hasGender,
            hasEmail,
            hasPhoneNumber,
            hasCustomFields
        ).any { hasBeenSet -> hasBeenSet }
    }

    companion object {

        val EMPTY = PersonalDetailsContent(
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

@Serializable
data class AddressDetailsContent(
    val organization: String,
    val streetAddress: String,
    val zipOrPostalCode: String,
    val city: String,
    val stateOrProvince: String,
    val countryOrRegion: String,
    val floor: String,
    val county: String,
    val customFields: List<CustomFieldContent>
) {

    val hasOrganization: Boolean by lazy { organization.isNotBlank() }

    val hasStreetAddress: Boolean by lazy { streetAddress.isNotBlank() }

    val hasZipOrPostalCode: Boolean by lazy { zipOrPostalCode.isNotBlank() }

    val hasCity: Boolean by lazy { city.isNotBlank() }

    val hasStateOrProvince: Boolean by lazy { stateOrProvince.isNotBlank() }

    val hasCountryOrRegion: Boolean by lazy { countryOrRegion.isNotBlank() }

    val hasFloor: Boolean by lazy { floor.isNotBlank() }

    val hasCounty: Boolean by lazy { county.isNotBlank() }

    val hasCustomFields: Boolean by lazy { customFields.isNotEmpty() }

    val hasAddressDetails: Boolean by lazy {
        listOf(
            hasOrganization,
            hasStreetAddress,
            hasZipOrPostalCode,
            hasCity,
            hasStateOrProvince,
            hasCountryOrRegion,
            hasFloor,
            hasCounty,
            hasCustomFields
        ).any { hasBeenSet -> hasBeenSet }
    }

    companion object {

        val EMPTY = AddressDetailsContent(
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

@Serializable
data class ContactDetailsContent(
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
    val customFields: List<CustomFieldContent>
) {
    val hasSocialSecurityNumber: Boolean by lazy { socialSecurityNumber.isNotBlank() }

    val hasPassportNumber: Boolean by lazy { passportNumber.isNotBlank() }

    val hasLicenseNumber: Boolean by lazy { licenseNumber.isNotBlank() }

    val hasWebsite: Boolean by lazy { website.isNotBlank() }

    val hasXHandle: Boolean by lazy { xHandle.isNotBlank() }

    val hasSecondPhoneNumber: Boolean by lazy { secondPhoneNumber.isNotBlank() }

    val hasLinkedin: Boolean by lazy { linkedin.isNotBlank() }

    val hasReddit: Boolean by lazy { reddit.isNotBlank() }

    val hasFacebook: Boolean by lazy { facebook.isNotBlank() }

    val hasYahoo: Boolean by lazy { yahoo.isNotBlank() }

    val hasInstagram: Boolean by lazy { instagram.isNotBlank() }

    val hasCustomFields: Boolean by lazy { customFields.isNotEmpty() }

    val hasContactDetails: Boolean by lazy {
        listOf(
            hasSocialSecurityNumber,
            hasPassportNumber,
            hasLicenseNumber,
            hasWebsite,
            hasXHandle,
            hasSecondPhoneNumber,
            hasLinkedin,
            hasReddit,
            hasFacebook,
            hasYahoo,
            hasInstagram,
            hasCustomFields
        ).any { hasBeenSet -> hasBeenSet }
    }

    companion object {
        val EMPTY = ContactDetailsContent(
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

@Serializable
data class WorkDetailsContent(
    val company: String,
    val jobTitle: String,
    val personalWebsite: String,
    val workPhoneNumber: String,
    val workEmail: String,
    val customFields: List<CustomFieldContent>
) {

    val hasCompany: Boolean by lazy { company.isNotBlank() }

    val hasJobTitle: Boolean by lazy { jobTitle.isNotBlank() }

    val hasPersonalWebsite: Boolean by lazy { personalWebsite.isNotBlank() }

    val hasWorkPhoneNumber: Boolean by lazy { workPhoneNumber.isNotBlank() }

    val hasWorkEmail: Boolean by lazy { workEmail.isNotBlank() }

    val hasCustomFields: Boolean by lazy { customFields.isNotEmpty() }

    val hasWorkDetails: Boolean by lazy {
        listOf(
            hasCompany,
            hasJobTitle,
            hasPersonalWebsite,
            hasWorkPhoneNumber,
            hasWorkEmail,
            hasCustomFields
        ).any { hasBeenSet -> hasBeenSet }
    }

    companion object {
        val EMPTY = WorkDetailsContent(
            company = "",
            jobTitle = "",
            personalWebsite = "",
            workPhoneNumber = "",
            workEmail = "",
            customFields = emptyList()
        )
    }
}

@Serializable
data class ExtraSectionContent(
    val title: String,
    val customFields: List<CustomFieldContent>
) {

    val hasCustomFields: Boolean by lazy { customFields.isNotEmpty() }

    companion object {
        val EMPTY = ExtraSectionContent(
            title = "",
            customFields = emptyList()
        )
    }
}
