package proton.android.pass.ui

import android.os.Bundle
import android.view.autofill.AutofillManager
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import me.proton.core.compose.component.ProtonCenteredProgress
import proton.android.pass.commonui.api.setSecureMode
import proton.android.pass.di.UserPreferenceModule
import proton.android.pass.preferences.AllowScreenshotsPreference
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
        setSecureMode()
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

            LaunchedEffect(state) {
                launcherViewModel.onUserStateChanced(state)
            }
            when (state) {
                AccountNeeded -> {
                    disableAutofill()
                    launcherViewModel.addAccount()
                }

                Processing -> ProtonCenteredProgress(Modifier.fillMaxSize())
                StepNeeded -> ProtonCenteredProgress(Modifier.fillMaxSize())
                PrimaryExist ->
                    PassApp(
                        onNavigate = {
                            when (it) {
                                AppNavigation.Finish -> finish()
                                AppNavigation.Report -> launcherViewModel.report()
                                is AppNavigation.SignOut -> launcherViewModel.remove(it.userId)
                                AppNavigation.Subscription -> launcherViewModel.subscription()
                                AppNavigation.Upgrade -> launcherViewModel.upgrade()
                                AppNavigation.Restart -> restartApp()
                            }
                        }
                    )
            }
        }
    }

    private fun restartApp() {
        val intent = intent
        finish()
        startActivity(intent)
    }

    private fun disableAutofill() {
        val autofillManager = getSystemService(AutofillManager::class.java)
        autofillManager.disableAutofillServices()
    }

    private fun setSecureMode() {
        val factory = EntryPointAccessors.fromApplication(this, UserPreferenceModule::class.java)
        val repository = factory.getRepository()
        val setting = runBlocking {
            repository.getAllowScreenshotsPreference()
                .firstOrNull()
                ?: AllowScreenshotsPreference.Enabled
        }
        setSecureMode(setting)
    }
}
