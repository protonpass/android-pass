package me.proton.android.pass.ui.create.password

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import me.proton.android.pass.navigation.api.AppNavigator
import me.proton.android.pass.navigation.api.composable
import me.proton.android.pass.ui.navigation.AppNavItem
import me.proton.pass.presentation.create.password.CreatePassword

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
