package me.proton.pass.presentation.home

import androidx.compose.runtime.Stable
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ShareId

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
