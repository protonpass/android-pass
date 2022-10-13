package me.proton.android.pass.ui

import android.os.Bundle
import android.view.WindowManager
import android.view.autofill.AutofillManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint
import me.proton.android.pass.BuildConfig
import me.proton.android.pass.ui.launcher.LauncherViewModel
import me.proton.android.pass.ui.launcher.LauncherViewModel.State.AccountNeeded
import me.proton.android.pass.ui.launcher.LauncherViewModel.State.PrimaryExist
import me.proton.android.pass.ui.launcher.LauncherViewModel.State.Processing
import me.proton.android.pass.ui.launcher.LauncherViewModel.State.StepNeeded
import me.proton.core.compose.component.ProtonCenteredProgress
import me.proton.core.pass.presentation.components.navigation.AuthNavigation

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val launcherViewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        applySecureFlag()
        installSplashScreen()
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, true)

        // Register activities for result.
        launcherViewModel.register(this)

        setContent {
            val state by launcherViewModel.state.collectAsState(Processing)
            when (state) {
                AccountNeeded -> {
                    disableAutofill()
                    launcherViewModel.addAccount()
                }
                Processing -> ProtonCenteredProgress(Modifier.fillMaxSize())
                StepNeeded -> ProtonCenteredProgress(Modifier.fillMaxSize())
                PrimaryExist -> {
                    val authNavigation = AuthNavigation(
                        onSignIn = { launcherViewModel.signIn(it) },
                        onSignOut = { launcherViewModel.signOut(it) },
                        onRemove = { launcherViewModel.remove(it) },
                        onSwitch = { launcherViewModel.switch(it) }
                    )
                    PassApp(authNavigation = authNavigation)
                }
            }
        }
    }

    private fun applySecureFlag() {
        if (!BuildConfig.ALLOW_SCREENSHOTS) {
            // Release builds should secure window so that content is protected
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }
    }

    private fun disableAutofill() {
        val autofillManager = getSystemService(AutofillManager::class.java)
        autofillManager.disableAutofillServices()
    }
}
