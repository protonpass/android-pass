package me.proton.android.pass.ui.create.password

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavGraphBuilder
import me.proton.android.pass.ui.navigation.AppNavigator
import me.proton.android.pass.ui.navigation.NavItem
import me.proton.android.pass.ui.navigation.composable
import me.proton.pass.presentation.create.password.CreatePassword

@OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalComposeUiApi::class
)
fun NavGraphBuilder.createPasswordGraph(nav: AppNavigator) {
    composable(NavItem.CreatePassword) {
        CreatePassword(
            onUpClick = { nav.onBackClick() },
            onConfirm = {}
        )
    }
}
