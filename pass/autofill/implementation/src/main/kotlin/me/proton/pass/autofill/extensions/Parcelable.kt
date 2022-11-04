package me.proton.pass.autofill.extensions

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
