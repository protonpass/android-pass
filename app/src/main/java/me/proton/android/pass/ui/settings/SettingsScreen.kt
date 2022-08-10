package me.proton.android.pass.ui.settings

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.proton.android.pass.ui.home.HomeScreenNavigation
import me.proton.core.pass.presentation.components.navigation.drawer.NavDrawerNavigation

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    navDrawerNavigation: NavDrawerNavigation,
    navigation: HomeScreenNavigation,
) {
    Text("Future settings screen")
}
