package me.proton.core.pass.common_secret

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class SecretValue : Parcelable {
    @Parcelize data class Single(val contents: String) : SecretValue()

    @Parcelize data class Login(
        val identity: String,
        val password: String
    ) : SecretValue()
}
