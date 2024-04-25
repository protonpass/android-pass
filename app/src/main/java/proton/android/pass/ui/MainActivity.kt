/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import me.proton.core.accountmanager.presentation.compose.SignOutDialogActivity
import me.proton.core.compose.component.ProtonCenteredProgress
import me.proton.core.notification.presentation.deeplink.DeeplinkManager
import me.proton.core.notification.presentation.deeplink.onActivityCreate
import proton.android.pass.autofill.di.UserPreferenceEntryPoint
import proton.android.pass.commonui.api.setSecureMode
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.AllowScreenshotsPreference
import proton.android.pass.ui.launcher.LauncherViewModel
import proton.android.pass.ui.launcher.LauncherViewModel.State.AccountNeeded
import proton.android.pass.ui.launcher.LauncherViewModel.State.PrimaryExist
import proton.android.pass.ui.launcher.LauncherViewModel.State.Processing
import proton.android.pass.ui.launcher.LauncherViewModel.State.StepNeeded
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var deeplinkManager: DeeplinkManager

    private val launcherViewModel: LauncherViewModel by viewModels()

    private val updateResultLauncher: ActivityResultLauncher<IntentSenderRequest> =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result: ActivityResult ->
            when (result.resultCode) {
                RESULT_CANCELED -> launcherViewModel.declineUpdate()
                else -> {}
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        setSecureMode()
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        deeplinkManager.onActivityCreate(this, savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Register activities for result.
        launcherViewModel.register(this)

        setContent {
            val state by launcherViewModel.state.collectAsStateWithLifecycle()
            runCatching {
                splashScreen.setKeepOnScreenCondition {
                    state == Processing || state == StepNeeded
                }
            }.onFailure {
                PassLogger.w(TAG, "Error setting splash screen keep on screen condition")
                PassLogger.w(TAG, it)
            }
            LaunchedEffect(state) {
                launcherViewModel.onUserStateChanced(state)
            }
            when (state) {
                AccountNeeded -> {
                    launcherViewModel.addAccount()
                }

                Processing -> ProtonCenteredProgress(Modifier.fillMaxSize())
                StepNeeded -> ProtonCenteredProgress(Modifier.fillMaxSize())
                PrimaryExist -> {
                    DisposableEffect(Unit) {
                        launcherViewModel.checkForUpdates(updateResultLauncher)
                        onDispose { launcherViewModel.cancelUpdateListener() }
                    }
                    PassApp(
                        onNavigate = {
                            when (it) {
                                is AppNavigation.Finish -> finish()
                                is AppNavigation.SignOut -> SignOutDialogActivity.start(this)
                                is AppNavigation.ForceSignOut -> launcherViewModel.disable()
                                is AppNavigation.Report -> launcherViewModel.report()
                                is AppNavigation.Subscription -> launcherViewModel.subscription()
                                is AppNavigation.Upgrade -> launcherViewModel.upgrade()
                                is AppNavigation.Restart -> restartApp()
                                is AppNavigation.PasswordManagement -> launcherViewModel.passwordManagement()
                                is AppNavigation.RecoveryEmail -> launcherViewModel.recoveryEmail()
                            }
                        }
                    )
                }
            }
        }
    }

    private fun restartApp() {
        val intent = intent
        finish()
        startActivity(intent)
    }

    private fun setSecureMode() {
        val factory = EntryPointAccessors.fromApplication(
            context = this,
            entryPoint = UserPreferenceEntryPoint::class.java
        )
        val repository = factory.getRepository()
        val setting = runBlocking {
            repository.getAllowScreenshotsPreference()
                .firstOrNull()
                ?: AllowScreenshotsPreference.Disabled
        }
        setSecureMode(setting)
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
