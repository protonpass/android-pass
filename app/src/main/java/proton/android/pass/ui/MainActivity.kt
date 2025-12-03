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

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.IntentSanitizer
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import me.proton.core.accountmanager.presentation.compose.SignOutDialogActivity
import me.proton.core.compose.component.ProtonCenteredProgress
import me.proton.core.notification.presentation.deeplink.DeeplinkManager
import me.proton.core.notification.presentation.deeplink.onActivityCreate
import me.proton.core.telemetry.domain.TelemetryManager
import me.proton.core.telemetry.presentation.ProductMetricsDelegate
import me.proton.core.telemetry.presentation.ProductMetricsDelegateOwner
import me.proton.core.telemetry.presentation.compose.LocalProductMetricsDelegateOwner
import proton.android.pass.PassActivityOrchestrator
import proton.android.pass.appconfig.api.AppConfig
import proton.android.pass.appconfig.api.BuildFlavor.Companion.isQuest
import proton.android.pass.autofill.di.UserPreferenceEntryPoint
import proton.android.pass.commonui.api.BrowserUtils
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.enableEdgeToEdgeProtonPass
import proton.android.pass.commonui.api.setSecureMode
import proton.android.pass.composecomponents.impl.theme.isDark
import proton.android.pass.features.welcome.WelcomeScreen
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.AllowScreenshotsPreference
import proton.android.pass.ui.internal.WarningReloadAppDialog
import proton.android.pass.ui.launcher.AccountState.AccountNeeded
import proton.android.pass.ui.launcher.AccountState.PrimaryExist
import proton.android.pass.ui.launcher.AccountState.Processing
import proton.android.pass.ui.launcher.AccountState.StepNeeded
import proton.android.pass.ui.launcher.LauncherViewModel
import javax.inject.Inject

private const val PROTON_DEFAULT_UPGRADE_URL = "https://account.proton.me/pass/upgrade"
private const val PROTON_HORIZON_UPGRADE_URL =
    "https://go.getproton.me/aff_c?offer_id=48&aff_id=11853&url_id=1283"

@AndroidEntryPoint
class MainActivity : FragmentActivity(), ProductMetricsDelegateOwner {

    @Inject
    lateinit var appConfig: AppConfig

    @Inject
    lateinit var deeplinkManager: DeeplinkManager

    @Inject
    lateinit var passActivityOrchestrator: PassActivityOrchestrator

    @Inject
    lateinit var telemetryManager: TelemetryManager

    private val launcherViewModel: LauncherViewModel by viewModels()

