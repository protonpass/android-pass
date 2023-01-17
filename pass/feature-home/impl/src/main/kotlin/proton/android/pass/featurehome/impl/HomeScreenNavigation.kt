package proton.android.pass.featurehome.impl

import androidx.compose.runtime.Stable
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

@Stable
data class HomeScreenNavigation(
    val toCreateLogin: (ShareId) -> Unit,
    val toEditLogin: (ShareId, ItemId) -> Unit,
    val toCreateNote: (ShareId) -> Unit,
    val toEditNote: (ShareId, ItemId) -> Unit,
    val toCreateAlias: (ShareId) -> Unit,
    val toEditAlias: (ShareId, ItemId) -> Unit,
    val toItemDetail: (ShareId, ItemId) -> Unit,
    val toAuth: () -> Unit,
    val toOnBoarding: () -> Unit,
    val toCreatePassword: (ShareId) -> Unit
)
