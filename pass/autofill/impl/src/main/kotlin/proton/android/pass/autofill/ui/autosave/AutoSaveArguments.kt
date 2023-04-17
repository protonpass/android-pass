package proton.android.pass.autofill.ui.autosave

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import proton.android.pass.autofill.entities.SaveInformation

data class AutoSaveArguments(
    val saveInformation: SaveInformation,
    val linkedAppInfo: LinkedAppInfo?,
    val title: String,
    val website: String?
)

@Parcelize
data class LinkedAppInfo(
    val packageName: String,
    val appName: String
) : Parcelable