    private val updateResultLauncher: ActivityResultLauncher<IntentSenderRequest> =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result: ActivityResult ->
            when (result.resultCode) {
                RESULT_CANCELED -> launcherViewModel.declineUpdate()
                else -> {}
            }
        }

    override val productMetricsDelegate = object : ProductMetricsDelegate {
        override val telemetryManager: TelemetryManager get() = this@MainActivity.telemetryManager
        override val productGroup: String = "account.any.signup"
        override val productFlow: String = "pass_new_login"
    }

    @Suppress("LongMethod")
    override fun onCreate(savedInstanceState: Bundle?) {
        setSecureMode()
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        deeplinkManager.onActivityCreate(this, savedInstanceState)
        enableEdgeToEdgeProtonPass()

        // Register activities for result.
        launcherViewModel.register(this)
        passActivityOrchestrator.register(this)

        setContent {
            val context = LocalContext.current
            val state by launcherViewModel.state.collectAsState()
            runCatching {
                splashScreen.setKeepOnScreenCondition { state.accountState == Processing }
            }.onFailure {
                PassLogger.w(TAG, "Error setting splash screen keep on screen condition")
                PassLogger.w(TAG, it)
            }
            DisposableEffect(state) {
                when (state.accountState) {
                    AccountNeeded -> {
                        if (!state.isNewLoginFlowEnabled) launcherViewModel.onAccountNeeded()
                    }

                    PrimaryExist -> launcherViewModel.onPrimaryExist(updateResultLauncher)
                    Processing,
                    StepNeeded -> Unit
                }
                onDispose {
                    when (state.accountState) {
                        PrimaryExist -> launcherViewModel.cancelUpdateListener()
                        Processing,
                        AccountNeeded,
                        StepNeeded -> Unit
                    }
                }
            }

            val isDark = isDark(state.themePreference)
            PassTheme(isDark = isDark) {
                var showWarningReloadAppDialog by rememberSaveable { mutableStateOf(false) }

                when (state.accountState) {
                    Processing,
                    StepNeeded -> ProtonCenteredProgress(Modifier.fillMaxSize())

                    AccountNeeded -> {
                        if (state.isNewLoginFlowEnabled) {
                            CompositionLocalProvider(LocalProductMetricsDelegateOwner provides this@MainActivity) {
                                WelcomeScreen(
                                    onSignUp = { launcherViewModel.signUp() },
                                    onSignIn = { launcherViewModel.signIn() }
                                )
                            }
                        } else {
                            ProtonCenteredProgress(Modifier.fillMaxSize())
                        }
                    }

                    PrimaryExist ->
                        PassApp(
                            onNavigate = {
                                when (it) {
                                    is AppNavigation.Finish -> finish()
                                    is AppNavigation.SignOut ->
                                        SignOutDialogActivity.start(this, it.userId)

                                    is AppNavigation.SignIn -> launcherViewModel.signIn(it.userId)
                                    is AppNavigation.ForceSignOut -> launcherViewModel.disable(it.userId)
                                    is AppNavigation.Subscription -> launcherViewModel.subscription()
                                    is AppNavigation.Upgrade -> {
                                        when {
                                            state.supportPayment -> {
                                                launcherViewModel.upgrade()
                                            }

                                            !state.supportPayment &&
                                                state.canShowWarningReloadApp -> {
                                                showWarningReloadAppDialog = true
                                            }

                                            else -> {
                                                BrowserUtils.openWebsite(
                                                    context = context,
                                                    website = when {
                                                        appConfig.flavor.isQuest() -> PROTON_HORIZON_UPGRADE_URL

                                                        else -> PROTON_DEFAULT_UPGRADE_URL
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    is AppNavigation.Restart -> restartApp()
                                    is AppNavigation.PasswordManagement -> launcherViewModel.passwordManagement()
                                    is AppNavigation.RecoveryEmail -> launcherViewModel.recoveryEmail()
                                    is AppNavigation.AddAccount -> launcherViewModel.signIn()
                                    is AppNavigation.RemoveAccount -> launcherViewModel.remove(it.userId)
                                    is AppNavigation.SwitchAccount -> {
                                        launcherViewModel.switch(it.userId)
                                    }

                                    is AppNavigation.SecurityKeys -> launcherViewModel.securityKeys()
                                    is AppNavigation.ForceSignOutAllUsers -> launcherViewModel.disableAll()
                                }
                            }
                        )
                }

                if (showWarningReloadAppDialog) {
                    WarningReloadAppDialog(
                        onOkClick = { reminderCheck ->
                            showWarningReloadAppDialog = false
                            if (reminderCheck) {
                                launcherViewModel.doNotDisplayReloadAppWarningDialog()
                            }
                            BrowserUtils.openWebsite(
                                context = context,
                                website = PROTON_DEFAULT_UPGRADE_URL
                            )
                        },
                        onCancelClick = {
                            showWarningReloadAppDialog = false
                        }
                    )
                }
            }
        }
    }

    private fun restartApp() {
        val sanitizedIntent = IntentSanitizer.Builder()
            .allowComponent(ComponentName(this, MainActivity::class.java))
            .allowReceiverFlags()
            .allowAction(Intent.ACTION_MAIN)
            .allowPackage(this.packageName)
            .allowCategory(Intent.CATEGORY_LAUNCHER)
            .build()
            .sanitizeByFiltering(intent)

        finish()
        startActivity(sanitizedIntent)
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
