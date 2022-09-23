package me.proton.android.pass.ui.launcher

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import me.proton.android.pass.ui.help.HelpScreen
import me.proton.android.pass.ui.home.HomeScreen
import me.proton.android.pass.ui.home.HomeScreenNavigation
import me.proton.android.pass.ui.settings.SettingsScreen
import me.proton.android.pass.ui.trash.TrashScreen
import me.proton.core.compose.component.ProtonCenteredProgress
import me.proton.core.domain.entity.UserId
import me.proton.core.pass.presentation.components.navigation.drawer.NavDrawerNavigation
import me.proton.core.pass.presentation.components.navigation.drawer.NavigationDrawerSection

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class)
@Composable
fun LauncherScreen(
    onDrawerStateChanged: (Boolean) -> Unit = {},
    homeScreenNavigation: HomeScreenNavigation,
    viewModel: LauncherViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState(LauncherViewModel.State.Processing)
    val section by viewModel.sectionStateFlow.collectAsState(viewModel.initialSection)

    val events = object : NavDrawerNavigation {
        override val selectedSection = section
        override val onDrawerStateChanged = onDrawerStateChanged
        override val onSignIn: (UserId?) -> Unit = { viewModel.signIn(it) }
        override val onSignOut: (UserId) -> Unit = { viewModel.signOut(it) }
        override val onRemove: (UserId?) -> Unit = { viewModel.remove(it) }
        override val onSwitch: (UserId) -> Unit = { viewModel.switch(it) }
        override val onSectionSelected: (NavigationDrawerSection) -> Unit =
            { viewModel.onDrawerSectionSelected(it) }
    }

    when (state) {
        LauncherViewModel.State.AccountNeeded -> viewModel.addAccount()
        LauncherViewModel.State.PrimaryExist -> when (section) {
            NavigationDrawerSection.Items -> HomeScreen(
                navDrawerNavigation = events,
                navigation = homeScreenNavigation
            )
            NavigationDrawerSection.Settings -> SettingsScreen(
                navDrawerNavigation = events,
                navigation = homeScreenNavigation
            )
            NavigationDrawerSection.Trash -> TrashScreen(
                navDrawerNavigation = events
            )
            NavigationDrawerSection.Help -> HelpScreen(
                navDrawerNavigation = events,
                navigation = homeScreenNavigation
            )
        }
        LauncherViewModel.State.Processing -> ProtonCenteredProgress(Modifier.fillMaxSize())
        LauncherViewModel.State.StepNeeded -> ProtonCenteredProgress(Modifier.fillMaxSize())
    }
}
