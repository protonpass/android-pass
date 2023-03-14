package proton.android.pass.featureitemcreate.impl.alias

import android.os.Parcelable
import androidx.compose.runtime.Stable
import kotlinx.parcelize.Parcelize

@Parcelize
@Stable
data class SelectedAliasMailboxUiModel(
    val model: AliasMailboxUiModel,
    val selected: Boolean
) : Parcelable
