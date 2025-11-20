/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.credentials.passwords.creation.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CreatePasswordResponse
import androidx.credentials.provider.PendingIntentHandler
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.enableEdgeToEdgeProtonPass
import proton.android.pass.composecomponents.impl.theme.isDark
import proton.android.pass.features.credentials.passwords.creation.navigation.PasswordCredentialCreationNavEvent
import proton.android.pass.features.credentials.passwords.creation.presentation.PasswordCredentialCreationEvent
import proton.android.pass.features.credentials.passwords.creation.presentation.PasswordCredentialCreationRequest
import proton.android.pass.features.credentials.passwords.creation.presentation.PasswordCredentialCreationState
import proton.android.pass.features.credentials.passwords.creation.presentation.PasswordCredentialCreationViewModel
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@[AndroidEntryPoint RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)]
internal class PasswordCredentialCreationActivity : FragmentActivity() {

    @Inject
    internal lateinit var snackbarDispatcher: SnackbarDispatcher

    private val viewModel: PasswordCredentialCreationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.onUpdateRequest(getPasswordCredentialCreationRequest())

        viewModel.onRegister(this)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stateFlow.collectLatest { state ->
                    when (state) {
                        PasswordCredentialCreationState.NotReady -> Unit
                        PasswordCredentialCreationState.Close -> onCancelCreationRequest()
                        is PasswordCredentialCreationState.Ready -> setContent(state)
                    }
                }
            }
        }
    }

    override fun onStop() {
        snackbarDispatcher.reset()

        super.onStop()
    }

    private fun setContent(state: PasswordCredentialCreationState.Ready) {
        enableEdgeToEdgeProtonPass()
        setContent {
            val isDark = isDark(state.themePreference)
            PassTheme(isDark = isDark) {
                PasswordCredentialCreationScreen(
                    state = state,
                    onNavigate = { destination ->
                        when (destination) {
                            PasswordCredentialCreationNavEvent.Cancel -> {
                                onCancelCreationRequest()
                            }

                            is PasswordCredentialCreationNavEvent.ForceSignOut -> {
                                viewModel.onSignOut(userId = destination.userId)
                            }

                            PasswordCredentialCreationNavEvent.SendResponse -> {
                                onProceedCreationRequest()
                            }

                            PasswordCredentialCreationNavEvent.Upgrade -> {
                                viewModel.onUpgrade()
                            }
                        }
                    },
                    onEvent = { event ->
                        when (event) {
                            is PasswordCredentialCreationEvent.OnEventConsumed -> {
                                viewModel.onConsumeEvent(event = event.event)
                            }
                        }
                    }
                )
            }
        }
    }

    private fun getPasswordCredentialCreationRequest(): PasswordCredentialCreationRequest? =
        PendingIntentHandler.retrieveProviderCreateCredentialRequest(intent)
            ?.let { providerCreateCredentialRequest ->
                (providerCreateCredentialRequest.callingRequest as? CreatePasswordRequest)
                    ?.let { createPasswordRequest ->
                        PasswordCredentialCreationRequest(
                            id = createPasswordRequest.id,
                            password = createPasswordRequest.password,
                            domain = createPasswordRequest.origin.orEmpty(),
                            packageName = providerCreateCredentialRequest.callingAppInfo.packageName,
                            context = this
                        )
                    }
            }

    private fun onCancelCreationRequest() {
        setResult(RESULT_CANCELED)

        finish()
    }

    private fun onProceedCreationRequest() {
        CreatePasswordResponse()
            .also {
                viewModel.onResponseSent()
            }
            .also { createPasswordResponse ->
                val responseIntent = Intent()

                PendingIntentHandler.setCreateCredentialResponse(
                    intent = responseIntent,
                    response = createPasswordResponse
                )

                setResult(RESULT_OK, responseIntent)

                finish()
            }
    }

}
