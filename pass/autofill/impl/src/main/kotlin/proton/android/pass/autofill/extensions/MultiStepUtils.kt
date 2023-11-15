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

package proton.android.pass.autofill.extensions

import android.os.Bundle
import android.os.Parcelable
import android.view.autofill.AutofillId
import kotlinx.parcelize.Parcelize

object MultiStepUtils {

    private const val USERNAME_FIELD_KEY = "username_field"
    private const val PASSWORD_FIELD_KEY = "password_field"

    @Parcelize
    data class SaveFieldInfo(
        val sessionId: Int,
        val fieldId: AutofillId
    ) : Parcelable

    fun Bundle.addUsernameToState(sessionId: Int, fieldId: AutofillId) {
        putByteArray(USERNAME_FIELD_KEY, marshalParcelable(SaveFieldInfo(sessionId, fieldId)))
    }

    fun Bundle.addPasswordToState(sessionId: Int, fieldId: AutofillId) {
        putByteArray(PASSWORD_FIELD_KEY, marshalParcelable(SaveFieldInfo(sessionId, fieldId)))
    }

    fun Bundle?.getUsernameFromState(): SaveFieldInfo? =
        this?.getByteArray(USERNAME_FIELD_KEY)?.deserializeParcelable()

    fun Bundle?.getPasswordFromState(): SaveFieldInfo? =
        this?.getByteArray(PASSWORD_FIELD_KEY)?.deserializeParcelable()
}
