package me.proton.android.pass.ui.launcher

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import me.proton.android.pass.ui.home.HomeScreen
import me.proton.android.pass.ui.home.HomeScreenNavigation
import me.proton.android.pass.ui.launcher.LauncherViewModel.State.*
import me.proton.core.compose.component.ProtonCenteredProgress

@ExperimentalMaterialApi
object LauncherScreen {

    @Composable
    fun View(
        onDrawerStateChanged: (Boolean) -> Unit = {},
        homeScreenNavigation: HomeScreenNavigation,
        viewModel: LauncherViewModel = hiltViewModel(),
    ) {
        val state by viewModel.state.collectAsState(Processing)

        when (state) {
            AccountNeeded -> viewModel.addAccount()
            PrimaryExist -> HomeScreen.View(
                onDrawerStateChanged = onDrawerStateChanged,
                onSignIn = { viewModel.signIn(it) },
                onSignOut = { viewModel.signOut(it) },
                onRemove = { viewModel.remove(it) },
                onSwitch = { viewModel.switch(it) },
                navigation = homeScreenNavigation,
            )
            Processing -> ProtonCenteredProgress(Modifier.fillMaxSize())
            StepNeeded -> ProtonCenteredProgress(Modifier.fillMaxSize())
        }
    }
}
