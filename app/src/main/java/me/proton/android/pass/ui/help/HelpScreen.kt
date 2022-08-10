package me.proton.android.pass.ui.help

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.proton.android.pass.ui.home.HomeScreenNavigation
import me.proton.core.pass.presentation.components.navigation.drawer.NavDrawerNavigation

@Composable
fun HelpScreen(
    modifier: Modifier = Modifier,
    navDrawerNavigation: NavDrawerNavigation,
    navigation: HomeScreenNavigation,
) {
    Text("Future help screen")
}
