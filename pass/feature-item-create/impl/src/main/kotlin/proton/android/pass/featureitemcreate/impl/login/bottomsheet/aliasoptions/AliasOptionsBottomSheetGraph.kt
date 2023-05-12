package proton.android.pass.featureitemcreate.impl.login.bottomsheet.aliasoptions

import androidx.navigation.NavGraphBuilder
import proton.android.pass.featureitemcreate.impl.login.BaseLoginNavigation
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.ShowUpgradeNavArgId
import proton.android.pass.navigation.api.bottomSheet
import proton.pass.domain.ShareId

const val CLEAR_ALIAS_NAV_PARAMETER_KEY = "clearAlias"

object AliasOptionsBottomSheet : NavItem(
    baseRoute = "login/alias-options",
    navArgIds = listOf(CommonNavArgId.ShareId, ShowUpgradeNavArgId),
) {
    fun createNavRoute(shareId: ShareId, showUpgrade: Boolean) =
        "$baseRoute/${shareId.id}/$showUpgrade"
}

sealed interface AliasOptionsNavigation {
    object OnEditAlias : AliasOptionsNavigation
    object OnDeleteAlias : AliasOptionsNavigation
}

fun NavGraphBuilder.aliasOptionsBottomSheetGraph(
    onNavigate: (BaseLoginNavigation) -> Unit
) {
    bottomSheet(AliasOptionsBottomSheet) { navStack ->
        val shareId = navStack.arguments?.getString(CommonNavArgId.ShareId.key)
            ?: throw IllegalStateException("ShareId is required")
        val showUpgrade = navStack.arguments?.getBoolean(ShowUpgradeNavArgId.key)
            ?: throw IllegalStateException("ShowUpgrade is required")

        AliasOptionsBottomSheet(
            onNavigate = {
                when (it) {
                    is AliasOptionsNavigation.OnEditAlias -> {
                        onNavigate(BaseLoginNavigation.EditAlias(ShareId(shareId), showUpgrade))
                    }
                    is AliasOptionsNavigation.OnDeleteAlias -> {
                        onNavigate(BaseLoginNavigation.DeleteAlias)
                    }
                }
            }
        )
    }
}
