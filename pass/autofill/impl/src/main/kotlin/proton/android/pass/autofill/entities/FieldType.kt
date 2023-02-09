package proton.android.pass.autofill.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class FieldType : Parcelable {
    FullName,
    Username,
    Email,
    Password,
    Phone,
    Other,
    Unknown;

    companion object {
        fun from(value: String): FieldType = try {
            FieldType.valueOf(value)
        } catch (_: Exception) {
            Unknown
        }
    }
}
