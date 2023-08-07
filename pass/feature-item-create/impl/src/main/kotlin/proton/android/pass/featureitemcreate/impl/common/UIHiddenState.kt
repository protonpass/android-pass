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

package proton.android.pass.featureitemcreate.impl.common

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import me.proton.core.crypto.common.keystore.EncryptedString
import proton.pass.domain.HiddenState

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

    companion object {
        fun HiddenState.from() = when (this) {
            is HiddenState.Empty -> Empty(encrypted)
            is HiddenState.Concealed -> Concealed(encrypted)
            is HiddenState.Revealed -> Revealed(encrypted, clearText)
        }
    }
}
