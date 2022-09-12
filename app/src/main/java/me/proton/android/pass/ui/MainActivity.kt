package me.proton.android.pass.ui

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import me.proton.android.pass.BuildConfig
import me.proton.android.pass.ui.launcher.LauncherViewModel
import me.proton.android.pass.ui.navigation.AppNavGraph
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.crypto.common.keystore.KeyStoreCrypto

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var keyStoreCrypto: KeyStoreCrypto

    private val launcherViewModel: LauncherViewModel by viewModels()

    @ExperimentalMaterialApi
    @ExperimentalAnimationApi
    @ExperimentalComposeUiApi
    override fun onCreate(savedInstanceState: Bundle?) {
        applySecureFlag()
        installSplashScreen()
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, true)

        // Register activities for result.
        launcherViewModel.register(this)

        setContent {
            val systemUiController = rememberSystemUiController()
            var isDrawerOpen by remember { mutableStateOf(false) }
            LaunchedEffect(isDrawerOpen) {
                systemUiController.setStatusBarColor(
                    color = Color.Transparent,
                    darkIcons = !isDrawerOpen
                )
            }

            ProtonTheme {
                ProvideWindowInsets {
                    AppNavGraph(
                        keyStoreCrypto = keyStoreCrypto,
                        launcherViewModel = launcherViewModel
                    ) { isOpen ->
                        isDrawerOpen = isOpen
                    }
                }
            }
        }
    }

    private fun applySecureFlag() {
        if (!BuildConfig.DEBUG) {
            // Release builds should secure window so that content is protected
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }
    }
}
