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

package proton.android.pass.features.itemcreate.creditcard

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.toEncryptedByteArray
import proton.android.pass.domain.CreditCardType
import proton.android.pass.domain.ItemContents
import proton.android.pass.features.itemcreate.common.UICustomFieldContent
import proton.android.pass.features.itemcreate.common.UIHiddenState
import proton.android.pass.features.itemcreate.common.UIHiddenState.Companion.from

@Parcelize
@Immutable
data class CreditCardItemFormState(
    val title: String,
    val note: String,
    val cardHolder: String,
    val type: CreditCardType,
    val number: String,
    val cvv: UIHiddenState,
    val pin: UIHiddenState,
    val expirationDate: String,
    val customFields: List<UICustomFieldContent>
) : Parcelable {

    constructor(itemContents: ItemContents.CreditCard) : this(
        title = itemContents.title,
        note = itemContents.note,
        cardHolder = itemContents.cardHolder,
        type = itemContents.type,
        number = itemContents.number,
        cvv = from(itemContents.cvv),
        pin = from(itemContents.pin),
        expirationDate = itemContents.expirationDate,
        customFields = itemContents.customFields.map(UICustomFieldContent.Companion::from)
    )

    fun validate(): Set<CreditCardValidationErrors> {
        val mutableSet = mutableSetOf<CreditCardValidationErrors>()
        if (title.isBlank()) mutableSet.add(CreditCardValidationErrors.BlankTitle)
        if (expirationDate.isNotBlank() && !expirationDateRegex.matches(expirationDate))
            mutableSet.add(CreditCardValidationErrors.InvalidExpirationDate)
        return mutableSet.toSet()
    }

    fun toItemContents(): ItemContents = ItemContents.CreditCard(
        title = title,
        note = note,
        cardHolder = cardHolder,
        number = number,
        cvv = cvv.toHiddenState(),
        expirationDate = expirationDate,
        pin = pin.toHiddenState(),
        type = type,
        customFields = customFields.map(UICustomFieldContent::toCustomFieldContent)
    )

    fun compare(other: CreditCardItemFormState, encryptionContext: EncryptionContext): Boolean = title == other.title &&
        note == other.note &&
        cardHolder == other.cardHolder &&
        number == other.number &&
        encryptionContext.decrypt(cvv.encrypted.toEncryptedByteArray())
            .contentEquals(encryptionContext.decrypt(other.cvv.encrypted.toEncryptedByteArray())) &&
        expirationDate == other.expirationDate &&
        encryptionContext.decrypt(pin.encrypted.toEncryptedByteArray())
            .contentEquals(encryptionContext.decrypt(other.pin.encrypted.toEncryptedByteArray())) &&
        type == other.type

    fun sanitise(): CreditCardItemFormState = copy(expirationDate = ExpirationDateProtoMapper.toProto(expirationDate))

    companion object {
        private val expirationDateRegex = Regex("^(0[1-9]|1[0-2])\\d{2}\$")

        fun default(encryptionContext: EncryptionContext) = CreditCardItemFormState(
            title = "",
            note = "",
            cardHolder = "",
            number = "",
            cvv = UIHiddenState.Empty(encryptionContext.encrypt("")),
            expirationDate = "",
            pin = UIHiddenState.Empty(encryptionContext.encrypt("")),
            type = CreditCardType.Other,
            customFields = emptyList()
        )
    }
}

sealed interface CreditCardValidationErrors {
    data object BlankTitle : CreditCardValidationErrors
    data object InvalidExpirationDate : CreditCardValidationErrors
}
