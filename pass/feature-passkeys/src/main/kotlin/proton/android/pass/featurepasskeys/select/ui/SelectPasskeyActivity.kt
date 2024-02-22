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

package proton.android.pass.featurepasskeys.select.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.view.WindowCompat
import androidx.credentials.GetCredentialResponse
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PublicKeyCredential
import androidx.credentials.provider.BeginGetPublicKeyCredentialOption
import androidx.credentials.provider.PendingIntentHandler
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.PasskeyId
import proton.android.pass.domain.ShareId
import proton.android.pass.featurepasskeys.select.navigation.SelectPasskeyNavigation
import proton.android.pass.featurepasskeys.select.presentation.SelectPasskeyActivityViewModel
import proton.android.pass.featurepasskeys.select.presentation.SelectPasskeyAppState
import proton.android.pass.featurepasskeys.select.presentation.SelectPasskeyRequest
import proton.android.pass.featurepasskeys.select.ui.app.SelectPasskeyApp
import proton.android.pass.log.api.PassLogger

@AndroidEntryPoint
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
class SelectPasskeyActivity : FragmentActivity() {
    private val viewModel: SelectPasskeyActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val request = getRequest() ?: run {
            sendResponse(null)
            return
        }
        viewModel.setRequest(request)

        lifecycleScope.launch {
            viewModel.state.collectLatest(::onStateReceived)
        }
    }

    private fun onStateReceived(state: SelectPasskeyAppState) {
        when (state) {
            is SelectPasskeyAppState.NotReady -> {}
            is SelectPasskeyAppState.ErrorAuthenticating,
            is SelectPasskeyAppState.Close -> sendResponse(null)
            is SelectPasskeyAppState.Ready -> {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                setContent {
                    SelectPasskeyApp(
                        appState = state,
                        onNavigate = {
                            when (it) {
                                SelectPasskeyNavigation.Cancel -> {
                                    sendResponse(null)
                                }

                                SelectPasskeyNavigation.ForceSignOut -> {
                                    viewModel.signOut()
                                }

                                SelectPasskeyNavigation.Upgrade -> {
                                    viewModel.upgrade()
                                }

                                is SelectPasskeyNavigation.SendResponse -> {
                                    sendResponse(it.response)
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    @Suppress("CyclomaticComplexMethod", "ComplexMethod", "LongMethod", "ReturnCount")
    private fun getRequest(): SelectPasskeyRequest? {
        val extras = intent.extras ?: run {
            PassLogger.w(TAG, "Intent must contain extras")
            return null
        }
        when (extras.getString(EXTRAS_REQUEST_TYPE_KEY)) {
            RequestType.UsePasskey.name -> {
                val shareId = extras.getString(EXTRAS_SHARE_ID) ?: run {
                    PassLogger.w(TAG, "UsePasskey request does not contain ShareId")
                    return null
                }
                val itemId = extras.getString(EXTRAS_ITEM_ID) ?: run {
                    PassLogger.w(TAG, "UsePasskey request does not contain ItemId")
                    return null
                }
                val passKeyId = extras.getString(EXTRAS_PASSKEY_ID) ?: run {
                    PassLogger.w(TAG, "UsePasskey request does not contain PasskeyId")
                    return null
                }

                val request = PendingIntentHandler.retrieveProviderGetCredentialRequest(intent)
                    ?: run {
                        PassLogger.w(TAG, "Intent does not contain ProviderGetCredentialRequest")
                        return null
                    }

                val option = request.credentialOptions
                    .firstOrNull { it is GetPublicKeyCredentialOption }
                    ?.let { it as GetPublicKeyCredentialOption }
                    ?: run {
                        PassLogger.w(
                            TAG,
                            "Request does not contain any GetPublicKeyCredentialOption"
                        )
                        return null
                    }

                val origin = request.callingAppInfo.origin ?: run {
                    PassLogger.w(TAG, "Request does not contain origin")
                    return null
                }

                return SelectPasskeyRequest.UsePasskey(
                    request = option.requestJson,
                    shareId = ShareId(shareId),
                    itemId = ItemId(itemId),
                    passkeyId = PasskeyId(passKeyId),
                    origin = origin
                )
            }

            RequestType.SelectPasskey.name -> {
                val requestJson = extras.getString(EXTRAS_REQUEST_JSON) ?: run {
                    PassLogger.w(TAG, "SelectPasskey request does not contain requestJson")
                    return null
                }
                val requestOrigin = extras.getString(EXTRAS_REQUEST_ORIGIN) ?: run {
                    PassLogger.w(TAG, "SelectPasskey request does not contain requestOrigin")
                    return null
                }
                return SelectPasskeyRequest.SelectPasskey(
                    request = requestJson,
                    origin = requestOrigin
                )
            }

            else -> {
                PassLogger.w(TAG, "Unknown request type")
                return null
            }
        }
    }

    private fun sendResponse(responseJson: String?) {
        if (responseJson == null) {
            setResult(Activity.RESULT_CANCELED)
        } else {
            val response = GetCredentialResponse(PublicKeyCredential(responseJson))

            val responseIntent = Intent()
            PendingIntentHandler.setGetCredentialResponse(responseIntent, response)
            setResult(Activity.RESULT_OK, responseIntent)
        }

        finish()
    }

    companion object {

        private enum class RequestType {
            UsePasskey,
            SelectPasskey
        }

        private const val TAG = "SelectPasskeyActivity"
        private const val EXTRAS_REQUEST_TYPE_KEY = "REQUEST_TYPE"

        private const val EXTRAS_SHARE_ID = "SHARE_ID"
        private const val EXTRAS_ITEM_ID = "ITEM_ID_ID"
        private const val EXTRAS_PASSKEY_ID = "PASSKEY_ID"

        private const val EXTRAS_REQUEST_JSON = "REQUEST_JSON"
        private const val EXTRAS_REQUEST_ORIGIN = "REQUEST_ORIGIN"

        fun createIntentForUsePasskey(
            context: Context,
            shareId: ShareId,
            itemId: ItemId,
            passkeyId: PasskeyId
        ) = Intent(context, SelectPasskeyActivity::class.java).apply {
            setPackage(context.packageName)

            putExtra(EXTRAS_REQUEST_TYPE_KEY, RequestType.UsePasskey.name)
            putExtra(EXTRAS_SHARE_ID, shareId.id)
            putExtra(EXTRAS_ITEM_ID, itemId.id)
            putExtra(EXTRAS_PASSKEY_ID, passkeyId.value)
        }

        fun createIntentForSelectPasskey(
            context: Context,
            option: BeginGetPublicKeyCredentialOption,
            origin: String
        ) = Intent(context, SelectPasskeyActivity::class.java).apply {
            setPackage(context.packageName)

            putExtra(EXTRAS_REQUEST_JSON, option.requestJson)
            putExtra(EXTRAS_REQUEST_ORIGIN, origin)
            putExtra(EXTRAS_REQUEST_TYPE_KEY, RequestType.SelectPasskey.name)
        }
    }
}
