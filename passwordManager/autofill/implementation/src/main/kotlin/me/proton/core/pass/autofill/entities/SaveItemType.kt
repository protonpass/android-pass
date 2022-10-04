package me.proton.core.pass.autofill.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class SaveItemType : Parcelable {
    @Parcelize
    data class SingleValue(val contents: String) : SaveItemType()

    @Parcelize
    data class Login(
        val identity: String,
        val password: String
    ) : SaveItemType()
}
