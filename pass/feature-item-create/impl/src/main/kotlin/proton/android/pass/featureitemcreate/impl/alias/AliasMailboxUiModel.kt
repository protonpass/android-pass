package proton.android.pass.featureitemcreate.impl.alias

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import proton.pass.domain.AliasMailbox

@Parcelize
data class AliasMailboxUiModel(
    val id: Int,
    val email: String
) : Parcelable {

    constructor(aliasMailbox: AliasMailbox) : this(
        id = aliasMailbox.id,
        email = aliasMailbox.email
    )

    fun toDomain(): AliasMailbox = AliasMailbox(id = id, email = email)
}
