package proton.android.pass.featureitemcreate.impl.alias

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import proton.pass.domain.AliasOptions

@Parcelize
data class AliasOptionsUiModel(
    val suffixes: List<AliasSuffixUiModel>,
    val mailboxes: List<AliasMailboxUiModel>
) : Parcelable {
    constructor(aliasOptions: AliasOptions) : this(
        suffixes = aliasOptions.suffixes.map(::AliasSuffixUiModel),
        mailboxes = aliasOptions.mailboxes.map(::AliasMailboxUiModel)
    )
}
