package me.proton.core.pass.autofill.service.entities

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// TODO: use Parcelize here once https://github.com/JetBrains/kotlin/pull/4575 is merged
data class SearchCredentialsInfo(
    val appPackageName: String,
    val appName: String,
    val assistFields: List<AssistField>,
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createTypedArrayList(AssistField.CREATOR)!!) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(appPackageName)
        parcel.writeString(appName)
        parcel.writeTypedList(assistFields)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SearchCredentialsInfo> {
        override fun createFromParcel(parcel: Parcel): SearchCredentialsInfo {
            return SearchCredentialsInfo(parcel)
        }

        override fun newArray(size: Int): Array<SearchCredentialsInfo?> {
            return arrayOfNulls(size)
        }
    }

}
