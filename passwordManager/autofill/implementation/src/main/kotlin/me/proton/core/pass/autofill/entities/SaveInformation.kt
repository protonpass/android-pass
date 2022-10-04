package me.proton.core.pass.autofill.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SaveInformation(
    val appName: String,
    val packageName: String,
    val itemType: SaveItemType
) : Parcelable
