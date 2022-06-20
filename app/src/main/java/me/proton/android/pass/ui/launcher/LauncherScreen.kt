package me.proton.android.pass.ui.launcher

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import me.proton.android.pass.ui.home.HomeScreen
import me.proton.android.pass.ui.launcher.LauncherViewModel.State.AccountNeeded
import me.proton.android.pass.ui.launcher.LauncherViewModel.State.PrimaryExist
import me.proton.android.pass.ui.launcher.LauncherViewModel.State.Processing
import me.proton.android.pass.ui.launcher.LauncherViewModel.State.StepNeeded
import me.proton.core.compose.component.ProtonCenteredProgress

@ExperimentalMaterialApi
object LauncherScreen {
    const val route = "auth"

    @Composable
    fun view(
        onDrawerStateChanged: (Boolean) -> Unit = {},
        viewModel: LauncherViewModel = hiltViewModel(),
    ) {
        val state by viewModel.state.collectAsState(Processing)

        when (state) {
            AccountNeeded -> viewModel.addAccount()
            PrimaryExist -> HomeScreen.view(
                onDrawerStateChanged = onDrawerStateChanged,
                onSignIn = { viewModel.signIn(it) },
                onSignOut = { viewModel.signOut(it) },
                onRemove = { viewModel.remove(it) },
                onSwitch = { viewModel.switch(it) },
            )
            Processing -> ProtonCenteredProgress(Modifier.fillMaxSize())
            StepNeeded -> ProtonCenteredProgress(Modifier.fillMaxSize())
        }
    }
}
