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

package proton.android.pass.autofill.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import me.proton.core.crypto.common.keystore.EncryptedString
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId

@Parcelize
sealed interface AutofillItem : Parcelable {

    fun shareId(): ShareId
    fun itemId(): ItemId
    fun type(): String
    fun shouldAssociate(): Boolean

    @Parcelize
    data class Login(
        val itemId: String,
        val shareId: String,
        val username: String,
        val password: EncryptedString?,
        val totp: EncryptedString?,
        val shouldLinkPackageName: Boolean,
        val email: String = username
    ) : AutofillItem {
        override fun shareId() = ShareId(shareId)
        override fun itemId() = ItemId(itemId)
        override fun type() = "AutofillItem.Login"
        override fun shouldAssociate(): Boolean = shouldLinkPackageName
    }

    @Parcelize
    data class CreditCard(
        val itemId: String,
        val shareId: String,
        val number: String,
        val cardHolder: String,
        val expiration: String,
        val cvv: EncryptedString?
    ) : AutofillItem {
        override fun shareId() = ShareId(shareId)
        override fun itemId() = ItemId(itemId)
        override fun type() = "AutofillItem.CreditCard"
        override fun shouldAssociate(): Boolean = false
    }

    @Parcelize
    data class Identity(
        val itemId: String,
        val shareId: String,
        val fullName: String?,
        val firstName: String?,
        val middleName: String?,
        val lastName: String?,
        val address: String?,
        val city: String?,
        val postalCode: String?,
        val phoneNumber: String?,
        val organization: String?,
        val country: String?
    ) : AutofillItem {
        override fun shareId() = ShareId(shareId)
        override fun itemId() = ItemId(itemId)
        override fun type() = "AutofillItem.Identity"
        override fun shouldAssociate(): Boolean = false
    }
}
