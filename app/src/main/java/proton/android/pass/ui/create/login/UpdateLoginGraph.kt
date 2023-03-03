package proton.android.pass.ui.create.login

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import proton.android.pass.featurecreateitem.impl.login.UpdateLogin
import proton.android.pass.featurecreateitem.impl.totp.CameraTotp
import proton.android.pass.featurecreateitem.impl.totp.TOTP_NAV_PARAMETER_KEY
import proton.android.pass.featurehome.impl.Home
import proton.android.pass.navigation.api.AppNavigator
import proton.android.pass.navigation.api.composable
import proton.android.pass.ui.navigation.EditLogin
import proton.android.pass.ui.navigation.ViewItem

@OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalLifecycleComposeApi::class
)
fun NavGraphBuilder.updateLoginGraph(nav: AppNavigator) {
    composable(EditLogin) {
        val primaryTotp by nav.navState<String>(TOTP_NAV_PARAMETER_KEY, null)
            .collectAsStateWithLifecycle()
        UpdateLogin(
            draftAlias = null,
            primaryTotp = primaryTotp,
            onUpClick = { nav.onBackClick() },
            onSuccess = { shareId, itemId ->
                nav.navigate(
                    destination = ViewItem,
                    route = ViewItem.createNavRoute(shareId, itemId),
                    backDestination = Home
                )
            },
            onScanTotp = { nav.navigate(CameraTotp) }
        )
    }
}
