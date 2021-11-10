package me.proton.core.pass.autofill.service.entities

import android.os.Parcel
import android.os.Parcelable
import android.view.autofill.AutofillValue
import me.proton.core.pass.common_secret.SecretType

// TODO: use Parcelize here once https://github.com/JetBrains/kotlin/pull/4575 is merged
data class AssistField(
    val id: AutofillFieldId,
    val type: SecretType?,
    val value: AutofillValue?,
    val text: String?
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(AndroidAutofillFieldId::class.java.classLoader)!!,
        parcel.readParcelable(SecretType::class.java.classLoader),
        parcel.readParcelable(AutofillValue::class.java.classLoader),
        parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(id, flags)
        parcel.writeParcelable(type, flags)
        parcel.writeParcelable(value, flags)
        parcel.writeString(text)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AssistField> {
        override fun createFromParcel(parcel: Parcel): AssistField {
            return AssistField(parcel)
        }

        override fun newArray(size: Int): Array<AssistField?> {
            return arrayOfNulls(size)
        }
    }

}
