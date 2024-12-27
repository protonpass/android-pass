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

package proton.android.pass.features.itemcreate.common

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import me.proton.core.crypto.common.keystore.EncryptedString
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.toEncryptedByteArray
import proton.android.pass.domain.HiddenState

@Parcelize
sealed interface UIHiddenState : Parcelable {
    val encrypted: EncryptedString

    @Immutable
    @Parcelize
    data class Empty(override val encrypted: EncryptedString) : UIHiddenState, Parcelable

    @Immutable
    @Parcelize
    data class Concealed(override val encrypted: EncryptedString) : UIHiddenState, Parcelable

    @Immutable
    @Parcelize
    data class Revealed(
        override val encrypted: EncryptedString,
        val clearText: String
    ) : UIHiddenState, Parcelable

    fun toHiddenState() = when (this) {
        is Empty -> HiddenState.Empty(encrypted)
        is Concealed -> HiddenState.Concealed(encrypted)
        is Revealed -> HiddenState.Revealed(encrypted, clearText)
    }

    fun compare(other: UIHiddenState, encryptionContext: EncryptionContext): Boolean = when (this) {
        is Empty -> when (other) {
            is Empty -> true
            else -> false
        }

        is Concealed -> when (other) {
            is Concealed -> encryptionContext.decrypt(encrypted.toEncryptedByteArray())
                .contentEquals(encryptionContext.decrypt(other.encrypted.toEncryptedByteArray()))

            else -> false
        }

        is Revealed -> when (other) {
            is Revealed -> encryptionContext.decrypt(encrypted.toEncryptedByteArray())
                .contentEquals(encryptionContext.decrypt(other.encrypted.toEncryptedByteArray()))

            else -> false
        }
    }

    companion object {
        fun from(state: HiddenState) = when (state) {
            is HiddenState.Empty -> Empty(state.encrypted)
            is HiddenState.Concealed -> Concealed(state.encrypted)
            is HiddenState.Revealed -> Revealed(state.encrypted, state.clearText)
        }
    }
}
