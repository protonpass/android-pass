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

import kotlinx.serialization.Serializable
import me.proton.core.crypto.common.keystore.EncryptedString
import proton.android.pass.domain.entity.PackageInfo
import proton.android.pass.domain.items.ItemCategory

@Serializable
sealed interface CustomField {
    val label: String

    @Serializable
    data class Text(override val label: String, val value: String) : CustomField

    @Serializable
    data class Hidden(override val label: String, val value: EncryptedString) : CustomField

    @Serializable
    data class Totp(override val label: String, val value: EncryptedString) : CustomField

    data object Unknown : CustomField {
        override val label: String = "UNKNOWN"
    }
}

@Serializable
data class PersonalDetails(
    val fullName: String,
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val birthdate: String,
    val gender: String,
    val email: String,
    val phoneNumber: String,
    val customFields: List<CustomField>
) {

    companion object {
        val EMPTY = PersonalDetails(
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
data class AddressDetails(
    val organization: String,
    val streetAddress: String,
    val zipOrPostalCode: String,
    val city: String,
    val stateOrProvince: String,
    val countryOrRegion: String,
    val floor: String,
    val county: String,
    val customFields: List<CustomField>
) {
    companion object {
        val EMPTY = AddressDetails(
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
data class ContactDetails(
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
    val customFields: List<CustomField>
) {
    companion object {
        val EMPTY = ContactDetails(
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
data class WorkDetails(
    val company: String,
    val jobTitle: String,
    val personalWebsite: String,
    val workPhoneNumber: String,
    val workEmail: String,
    val customFields: List<CustomField>
) {
    companion object {
        val EMPTY = WorkDetails(
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
data class ExtraSection(
    val sectionName: String,
    val customFields: List<CustomField>
)

@Serializable
sealed interface ItemType {

    @Serializable
    data class Login(
        val itemEmail: String,
        val itemUsername: String,
        val password: EncryptedString,
        val websites: List<String>,
        val packageInfoSet: Set<PackageInfo>,
        val primaryTotp: EncryptedString,
        val customFields: List<CustomField>,
        val passkeys: List<Passkey>
    ) : ItemType

    @Serializable
    data class Note(val text: String) : ItemType

    @Serializable
    data class Alias(val aliasEmail: String) : ItemType

    @Serializable
    data class CreditCard(
        val cardHolder: String,
        val number: EncryptedString,
        val cvv: EncryptedString,
        val pin: EncryptedString,
        val creditCardType: CreditCardType,
        val expirationDate: String
    ) : ItemType

    @Serializable
    data class Identity(
        val personalDetails: PersonalDetails,
        val addressDetails: AddressDetails,
        val contactDetails: ContactDetails,
        val workDetails: WorkDetails,
        val extraSections: List<ExtraSection>
    ) : ItemType

    @Serializable
    data class Custom(
        val customFields: List<CustomField>,
        val extraSections: List<ExtraSection>
    ) : ItemType

    @Serializable
    data class WifiNetwork(
        val ssid: String,
        val password: EncryptedString,
        val customFields: List<CustomField>,
        val extraSections: List<ExtraSection>
    ) : ItemType

    @Serializable
    data class SSHKey(
        val publicKey: String,
        val privateKey: EncryptedString,
        val customFields: List<CustomField>,
        val extraSections: List<ExtraSection>
    ) : ItemType

    @Serializable
    data object Password : ItemType

    @Serializable
    data object Unknown : ItemType

    val category: ItemCategory
        get() = when (this) {
            is Alias -> ItemCategory.Alias
            is CreditCard -> ItemCategory.CreditCard
            is Login -> ItemCategory.Login
            is Note -> ItemCategory.Note
            Password -> ItemCategory.Password
            is Identity -> ItemCategory.Identity
            is SSHKey,
            is WifiNetwork,
            is Custom -> ItemCategory.Custom
            Unknown -> ItemCategory.Unknown
        }

    companion object // Needed for being able to define static extension functions
}
