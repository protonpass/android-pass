package me.proton.core.pass.common_secret

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class SecretType(val value: Int): Parcelable {
    Email(1),
    Username(2),
    Password(3),
    Login(4),
    Phone(5),
    FullName(6),
    Other(Int.MAX_VALUE);

    companion object {
        val map = values().associateBy { it.value }
    }
}
