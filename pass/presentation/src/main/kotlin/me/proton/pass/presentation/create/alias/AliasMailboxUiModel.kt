package me.proton.pass.presentation.create.alias

import android.os.Parcelable
import androidx.compose.runtime.Stable
import kotlinx.parcelize.Parcelize
import me.proton.pass.domain.AliasMailbox

@Parcelize
@Stable
data class AliasMailboxUiModel(
    val model: AliasMailbox,
    val selected: Boolean
) : Parcelable
