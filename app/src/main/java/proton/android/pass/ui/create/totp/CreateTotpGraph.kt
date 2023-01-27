package proton.android.pass.ui.create.totp

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.featurecreateitem.impl.totp.CreateManualTotp
import proton.android.pass.navigation.api.AppNavigator
import proton.android.pass.navigation.api.composable
import proton.android.pass.ui.navigation.AppNavItem


const val TOTP_NAV_PARAMETER_KEY = "totp"

@OptIn(
    ExperimentalAnimationApi::class
)
fun NavGraphBuilder.createTotpGraph(nav: AppNavigator) {
    composable(AppNavItem.CreateTotp) {
        CreateManualTotp(
            onAddManualTotp = { totp -> nav.navigateUpWithResult(TOTP_NAV_PARAMETER_KEY, totp) },
            onCloseManualTotp = { nav.onBackClick() }
        )
    }
}
