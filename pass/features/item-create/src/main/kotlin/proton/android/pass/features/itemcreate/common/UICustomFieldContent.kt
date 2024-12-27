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
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.domain.CustomFieldContent
import proton.android.pass.features.itemcreate.bottomsheets.customfield.CustomFieldType
import java.util.UUID

@Immutable
@Parcelize
sealed interface UICustomFieldContent : Parcelable {
    val label: String

    @Immutable
    @Parcelize
    data class Text(override val label: String, val value: String) : UICustomFieldContent

    @Immutable
    @Parcelize
    data class Hidden(override val label: String, val value: UIHiddenState) : UICustomFieldContent

    @Immutable
    @Parcelize
    data class Totp(override val label: String, val value: UIHiddenState, val id: String) :
        UICustomFieldContent

    fun toCustomFieldContent() = when (this) {
        is Text -> CustomFieldContent.Text(label, value)
        is Hidden -> CustomFieldContent.Hidden(label, value.toHiddenState())
        is Totp -> CustomFieldContent.Totp(label, value.toHiddenState())
    }

    fun toCustomFieldType(): CustomFieldType = when (this) {
        is Text -> CustomFieldType.Text
        is Hidden -> CustomFieldType.Hidden
        is Totp -> CustomFieldType.Totp
    }

    fun compare(other: UICustomFieldContent, encryptionContext: EncryptionContext): Boolean = when (this) {
        is Text -> when (other) {
            is Text -> value == other.value
            else -> false
        }

        is Hidden -> when (other) {
            is Hidden -> value.compare(other.value, encryptionContext)
            else -> false
        }

        is Totp -> when (other) {
            is Totp -> value.compare(other.value, encryptionContext)
            else -> false
        }
    }

    companion object {
        fun from(state: CustomFieldContent) = when (state) {
            is CustomFieldContent.Text -> Text(state.label, state.value)
            is CustomFieldContent.Hidden -> Hidden(state.label, UIHiddenState.from(state.value))
            is CustomFieldContent.Totp -> Totp(state.label, UIHiddenState.from(state.value), generateUniqueID())
        }

        private fun generateUniqueID(): String = UUID.randomUUID().toString()
    }
}
