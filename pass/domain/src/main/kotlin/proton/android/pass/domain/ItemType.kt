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

const val ITEM_TYPE_UNKNOWN = -1
const val ITEM_TYPE_LOGIN = 0
const val ITEM_TYPE_ALIAS = 1
const val ITEM_TYPE_NOTE = 2
const val ITEM_TYPE_PASSWORD = 3
const val ITEM_TYPE_CREDIT_CARD = 4

@Serializable
sealed interface CustomField {
    val label: String

    @Serializable
    data class Text(override val label: String, val value: String) : CustomField

    @Serializable
    data class Hidden(override val label: String, val value: EncryptedString) : CustomField

    @Serializable
    data class Totp(override val label: String, val value: EncryptedString) : CustomField

    object Unknown : CustomField {
        override val label: String = "UNKNOWN"
    }
}

@Serializable
sealed interface ItemType {

    @Serializable
    data class Login(
        val username: String,
        val password: EncryptedString,
        val websites: List<String>,
        val packageInfoSet: Set<PackageInfo>,
        val primaryTotp: EncryptedString,
        val customFields: List<CustomField>
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
        val expirationDate: String,
    ) : ItemType

    @Serializable
    object Password : ItemType

    @Serializable
    object Unknown : ItemType

    @Suppress("MagicNumber")
    fun toWeightedInt(): Int = when (this) {
        is Login -> ITEM_TYPE_LOGIN
        is Alias -> ITEM_TYPE_ALIAS
        is Note -> ITEM_TYPE_NOTE
        is Password -> ITEM_TYPE_PASSWORD
        is Unknown -> ITEM_TYPE_UNKNOWN
        is CreditCard -> ITEM_TYPE_CREDIT_CARD
    }

    companion object // Needed for being able to define static extension functions
}
