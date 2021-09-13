package me.proton.android.pass.ui

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.android.pass.BuildConfig
import me.proton.android.pass.log.AppLogTag
import me.proton.android.pass.ui.launcher.AccountViewModel
import me.proton.android.pass.ui.navigation.AppNavGraph
import me.proton.android.pass.ui.theme.PasswordManagerTheme
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.util.kotlin.Logger
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var keyStoreCrypto: KeyStoreCrypto

    private val accountViewModel: AccountViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        applySecureFlag()
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setupAccountViewModel()

        setContent {
            val systemUiController = rememberSystemUiController()
            var isDrawerOpen by remember { mutableStateOf(false) }
            LaunchedEffect(isDrawerOpen) {
                systemUiController.setStatusBarColor(
                    color = Color.Transparent,
                    darkIcons = !isDrawerOpen
                )
            }

            PasswordManagerTheme {
                ProvideWindowInsets {
                    AppNavGraph(keyStoreCrypto = keyStoreCrypto) { isOpen ->
                        isDrawerOpen = isOpen
                    }
                }
            }
        }
    }

    private fun setupAccountViewModel() {
        accountViewModel.initialize(this)
        accountViewModel.state
            .flowWithLifecycle(lifecycle)
            .onEach { state ->
                logger.d(AppLogTag.UI, "AccountViewModel state = $state")
                when (state) {
                    is AccountViewModel.State.Processing -> Unit
                    is AccountViewModel.State.AccountReady -> Unit
                    is AccountViewModel.State.PrimaryNeeded -> accountViewModel.addAccount()
                    is AccountViewModel.State.ExitApp -> finish()
                }
            }
            .launchIn(lifecycleScope)
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