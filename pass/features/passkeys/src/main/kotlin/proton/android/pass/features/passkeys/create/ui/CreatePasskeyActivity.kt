/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.features.passkeys.create.ui

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CreatePublicKeyCredentialResponse
import androidx.credentials.provider.PendingIntentHandler
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.theme.SystemUIDisposableEffect
import proton.android.pass.composecomponents.impl.theme.isDark
import proton.android.pass.features.passkeys.create.presentation.CreatePasskeyActivityViewModel
import proton.android.pass.features.passkeys.create.presentation.CreatePasskeyAppState
import proton.android.pass.features.passkeys.create.presentation.CreatePasskeyRequest
import proton.android.pass.features.passkeys.create.ui.app.CreatePasskeyApp
import proton.android.pass.features.passkeys.create.ui.app.CreatePasskeyNavigation
import proton.android.pass.features.passkeys.create.ui.app.CreatePasskeyResponse
import proton.android.pass.log.api.PassLogger

@AndroidEntryPoint
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
class CreatePasskeyActivity : FragmentActivity() {

    private val viewModel: CreatePasskeyActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val request = getRequest() ?: run {
            sendResponse(CreatePasskeyResponse.Cancel)
            return
        }

        viewModel.register(this)
        viewModel.setRequest(request)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collectLatest { state ->
                    onStateReceived(request, state)
                }
            }
        }
    }

    private fun onStateReceived(request: CreatePasskeyRequest, state: CreatePasskeyAppState) {
        when (state) {
            CreatePasskeyAppState.Close -> sendResponse(CreatePasskeyResponse.Cancel)
            CreatePasskeyAppState.NotReady -> {}
            is CreatePasskeyAppState.Ready -> {
                enableEdgeToEdge()
                setContent {
                    val isDark = isDark(state.theme)
                    SystemUIDisposableEffect(isDark)
                    PassTheme(isDark = isDark) {
                        CreatePasskeyApp(
                            appState = state,
                            request = request,
                            onNavigate = {
                                when (it) {
                                    CreatePasskeyNavigation.Cancel -> {
                                        sendResponse(CreatePasskeyResponse.Cancel)
                                    }

                                    is CreatePasskeyNavigation.ForceSignOut ->
                                        viewModel.signOut(it.userId)

                                    CreatePasskeyNavigation.Upgrade -> {
                                        viewModel.upgrade()
                                    }

                                    is CreatePasskeyNavigation.SendResponse -> {
                                        sendResponse(CreatePasskeyResponse.Success(it.response))
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

    }

    private fun getRequest(): CreatePasskeyRequest? {
        val request = PendingIntentHandler.retrieveProviderCreateCredentialRequest(intent)
        return request?.let {
            when (val req = it.callingRequest) {
                is CreatePublicKeyCredentialRequest -> CreatePasskeyRequest(it.callingAppInfo, req)
                else -> {
                    PassLogger.w(TAG, "Only CreatePublicKeyCredentialRequest is supported")
                    null
                }
            }
        }
    }

    private fun sendResponse(response: CreatePasskeyResponse) {
        when (response) {
            CreatePasskeyResponse.Cancel -> {
                setResult(Activity.RESULT_CANCELED)
            }
            is CreatePasskeyResponse.Success -> {
                viewModel.onResponseSent()
                val responseIntent = Intent()
                val responseData = CreatePublicKeyCredentialResponse(response.response)
                PendingIntentHandler.setCreateCredentialResponse(responseIntent, responseData)
                setResult(Activity.RESULT_OK, responseIntent)
            }
        }

        finish()
    }

    companion object {
        private const val TAG = "CreatePasskeyActivity"
    }
}
