package me.proton.android.pass.ui.create.password

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import me.proton.android.pass.navigation.api.AppNavigator
import me.proton.android.pass.navigation.api.composable
import me.proton.android.pass.ui.navigation.AppNavItem
import me.proton.pass.presentation.create.password.CreatePassword

@OptIn(
    ExperimentalAnimationApi::class
)
fun NavGraphBuilder.createPasswordGraph(modifier: Modifier, nav: AppNavigator) {
    composable(AppNavItem.CreatePassword) {
        CreatePassword(
            modifier = modifier,
            onUpClick = { nav.onBackClick() }
        )
    }
}
