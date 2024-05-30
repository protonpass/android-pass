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
        val aliasEmail: String
    ) : ItemContents()

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
        val personalDetails: PersonalDetails,
        val addressDetails: AddressDetails,
        val contactDetails: ContactDetails,
        val workDetails: WorkDetails
    ) : ItemContents()

    @Serializable
    data class Unknown(
        override val title: String,
        override val note: String
    ) : ItemContents()

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
    val phoneNumber: String
) {
    companion object {
        val EMPTY = PersonalDetails("", "", "", "", "", "", "", "")
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
    val county: String
) {
    companion object {
        val EMPTY = AddressDetails("", "", "", "", "", "", "", "")
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
    val instagram: String
) {
    companion object {
        val EMPTY = ContactDetails("", "", "", "", "", "", "", "", "", "", "")
    }

}

@Serializable
data class WorkDetails(
    val company: String,
    val jobTitle: String,
    val personalWebsite: String,
    val workPhoneNumber: String,
    val workEmail: String
) {
    companion object {
        val EMPTY = WorkDetails("", "", "", "", "")
    }
}
