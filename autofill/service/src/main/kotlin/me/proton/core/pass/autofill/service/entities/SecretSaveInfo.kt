package me.proton.core.pass.autofill.service.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import me.proton.core.pass.common_secret.SecretType
import me.proton.core.pass.common_secret.SecretValue

@Parcelize
data class SecretSaveInfo(
    val name: String,
    val appPackageName: String,
    val secretType: SecretType,
    val secretValue: SecretValue
) : Parcelable
