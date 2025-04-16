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

package proton.android.pass.features.credentials.passwords.selection.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.credentials.GetCredentialResponse
import androidx.credentials.PasswordCredential
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
import proton.android.pass.domain.credentials.PasswordCredentialItem
import proton.android.pass.features.credentials.passwords.selection.navigation.PasswordCredentialSelectionNavEvent
import proton.android.pass.features.credentials.passwords.selection.presentation.PasswordCredentialSelectionEvent
import proton.android.pass.features.credentials.passwords.selection.presentation.PasswordCredentialSelectionRequest
import proton.android.pass.features.credentials.passwords.selection.presentation.PasswordCredentialSelectionState
import proton.android.pass.features.credentials.passwords.selection.presentation.PasswordCredentialSelectionViewModel
import proton.android.pass.features.credentials.shared.passwords.domain.PasswordRequestType
import proton.android.pass.log.api.PassLogger

@[AndroidEntryPoint RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)]
internal class PasswordCredentialSelectionActivity : FragmentActivity() {

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

    private fun setContent(state: PasswordCredentialSelectionState.Ready) {
        enableEdgeToEdge()

        setContent {
            val isDark = isDark(state.themePreference)

            SystemUIDisposableEffect(isDark)

            PassTheme(isDark = isDark) {
                PasswordCredentialSelectionScreen(
                    state = state,
                    onNavigate = { destination ->
                        when (destination) {
                            PasswordCredentialSelectionNavEvent.Cancel -> {
                                onCancelSelectionRequest()
                            }

                            is PasswordCredentialSelectionNavEvent.ForceSignOut -> {
                                viewModel.onSignOut(userId = destination.userId)
                            }

                            PasswordCredentialSelectionNavEvent.Upgrade -> {
                                viewModel.onUpgrade()
                            }
                        }
                    },
                    onEvent = { event ->
                        when (event) {
                            PasswordCredentialSelectionEvent.OnAuthPerformed -> {
                                println("JIBIRI: OnAuthPerformed")
                            }

                            is PasswordCredentialSelectionEvent.OnItemSelected -> {
                                println("JIBIRI: OnItemSelected")
                            }

                            PasswordCredentialSelectionEvent.OnSelectScreenShown -> {
                                viewModel.onScreenShown()
                            }
                        }
                    }
                )
            }
        }
    }

    private fun getPasswordSelectionRequest(): PasswordCredentialSelectionRequest? = intent.extras
        ?.let { extrasBundle ->
            when (extrasBundle.getString(EXTRAS_REQUEST_TYPE_KEY)) {
                PasswordRequestType.SelectPassword.name -> {
                    createPasswordSelectRequest(extrasBundle)
                }

                PasswordRequestType.UsePassword.name -> {
                    createPasswordUseRequest(extrasBundle)
                }

                else -> {
                    PassLogger.w(TAG, "Unknown Password Credential selection request type")
                    return null
                }
            }
        }

    private fun createPasswordSelectRequest(extrasBundle: Bundle): PasswordCredentialSelectionRequest? =
        PasswordCredentialSelectionRequest.Select()

    private fun createPasswordUseRequest(extrasBundle: Bundle): PasswordCredentialSelectionRequest? =
        PasswordCredentialSelectionRequest.Use()

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

        private const val EXTRAS_REQUEST_TYPE_KEY = "REQUEST_TYPE"

        internal fun createPasswordCredentialIntent(
            context: Context,
            passwordCredentialItem: PasswordCredentialItem
        ): Intent = Intent(
            context,
            PasswordCredentialSelectionActivity::class.java
        ).apply {
            setPackage(context.packageName)

            putExtra(EXTRAS_REQUEST_TYPE_KEY, PasswordRequestType.UsePassword.name)
        }

        internal fun createPasswordCredentialIntent(context: Context): Intent = Intent(
            context,
            PasswordCredentialSelectionActivity::class.java
        ).apply {
            setPackage(context.packageName)

            putExtra(EXTRAS_REQUEST_TYPE_KEY, PasswordRequestType.SelectPassword.name)
        }

    }

}
