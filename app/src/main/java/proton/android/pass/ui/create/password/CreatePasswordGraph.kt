package proton.android.pass.ui.create.password

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.featurecreateitem.impl.password.CreatePassword
import proton.android.pass.navigation.api.AppNavigator
import proton.android.pass.navigation.api.composable
import proton.android.pass.ui.navigation.AppNavItem

@OptIn(
    ExperimentalAnimationApi::class
)
fun NavGraphBuilder.createPasswordGraph(nav: AppNavigator) {
    composable(AppNavItem.CreatePassword) {
        CreatePassword(
            onUpClick = { nav.onBackClick() }
        )
    }
}
