package proton.android.pass.featureitemcreate.impl.login

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import kotlinx.coroutines.flow.StateFlow
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

object EditLogin : NavItem(
    baseRoute = "login/edit",
    navArgIds = listOf(CommonNavArgId.ShareId, CommonNavArgId.ItemId)
) {
    fun createNavRoute(shareId: ShareId, itemId: ItemId) =
        "$baseRoute/${shareId.id}/${itemId.id}"
}

@OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalLifecycleComposeApi::class
)
fun NavGraphBuilder.updateLoginGraph(
    getPrimaryTotp: () -> StateFlow<String?>,
    onSuccess: (ShareId, ItemId) -> Unit,
    onUpClick: () -> Unit,
    onScanTotp: () -> Unit
) {
    composable(EditLogin) {
        val primaryTotp by getPrimaryTotp().collectAsStateWithLifecycle()
        UpdateLogin(
            draftAlias = null,
            primaryTotp = primaryTotp,
            onUpClick = onUpClick,
            onSuccess = onSuccess,
            onScanTotp = onScanTotp
        )
    }
}
