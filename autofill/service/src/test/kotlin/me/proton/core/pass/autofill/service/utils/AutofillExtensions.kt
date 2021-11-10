package me.proton.core.pass.autofill.service.utils

import android.os.Parcel
import android.os.Parcelable
import me.proton.core.pass.autofill.service.entities.AutofillFieldId
import kotlin.random.Random

fun newAutofillFieldId() = FakeAutofillFieldId()

/** Used for testing purposes */
class FakeAutofillFieldId(val id: Int = Random.nextInt()): AutofillFieldId {
    constructor(parcel: Parcel): this(parcel.readInt())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FakeAutofillFieldId> {
        override fun createFromParcel(parcel: Parcel): FakeAutofillFieldId {
            return FakeAutofillFieldId(parcel)
        }

        override fun newArray(size: Int): Array<FakeAutofillFieldId?> {
            return arrayOfNulls(size)
        }
    }

}
