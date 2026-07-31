/*
 * Copyright (c) 2025-2026 Proton AG
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

package proton.android.pass.features.credentials.passwords.selection.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.credentials.GetCredentialResponse
import androidx.credentials.PasswordCredential
import androidx.credentials.provider.CallingAppInfo
import androidx.credentials.provider.PendingIntentHandler
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import proton.android.pass.appconfig.api.AppConfig
import proton.android.pass.appconfig.api.BuildFlavor.Companion.upgradeUrl
import proton.android.pass.autofill.api.suggestions.PackageNameUrlSuggestionAdapter
import proton.android.pass.commonui.api.AndroidUtils
import proton.android.pass.commonui.api.BrowserUtils
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.enableEdgeToEdgeProtonPass
import proton.android.pass.composecomponents.impl.dialogs.WarningReloadAppDialog
import proton.android.pass.composecomponents.impl.theme.isDark
import proton.android.pass.data.api.usecases.Suggestion
import proton.android.pass.domain.credentials.PasswordCredentialItem
import proton.android.pass.domain.entity.PackageName
import proton.android.pass.features.credentials.passwords.selection.navigation.PasswordCredentialSelectionNavEvent
import proton.android.pass.features.credentials.passwords.selection.presentation.PasswordCredentialSelectionEvent
import proton.android.pass.features.credentials.passwords.selection.presentation.PasswordCredentialSelectionRequest
import proton.android.pass.features.credentials.passwords.selection.presentation.PasswordCredentialSelectionRequestType
import proton.android.pass.features.credentials.passwords.selection.presentation.dispatch
import proton.android.pass.features.credentials.passwords.selection.presentation.PasswordCredentialSelectionState
import proton.android.pass.features.credentials.passwords.selection.presentation.PasswordCredentialSelectionViewModel
import proton.android.pass.features.credentials.shared.passwords.search.PasswordCallerContext
import proton.android.pass.features.credentials.shared.passwords.search.PasswordCallerContextResolver
import proton.android.pass.features.credentials.shared.passwords.domain.PasswordRequestType
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@[AndroidEntryPoint RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)]
internal class PasswordCredentialSelectionActivity : FragmentActivity() {

    @Inject
    internal lateinit var packageNameUrlSuggestionAdapter: PackageNameUrlSuggestionAdapter

    @Inject
    internal lateinit var snackbarDispatcher: SnackbarDispatcher

    @Inject
    internal lateinit var appConfig: AppConfig

    @Inject
    internal lateinit var passwordCallerContextResolver: PasswordCallerContextResolver

    private val viewModel: PasswordCredentialSelectionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.onUpdateRequest(getPasswordSelectionRequest())

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stateFlow.collectLatest { state ->
                    when (state) {
                        PasswordCredentialSelectionState.NotReady -> Unit
                        PasswordCredentialSelectionState.Close -> onCancelSelectionRequest()
                        is PasswordCredentialSelectionState.Ready -> setContent(state)
                    }
                }
            }
        }
    }

    override fun onStop() {
        snackbarDispatcher.reset()

        super.onStop()
    }

    private fun setContent(state: PasswordCredentialSelectionState.Ready) {
        enableEdgeToEdgeProtonPass()
        setContent {
            val context = LocalContext.current
            val isDark = isDark(state.themePreference)
            var showWarningReloadAppDialog by rememberSaveable { mutableStateOf(false) }
            LaunchedEffect(isDark) {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightNavigationBars = !isDark
                    isAppearanceLightStatusBars = !isDark
                }
            }
            PassTheme(isDark = isDark) {
                PasswordCredentialSelectionScreen(
                    state = state,
                    onNavigate = { destination ->
                        onSelectionNavigate(
                            destination = destination,
                            state = state,
                            context = context,
                            onShowWarningReloadAppDialog = { showWarningReloadAppDialog = true }
                        )
                    },
                    onEvent = { event -> onSelectionEvent(event, state) }
                )

                if (showWarningReloadAppDialog) {
                    WarningReloadAppDialog(
                        onOkClick = { reminderCheck ->
                            showWarningReloadAppDialog = false
                            if (reminderCheck) {
                                viewModel.doNotDisplayReloadAppWarningDialog()
                            }
                            openUpgradeWebsite(context)
                        },
                        onCancelClick = {
                            showWarningReloadAppDialog = false
                        }
                    )
                }
            }
        }
    }

    private fun onSelectionEvent(
        event: PasswordCredentialSelectionEvent,
        state: PasswordCredentialSelectionState.Ready
    ) {
        when (event) {
            PasswordCredentialSelectionEvent.OnAuthPerformed -> {
                viewModel.onAuthPerformed(request = state.request)
            }

            is PasswordCredentialSelectionEvent.OnItemSelected -> {
                viewModel.onItemSelected(itemUiModel = event.itemUiModel)
            }

            PasswordCredentialSelectionEvent.OnAssociationConfirmed -> {
                viewModel.onAssociationConfirmed()
            }

            PasswordCredentialSelectionEvent.OnAssociationCancelled -> {
                viewModel.onAssociationCancelled()
            }

            PasswordCredentialSelectionEvent.OnSelectScreenShown -> {
                viewModel.onScreenShown()
            }

            is PasswordCredentialSelectionEvent.OnEventConsumed -> {
                viewModel.onEventConsumed(event = event.event)
            }
        }
    }

    private fun onSelectionNavigate(
        destination: PasswordCredentialSelectionNavEvent,
        state: PasswordCredentialSelectionState.Ready,
        context: Context,
        onShowWarningReloadAppDialog: () -> Unit
    ) {
        when (destination) {
            PasswordCredentialSelectionNavEvent.Cancel -> onCancelSelectionRequest()
            is PasswordCredentialSelectionNavEvent.ForceSignOut -> {
                viewModel.onSignOut(userId = destination.userId)
            }

            is PasswordCredentialSelectionNavEvent.SendResponse -> {
                onProceedSelectionRequest(
                    id = destination.id,
                    password = destination.password
                )
            }

            PasswordCredentialSelectionNavEvent.Upgrade -> {
                if (state.canShowWarningReloadApp) {
                    onShowWarningReloadAppDialog()
                } else {
                    openUpgradeWebsite(context)
                }
            }
        }
    }

    private fun openUpgradeWebsite(context: Context) {
        BrowserUtils.openWebsite(
            context = context,
            website = appConfig.flavor.upgradeUrl()
        )
    }

    private fun getPasswordSelectionRequest(): PasswordCredentialSelectionRequest? = intent.extras
        ?.let { extrasBundle ->
            PasswordCredentialSelectionRequestType
                .from(extrasBundle.getString(EXTRAS_REQUEST_TYPE_KEY))
                .dispatch(
                    onSelect = { createPasswordSelectRequest(extrasBundle) },
                    onUse = { createPasswordUseRequest(extrasBundle) },
                    onUnknown = {
                        PassLogger.w(TAG, "Unknown Password Credential selection request type")
                        null
                    }
                )
        }

    private fun createPasswordSelectRequest(extrasBundle: Bundle): PasswordCredentialSelectionRequest? {
        val suggestion = getSuggestionFromExtras(extrasBundle)
        val title = getTitleFromSuggestion(suggestion)
        val callerContext = getPasswordCallerContext()

        return PasswordCredentialSelectionRequest.Select(
            title = title,
            suggestion = suggestion,
            callerContext = callerContext
        )
    }

    private fun getPasswordCallerContext(): PasswordCallerContext? {
        val callingAppInfo = extractPasswordCallingAppInfo(intent) ?: run {
            PassLogger.w(TAG, "Password selection request does not contain BeginGetCredentialRequest")
            return null
        }
        return passwordCallerContextResolver.resolve(callingAppInfo)
    }

    private fun createPasswordUseRequest(extrasBundle: Bundle): PasswordCredentialSelectionRequest? {
        val username = extrasBundle.getString(EXTRAS_REQUEST_USERNAME) ?: run {
            PassLogger.w(TAG, "Password selection request does not contain username")
            return null
        }

        val encryptedPassword = extrasBundle.getString(EXTRAS_REQUEST_ENCRYPTED_PASSWORD) ?: run {
            PassLogger.w(TAG, "Password selection request does not contain username")
            return null
        }

        val suggestion = getSuggestionFromExtras(extrasBundle)

        val title = getTitleFromSuggestion(suggestion)

        return PasswordCredentialSelectionRequest.Use(
            username = username,
            encryptedPassword = encryptedPassword,
            suggestion = suggestion,
            title = title
        )
    }

    private fun getSuggestionFromExtras(extrasBundle: Bundle): Suggestion {
        return packageNameUrlSuggestionAdapter.adapt(
            packageName = PackageName(extrasBundle.getString(EXTRAS_REQUEST_PACKAGE_NAME).orEmpty()),
            url = extrasBundle.getString(EXTRAS_REQUEST_URL).orEmpty()
        ).toSuggestion()
    }

    private fun getTitleFromSuggestion(suggestion: Suggestion): String = when (suggestion) {
        is Suggestion.Url -> suggestion.value
        is Suggestion.PackageName -> AndroidUtils.getApplicationName(this, suggestion.value)
            .value()
            ?: suggestion.value
    }

    private fun onCancelSelectionRequest() {
        setResult(RESULT_CANCELED)

        finish()
    }

    internal fun onProceedSelectionRequest(id: String, password: String) {
        PasswordCredential(id = id, password = password)
            .let(::GetCredentialResponse)
            .also { getCredentialResponse ->
                val responseIntent = Intent()

                PendingIntentHandler.setGetCredentialResponse(
                    intent = responseIntent,
                    response = getCredentialResponse
                )

                setResult(RESULT_OK, responseIntent)

                finish()
            }
    }


    internal companion object {

        private const val TAG = "PasswordCredentialSelectionActivity"

        private const val EXTRAS_REQUEST_USERNAME = "REQUEST_USERNAME"
        private const val EXTRAS_REQUEST_ENCRYPTED_PASSWORD = "REQUEST_ENCRYPTED_PASSWORD"
        private const val EXTRAS_REQUEST_URL = "REQUEST_ENCRYPTED_URL"
        private const val EXTRAS_REQUEST_PACKAGE_NAME = "REQUEST_PACKAGE_NAME"
        private const val EXTRAS_REQUEST_TYPE_KEY = "REQUEST_TYPE"

        internal fun createPasswordCredentialIntent(
            context: Context,
            passwordCredentialItem: PasswordCredentialItem,
            suggestion: Suggestion
        ): Intent = Intent(
            context,
            PasswordCredentialSelectionActivity::class.java
        ).apply {
            setPackage(context.packageName)

            putExtra(EXTRAS_REQUEST_USERNAME, passwordCredentialItem.username)
            putExtra(EXTRAS_REQUEST_ENCRYPTED_PASSWORD, passwordCredentialItem.encryptedPassword)
            putExtra(EXTRAS_REQUEST_URL, (suggestion as? Suggestion.Url)?.value)
            putExtra(EXTRAS_REQUEST_PACKAGE_NAME, (suggestion as? Suggestion.PackageName)?.value)
            putExtra(EXTRAS_REQUEST_TYPE_KEY, PasswordRequestType.UsePassword.name)
        }

        internal fun createPasswordCredentialIntent(context: Context, suggestion: Suggestion): Intent = Intent(
            context,
            PasswordCredentialSelectionActivity::class.java
        ).apply {
            setPackage(context.packageName)

            putExtra(EXTRAS_REQUEST_URL, (suggestion as? Suggestion.Url)?.value)
            putExtra(EXTRAS_REQUEST_PACKAGE_NAME, (suggestion as? Suggestion.PackageName)?.value)
            putExtra(EXTRAS_REQUEST_TYPE_KEY, PasswordRequestType.SelectPassword.name)
        }

    }

}

internal fun extractPasswordCallingAppInfo(intent: Intent): CallingAppInfo? =
    PendingIntentHandler.retrieveBeginGetCredentialRequest(intent)?.callingAppInfo
