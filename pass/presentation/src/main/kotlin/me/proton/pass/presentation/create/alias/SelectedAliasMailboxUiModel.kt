package me.proton.pass.presentation.create.alias

import android.os.Parcelable
import androidx.compose.runtime.Stable
import kotlinx.parcelize.Parcelize

@Parcelize
@Stable
data class SelectedAliasMailboxUiModel(
    val model: AliasMailboxUiModel,
    val selected: Boolean
) : Parcelable
