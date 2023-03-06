package proton.android.pass.featurehome.impl

import androidx.compose.runtime.Stable
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

@Stable
data class HomeScreenNavigation(
    val toEditLogin: (ShareId, ItemId) -> Unit,
    val toEditNote: (ShareId, ItemId) -> Unit,
    val toEditAlias: (ShareId, ItemId) -> Unit,
    val toItemDetail: (ShareId, ItemId) -> Unit,
    val toAuth: () -> Unit,
    val toProfile: () -> Unit,
    val toOnBoarding: () -> Unit
)
