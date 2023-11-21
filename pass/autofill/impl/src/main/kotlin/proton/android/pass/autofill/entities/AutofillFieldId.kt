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
import android.view.autofill.AutofillId
import kotlinx.parcelize.Parcelize

/** Used for testing purposes */
interface AutofillFieldId : Parcelable {
    fun value(): Int
}

/** Wrapper class holding an actual `AutofillId` */
@Parcelize
data class AndroidAutofillFieldId(val autofillId: AutofillId) : AutofillFieldId {
    override fun value(): Int {
        val method = autofillId.javaClass.methods.firstOrNull { it.name == "getViewId" }
        method?.isAccessible = true
        return method?.invoke(autofillId) as? Int ?: 0
    }
}

/**
 * Helper to do common casting to AndroidAutofillFieldId.
 *
 * **DO NOT** use this in unit tests.
 */
fun AutofillFieldId.asAndroid() = this as AndroidAutofillFieldId
