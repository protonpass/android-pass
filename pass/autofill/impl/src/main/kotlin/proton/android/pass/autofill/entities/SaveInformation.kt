package proton.android.pass.autofill.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SaveInformation(
    val itemType: SaveItemType
) : Parcelable

fun SaveInformation.usernamePassword(): UsernamePassword = when (itemType) {
    is SaveItemType.Login -> UsernamePassword(itemType.identity, itemType.password)
    is SaveItemType.SingleValue -> UsernamePassword(itemType.contents, itemType.contents)
}

data class UsernamePassword(
    val username: String,
    val password: String
)
