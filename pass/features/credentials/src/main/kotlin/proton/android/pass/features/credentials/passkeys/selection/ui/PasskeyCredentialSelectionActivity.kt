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

package proton.android.pass.features.credentials.passkeys.selection.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.credentials.GetCredentialResponse
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PublicKeyCredential
import androidx.credentials.provider.BeginGetPublicKeyCredentialOption
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
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.PasskeyId
import proton.android.pass.domain.PasskeyItem
import proton.android.pass.domain.ShareId
import proton.android.pass.features.credentials.passkeys.selection.navigation.PasskeyCredentialSelectionNavEvent
import proton.android.pass.features.credentials.passkeys.selection.presentation.PasskeyCredentialSelectionEvent
import proton.android.pass.features.credentials.passkeys.selection.presentation.PasskeyCredentialSelectionRequest
import proton.android.pass.features.credentials.passkeys.selection.presentation.PasskeyCredentialSelectionState
import proton.android.pass.features.credentials.passkeys.selection.presentation.PasskeyCredentialSelectionViewModel
import proton.android.pass.features.credentials.shared.passkeys.domain.PasskeyRequestType
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@[AndroidEntryPoint RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)]
internal class PasskeyCredentialSelectionActivity : FragmentActivity() {

    @Inject
    internal lateinit var snackbarDispatcher: SnackbarDispatcher

    private val viewModel: PasskeyCredentialSelectionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.onUpdateRequest(getPasskeySelectionRequest())

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stateFlow.collectLatest { state ->
                    when (state) {
                        PasskeyCredentialSelectionState.NotReady -> Unit
                        PasskeyCredentialSelectionState.Close -> onCancelAuthRequest()
                        is PasskeyCredentialSelectionState.Ready -> setContent(state)
                    }
                }
            }
        }
    }

    override fun onStop() {
        snackbarDispatcher.reset()

        super.onStop()
    }

    private fun setContent(state: PasskeyCredentialSelectionState.Ready) {
        enableEdgeToEdge()

        setContent {
            val isDark = isDark(state.themePreference)

            SystemUIDisposableEffect(isDark)

            PassTheme(isDark = isDark) {
                PasskeyCredentialSelectionScreen(
                    state = state,
                    onNavigate = { destination ->
                        when (destination) {
                            PasskeyCredentialSelectionNavEvent.Cancel -> {
                                onCancelAuthRequest()
                            }

                            is PasskeyCredentialSelectionNavEvent.ForceSignOut -> {
                                viewModel.onSignOut(destination.userId)
                            }

                            is PasskeyCredentialSelectionNavEvent.SendResponse -> {
                                onProceedAuthRequest(destination.response)
                            }

                            PasskeyCredentialSelectionNavEvent.Upgrade -> {
                                viewModel.onUpgrade()
                            }
                        }
                    },
                    onEvent = { event ->
                        when (event) {
                            PasskeyCredentialSelectionEvent.OnAuthPerformed -> {
                                viewModel.onAuthPerformed(request = state.request)
                            }

                            is PasskeyCredentialSelectionEvent.OnItemSelected -> {
                                viewModel.onItemSelected(
                                    itemUiModel = event.itemUiModel,
                                    origin = state.request.requestOrigin,
                                    request = state.request.requestJson,
                                    clientDataHash = state.request.clientDataHash
                                )
                            }

                            is PasskeyCredentialSelectionEvent.OnPasskeySelected -> {
                                viewModel.onPasskeySelected(
                                    passkey = event.passkey,
                                    origin = state.request.requestOrigin,
                                    request = state.request.requestJson,
                                    clientDataHash = state.request.clientDataHash
                                )
                            }

                            PasskeyCredentialSelectionEvent.OnSelectScreenShown -> {
                                viewModel.onScreenShown()
                            }

                            is PasskeyCredentialSelectionEvent.OnEventConsumed -> {
                                viewModel.onConsumeEvent(event = event.event)
                            }
                        }
                    }
                )
            }
        }
    }

    private fun getPasskeySelectionRequest(): PasskeyCredentialSelectionRequest? = intent.extras?.let { extrasBundle ->
        when (extrasBundle.getString(EXTRAS_REQUEST_TYPE_KEY)) {
            PasskeyRequestType.SelectPasskey.name -> createPasskeySelectRequest(extrasBundle)
            PasskeyRequestType.UsePasskey.name -> createPasskeyUseRequest(extrasBundle)
            else -> {
                PassLogger.w(TAG, "Unknown passkey selection request type")
                return null
            }
        }
    }

    private fun createPasskeySelectRequest(extrasBundle: Bundle): PasskeyCredentialSelectionRequest? {
        val options = PendingIntentHandler.retrieveBeginGetCredentialRequest(intent)
            ?.beginGetCredentialOptions
            ?.firstOrNull { it is BeginGetPublicKeyCredentialOption }
            ?.let { it as BeginGetPublicKeyCredentialOption }
            ?: run {
                PassLogger.w(
                    TAG,
                    "Passkey selection request does not contain BeginGetPublicKeyCredentialOption"
                )
                return null
            }

        val requestOrigin = extrasBundle.getString(EXTRAS_REQUEST_ORIGIN) ?: run {
            PassLogger.w(TAG, "Passkey selection request does not contain requestOrigin")
            return null
        }

        return PasskeyCredentialSelectionRequest.Select(
            requestJson = options.requestJson,
            requestOrigin = requestOrigin,
            clientDataHash = options.clientDataHash
        )
    }

    @Suppress("ReturnCount")
    private fun createPasskeyUseRequest(extrasBundle: Bundle): PasskeyCredentialSelectionRequest? {
        val options = PendingIntentHandler.retrieveProviderGetCredentialRequest(intent)
            ?.credentialOptions
            ?.firstOrNull { it is GetPublicKeyCredentialOption }
            ?.let { it as GetPublicKeyCredentialOption }
            ?: run {
                PassLogger.w(
                    TAG,
                    "Passkey usage request does not contain GetPublicKeyCredentialOption"
                )
                return null
            }

        val requestOrigin = extrasBundle.getString(EXTRAS_REQUEST_ORIGIN) ?: run {
            PassLogger.w(TAG, "Passkey usage request does not contain requestOrigin")
            return null
        }

        val shareId = extrasBundle.getString(EXTRAS_SHARE_ID)?.let(::ShareId) ?: run {
            PassLogger.w(TAG, "Passkey usage request does not contain ShareId")
            return null
        }

        val itemId = extrasBundle.getString(EXTRAS_ITEM_ID)?.let(::ItemId) ?: run {
            PassLogger.w(TAG, "Passkey usage request does not contain ItemId")
            return null
        }

        val passKeyId = extrasBundle.getString(EXTRAS_PASSKEY_ID)?.let(::PasskeyId) ?: run {
            PassLogger.w(TAG, "Passkey usage request does not contain PasskeyId")
            return null
        }

        return PasskeyCredentialSelectionRequest.Use(
            requestJson = options.requestJson,
            requestOrigin = requestOrigin,
            clientDataHash = options.clientDataHash,
            shareId = shareId,
            itemId = itemId,
            passkeyId = passKeyId
        )
    }

    private fun onCancelAuthRequest() {
        setResult(RESULT_CANCELED)

        finish()
    }

    private fun onProceedAuthRequest(authResponseJson: String) {
        PublicKeyCredential(authenticationResponseJson = authResponseJson)
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

        private const val TAG = "PasskeyCredentialSelectionActivity"

        private const val EXTRAS_REQUEST_TYPE_KEY = "REQUEST_TYPE"
        private const val EXTRAS_SHARE_ID = "SHARE_ID"
        private const val EXTRAS_ITEM_ID = "ITEM_ID_ID"
        private const val EXTRAS_PASSKEY_ID = "PASSKEY_ID"
        private const val EXTRAS_REQUEST_ORIGIN = "REQUEST_ORIGIN"

        internal fun createPasskeyCredentialIntent(
            context: Context,
            passkeyItem: PasskeyItem,
            origin: String
        ): Intent = Intent(
            context,
            PasskeyCredentialSelectionActivity::class.java
        ).apply {
            setPackage(context.packageName)

            putExtra(EXTRAS_REQUEST_ORIGIN, origin)

            putExtra(EXTRAS_REQUEST_TYPE_KEY, PasskeyRequestType.UsePasskey.name)
            putExtra(EXTRAS_SHARE_ID, passkeyItem.shareId.id)
            putExtra(EXTRAS_ITEM_ID, passkeyItem.itemId.id)
            putExtra(EXTRAS_PASSKEY_ID, passkeyItem.passkey.id.value)
        }

        internal fun createPasskeyCredentialIntent(context: Context, origin: String): Intent = Intent(
            context,
            PasskeyCredentialSelectionActivity::class.java
        ).apply {
            setPackage(context.packageName)

            putExtra(EXTRAS_REQUEST_TYPE_KEY, PasskeyRequestType.SelectPasskey.name)
            putExtra(EXTRAS_REQUEST_ORIGIN, origin)
        }

    }

}
