package me.proton.core.pass.autofill.service.util

import android.os.Parcel
import android.os.Parcelable

fun Parcelable.toByteArray(): ByteArray {
    val parcelable = this
    return with(Parcel.obtain()) {
        parcelable.writeToParcel(this, 0)
        marshall().also { recycle() }
    }
}

inline fun <reified T : Parcelable, reified C : Parcelable.Creator<T>> C.fromByteArray(
    byteArray: ByteArray
): T {
    return with(Parcel.obtain()) {
        unmarshall(byteArray, 0, byteArray.size)
        setDataPosition(0)
        createFromParcel(this).also { recycle() }
    }
}
