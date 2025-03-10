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

package proton.android.pass.datamodels.api

import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.toEncryptedByteArray
import proton.android.pass.domain.CustomField
import proton.android.pass.domain.CustomFieldContent
import proton.android.pass.domain.HiddenState

fun CustomField.toContent(encryptionContext: EncryptionContext, isConcealed: Boolean): CustomFieldContent? =
    when (this) {
        CustomField.Unknown -> null
        is CustomField.Hidden -> {
            val hiddenFieldByteArray = encryptionContext.decrypt(value.toEncryptedByteArray())
            val hiddenState = if (hiddenFieldByteArray.isEmpty()) {
                HiddenState.Empty(value)
            } else {
                if (isConcealed) {
                    HiddenState.Concealed(value)
                } else {
                    HiddenState.Revealed(value, hiddenFieldByteArray.decodeToString())
                }
            }
            CustomFieldContent.Hidden(label = this.label, value = hiddenState)
        }

        is CustomField.Text -> {
            CustomFieldContent.Text(label = this.label, value = this.value)
        }

        is CustomField.Totp -> {
            val totpFieldByteArray = encryptionContext.decrypt(value.toEncryptedByteArray())
            val hiddenState = if (totpFieldByteArray.isEmpty()) {
                HiddenState.Empty(value)
            } else {
                HiddenState.Revealed(value, totpFieldByteArray.decodeToString())
            }
            CustomFieldContent.Totp(label = this.label, value = hiddenState)
        }
    }
