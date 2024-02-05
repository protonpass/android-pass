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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import proton.android.pass.domain.entity.PackageInfo
import proton.android.pass.domain.serialization.EncryptedByteArraySerializer

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

@Serializable
sealed class HiddenState {
    abstract val encrypted: EncryptedString

    @Serializable
    data class Empty(override val encrypted: EncryptedString) : HiddenState()

    @Serializable
    data class Concealed(override val encrypted: EncryptedString) : HiddenState()

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
data class Passkey(
    val id: PasskeyId,
    val domain: String,
    val rpId: String,
    val rpName: String,
    val userName: String,
    val userDisplayName: String,
    val userId: ByteArray,
    @Serializable(with = EncryptedByteArraySerializer::class)
    val contents: EncryptedByteArray
)

@Serializable
sealed class ItemContents {
    abstract val title: String
    abstract val note: String

    @Serializable
    data class Login(
        override val title: String,
        override val note: String,
        val username: String,
        val password: HiddenState,
        val urls: List<String>,
        val packageInfoSet: Set<PackageInfo>,
        val primaryTotp: HiddenState,
        val customFields: List<CustomFieldContent>,
        val passkeys: List<Passkey>
    ) : ItemContents() {
        companion object {
            fun create(
                password: HiddenState,
                primaryTotp: HiddenState
            ) = Login(
                title = "",
                username = "",
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

    @Serializable
    data class Note(
        override val title: String,
        override val note: String
    ) : ItemContents()

    @Serializable
    data class Alias(
        override val title: String,
        override val note: String,
        val aliasEmail: String
    ) : ItemContents()

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
        val expirationDate: String,
    ) : ItemContents() {
        companion object {
            fun default(
                cvv: HiddenState,
                pin: HiddenState
            ) = CreditCard(
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

    @Serializable
    data class Unknown(
        override val title: String,
        override val note: String
    ) : ItemContents()

}
