package proton.android.pass.ui

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.view.autofill.AutofillManager
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import me.proton.core.compose.component.ProtonCenteredProgress
import proton.android.pass.BuildConfig
import proton.android.pass.ui.launcher.LauncherViewModel
import proton.android.pass.ui.launcher.LauncherViewModel.State.AccountNeeded
import proton.android.pass.ui.launcher.LauncherViewModel.State.PrimaryExist
import proton.android.pass.ui.launcher.LauncherViewModel.State.Processing
import proton.android.pass.ui.launcher.LauncherViewModel.State.StepNeeded

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val launcherViewModel: LauncherViewModel by viewModels()

    @OptIn(ExperimentalLifecycleComposeApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        setupSecureMode()
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Register activities for result.
        launcherViewModel.register(this)

        setContent {
            val state by launcherViewModel.state.collectAsStateWithLifecycle()
            splashScreen.setKeepOnScreenCondition {
                state == Processing || state == StepNeeded
            }

            when (state) {
                AccountNeeded -> {
                    disableAutofill()
                    launcherViewModel.addAccount()
                }

                Processing -> ProtonCenteredProgress(Modifier.fillMaxSize())
                StepNeeded -> ProtonCenteredProgress(Modifier.fillMaxSize())
                PrimaryExist -> {
                    PassApp(
                        onNavigate = {
                            when (it) {
                                AppNavigation.Finish -> finish()
                                AppNavigation.Report -> launcherViewModel.report()
                                is AppNavigation.SignOut -> launcherViewModel.remove(it.userId)
                                AppNavigation.Subscription -> launcherViewModel.subscription()
                                AppNavigation.Upgrade -> launcherViewModel.upgrade()
                            }
                        }
                    )
                }
            }
        }
    }

    private fun setupSecureMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            setRecentsScreenshotEnabled(false)
        }
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
