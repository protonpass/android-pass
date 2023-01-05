package me.proton.android.pass.featurecreateitem.impl.alias

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import me.proton.pass.domain.AliasSuffix

@Parcelize
data class AliasSuffixUiModel(
    val suffix: String,
    val signedSuffix: String,
    val isCustom: Boolean,
    val domain: String
) : Parcelable {

    constructor(aliasSuffix: AliasSuffix) : this(
        suffix = aliasSuffix.suffix,
        signedSuffix = aliasSuffix.signedSuffix,
        isCustom = aliasSuffix.isCustom,
        domain = aliasSuffix.domain
    )

    fun toDomain(): AliasSuffix = AliasSuffix(
        suffix = suffix,
        signedSuffix = signedSuffix,
        isCustom = isCustom,
        domain = domain
    )
}
