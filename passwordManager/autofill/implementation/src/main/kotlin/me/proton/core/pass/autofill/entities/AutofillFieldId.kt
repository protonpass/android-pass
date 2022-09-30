package me.proton.core.pass.autofill.entities

import android.os.Parcel
import android.os.Parcelable
import android.view.autofill.AutofillId

/** Used for testing purposes */
interface AutofillFieldId : Parcelable

/** Wrapper class holding an actual `AutofillId` */
data class AndroidAutofillFieldId(val autofillId: AutofillId) : AutofillFieldId {
    constructor(parcel: Parcel) : this(parcel.readParcelable(AutofillId::class.java.classLoader)!!)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(autofillId, flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<AndroidAutofillFieldId> {
        override fun createFromParcel(parcel: Parcel): AndroidAutofillFieldId = AndroidAutofillFieldId(parcel)
        override fun newArray(size: Int): Array<AndroidAutofillFieldId?> = arrayOfNulls(size)
    }
}

/**
 * Helper to do common casting to AndroidAutofillFieldId.
 *
 * **DO NOT** use this in unit tests.
 */
fun AutofillFieldId.asAndroid() = this as AndroidAutofillFieldId
