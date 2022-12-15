package me.proton.pass.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AliasOptions(
    val suffixes: List<AliasSuffix>,
    val mailboxes: List<AliasMailbox>
) : Parcelable

@Parcelize
data class AliasSuffix(
    val suffix: String,
    val signedSuffix: String,
    val isCustom: Boolean,
    val domain: String
) : Parcelable

@Parcelize
data class AliasMailbox(
    val id: Int,
    val email: String
) : Parcelable
