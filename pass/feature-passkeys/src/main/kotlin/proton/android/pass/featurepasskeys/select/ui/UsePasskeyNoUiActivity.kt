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
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.credentials.GetCredentialResponse
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PublicKeyCredential
import androidx.credentials.provider.PendingIntentHandler
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.PasskeyId
import proton.android.pass.domain.ShareId
import proton.android.pass.featurepasskeys.select.SelectPasskeyUtils
import proton.android.pass.featurepasskeys.select.presentation.UsePasskeyNoUiRequest
import proton.android.pass.featurepasskeys.select.presentation.UsePasskeyNoUiViewModel
import proton.android.pass.featurepasskeys.select.presentation.UsePasskeyState
import proton.android.pass.log.api.PassLogger

@AndroidEntryPoint
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
class UsePasskeyNoUiActivity : FragmentActivity() {

    private val viewModel: UsePasskeyNoUiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val request = getRequest() ?: run {
            PassLogger.w(TAG, "Could not get request")
            sendResponse(null)
            return
        }

        viewModel.setRequest(request)
        lifecycleScope.launch {
            viewModel.state.collectLatest(::onStateReceived)
        }
    }

    override fun onStop() {
        viewModel.onStop()
        super.onStop()
    }

    private fun onStateReceived(state: UsePasskeyState) {
        when (state) {
            UsePasskeyState.Idle -> {}
            UsePasskeyState.Cancel -> sendResponse(null)
            is UsePasskeyState.SendResponse -> sendResponse(state.response)
        }
    }

    @Suppress("CyclomaticComplexMethod", "ComplexMethod", "ReturnCount")
    private fun getRequest(): UsePasskeyNoUiRequest? {
        val shareId = intent.getStringExtra(EXTRAS_SHARE_ID)?.let { ShareId(it) } ?: run {
            PassLogger.w(TAG, "Could not get ShareId")
            return null
        }
        val itemId = intent.getStringExtra(EXTRAS_ITEM_ID)?.let { ItemId(it) } ?: run {
            PassLogger.w(TAG, "Could not get ItemId")
            return null
        }
        val passkeyId = intent.getStringExtra(EXTRAS_PASSKEY_ID)?.let { PasskeyId(it) } ?: run {
            PassLogger.w(TAG, "Could not get PasskeyId")
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
                PassLogger.w(TAG, "Request does not contain any GetPublicKeyCredentialOption")
                return null
            }

        val clientDataHash = option.clientDataHash ?: run {
            PassLogger.w(TAG, "Request does not contain ClientDataHash")
            return null
        }

        val origin = SelectPasskeyUtils.getDomainFromRequest(request) ?: run {
            PassLogger.w(TAG, "Request does not contain origin")
            return null
        }

        return UsePasskeyNoUiRequest(
            origin = origin,
            requestJson = option.requestJson,
            shareId = shareId,
            itemId = itemId,
            passkeyId = passkeyId,
            clientDataHash = clientDataHash
        )
    }

    private fun sendResponse(responseJson: String?) {
        if (responseJson != null) {
            val response = GetCredentialResponse(PublicKeyCredential(responseJson))

            val responseIntent = Intent()
            PendingIntentHandler.setGetCredentialResponse(responseIntent, response)
            setResult(Activity.RESULT_OK, responseIntent)
        } else {
            setResult(Activity.RESULT_CANCELED)
        }

        finish()
    }

    companion object {

        private const val TAG = "UsePasskeyNoUiActivity"

        private const val EXTRAS_SHARE_ID = "SHARE_ID"
        private const val EXTRAS_ITEM_ID = "ITEM_ID_ID"
        private const val EXTRAS_PASSKEY_ID = "PASSKEY_ID"

        fun newIntent(
            context: Context,
            shareId: ShareId,
            itemId: ItemId,
            passkeyId: PasskeyId
        ) = Intent(context, UsePasskeyNoUiActivity::class.java).apply {
            setPackage(context.packageName)

            putExtra(EXTRAS_SHARE_ID, shareId.id)
            putExtra(EXTRAS_ITEM_ID, itemId.id)
            putExtra(EXTRAS_PASSKEY_ID, passkeyId.value)
        }
    }

}
