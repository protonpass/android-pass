package me.proton.android.pass.ui.create.login

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavGraphBuilder
import me.proton.android.pass.ui.navigation.AppNavigator
import me.proton.android.pass.ui.navigation.NavItem
import me.proton.android.pass.ui.navigation.composable
import me.proton.pass.presentation.create.login.CreateLogin

@OptIn(
    ExperimentalAnimationApi::class, ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class
)
fun NavGraphBuilder.createLoginGraph(nav: AppNavigator) {
    composable(NavItem.CreateLogin) {
        CreateLogin(
            onUpClick = { nav.onBackClick() },
            onSuccess = { nav.onBackClick() }
        )
    }
}
