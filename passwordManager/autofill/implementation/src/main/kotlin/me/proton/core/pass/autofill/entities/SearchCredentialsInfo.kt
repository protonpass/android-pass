package me.proton.core.pass.autofill.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SearchCredentialsInfo(
    val appPackageName: String,
    val appName: String,
    val assistFields: List<AssistField>
) : Parcelable
