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

import android.os.Parcel
import android.os.Parcelable

fun marshalParcelable(v: Parcelable): ByteArray {
    val p = Parcel.obtain()
    v.writeToParcel(p, 0)
    val asByteArray = p.marshall()
    p.recycle()
    return asByteArray
}

internal inline fun <reified T : Parcelable> ByteArray.deserializeParcelable(): T {
    val parcel = Parcel
        .obtain()
        .apply {
            unmarshall(this@deserializeParcelable, 0, size)
            setDataPosition(0)
        }

    return parcelableCreator<T>()
        .createFromParcel(parcel)
        .also {
            parcel.recycle()
        }
}

internal inline fun <reified T : Parcelable> parcelableCreator(): Parcelable.Creator<T> {
    val creator = T::class.java.getField("CREATOR").get(null)
    @Suppress("UNCHECKED_CAST")
    return creator as Parcelable.Creator<T>
}
