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

package proton.android.pass.features.credentials.passkeys.usage.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.credentials.GetCredentialResponse
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
import proton.android.pass.data.api.usecases.passkeys.PasskeyItem
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.PasskeyId
import proton.android.pass.domain.ShareId
import proton.android.pass.features.credentials.passkeys.usage.presentation.PasskeyCredentialUsageRequest
import proton.android.pass.features.credentials.passkeys.usage.presentation.PasskeyCredentialUsageState
import proton.android.pass.features.credentials.passkeys.usage.presentation.PasskeyCredentialUsageViewModel
import proton.android.pass.log.api.PassLogger

@[AndroidEntryPoint RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)]
internal class PasskeyCredentialUsageActivity : FragmentActivity() {

    private val viewModel: PasskeyCredentialUsageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.onUpdateRequest(getPasskeyUsageRequest())

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stateFlow.collectLatest { state ->
                    when (state) {
                        PasskeyCredentialUsageState.NotReady -> Unit
                        PasskeyCredentialUsageState.Cancel -> {
                            onCancelAuthRequest()
                        }

                        is PasskeyCredentialUsageState.Ready -> {
                            onProceedAuthRequest(authResponseJson = state.authResponseJson)
                        }
                    }
                }
            }
        }
    }

    override fun onStop() {
        viewModel.onStop()

        super.onStop()
    }

    @Suppress("ReturnCount")
    private fun getPasskeyUsageRequest(): PasskeyCredentialUsageRequest? {
        val requestJson = intent.getStringExtra(EXTRAS_REQUEST_JSON) ?: run {
            PassLogger.w(TAG, "Passkey usage request does not contain requestJson")
            return null
        }

        val requestOrigin = intent.getStringExtra(EXTRAS_REQUEST_ORIGIN) ?: run {
            PassLogger.w(TAG, "Passkey usage request does not contain requestOrigin")
            return null
        }

        val clientDataHash = intent.getByteArrayExtra(EXTRAS_REQUEST_CLIENT_DATA_HASH)

        val shareId = intent.getStringExtra(EXTRAS_SHARE_ID)?.let(::ShareId) ?: run {
            PassLogger.w(TAG, "Could not get ShareId")
            return null
        }

        val itemId = intent.getStringExtra(EXTRAS_ITEM_ID)?.let(::ItemId) ?: run {
            PassLogger.w(TAG, "Could not get ItemId")
            return null
        }

        val passkeyId = intent.getStringExtra(EXTRAS_PASSKEY_ID)?.let(::PasskeyId) ?: run {
            PassLogger.w(TAG, "Could not get PasskeyId")
            return null
        }

        return PasskeyCredentialUsageRequest(
            requestJson = requestJson,
            requestOrigin = requestOrigin,
            clientDataHash = clientDataHash,
            shareId = shareId,
            itemId = itemId,
            passkeyId = passkeyId
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

        private const val TAG = "PasskeyCredentialUsageActivity"

        private const val EXTRAS_REQUEST_JSON = "REQUEST_JSON"
        private const val EXTRAS_REQUEST_ORIGIN = "REQUEST_ORIGIN"
        private const val EXTRAS_REQUEST_CLIENT_DATA_HASH = "REQUEST_CLIENT_DATA_HASH"
        private const val EXTRAS_SHARE_ID = "SHARE_ID"
        private const val EXTRAS_ITEM_ID = "ITEM_ID_ID"
        private const val EXTRAS_PASSKEY_ID = "PASSKEY_ID"

        internal fun createPasskeyCredentialIntent(
            context: Context,
            origin: String,
            option: BeginGetPublicKeyCredentialOption,
            passkeyItem: PasskeyItem
        ): Intent = Intent(
            context,
            PasskeyCredentialUsageActivity::class.java
        ).apply {
            setPackage(context.packageName)

            putExtra(EXTRAS_REQUEST_ORIGIN, origin)
            putExtra(EXTRAS_REQUEST_JSON, option.requestJson)
            putExtra(EXTRAS_REQUEST_CLIENT_DATA_HASH, option.clientDataHash)
            putExtra(EXTRAS_SHARE_ID, passkeyItem.shareId.id)
            putExtra(EXTRAS_ITEM_ID, passkeyItem.itemId.id)
            putExtra(EXTRAS_PASSKEY_ID, passkeyItem.passkey.id.value)
        }

    }

}
