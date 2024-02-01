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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.credentials.GetCredentialResponse
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PublicKeyCredential
import androidx.credentials.provider.PendingIntentHandler
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.featurepasskeys.select.presentation.SelectPasskeyActivityViewModel
import proton.android.pass.featurepasskeys.select.presentation.SelectPasskeyRequest
import proton.android.pass.featurepasskeys.select.presentation.State

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
            viewModel.state.collectLatest {
                when (it) {
                    is State.Idle -> {}
                    is State.NoPasskeyFound -> sendResponse(null)
                    is State.SendResponse -> sendResponse(it.response)
                }
            }
        }

        setContent {
            PassTheme {
                Scaffold(
                    modifier = Modifier
                        .background(PassTheme.colors.backgroundStrong)
                        .systemBarsPadding()
                        .imePadding()
                ) {
                    Column(modifier = Modifier.padding(it)) {
                        Text("Fill with passkey")
                        Button(onClick = { viewModel.onButtonClick() }) {
                            Text("Fill")
                        }
                        Button(onClick = { viewModel.clearPasskeys() }) {
                            Text("Clear passkeys")
                        }
                    }
                }
            }
        }
    }

    private fun getRequest(): SelectPasskeyRequest? {
        val extras = intent.extras ?: return null
        if (extras.getString(EXTRAS_REQUEST_TYPE_KEY) != EXTRAS_REQUEST_TYPE_VALUE) return null

        val passKeyId = extras.getString(EXTRAS_PASSKEY_ID) ?: return null

        val request = PendingIntentHandler.retrieveProviderGetCredentialRequest(intent)
        val option = request?.credentialOptions
            ?.first { it is GetPublicKeyCredentialOption }
            ?.let { it as GetPublicKeyCredentialOption }
            ?: return null

        return SelectPasskeyRequest(
            callingAppInfo = request.callingAppInfo,
            callingRequest = option,
            passkeyId = passKeyId
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

        private const val EXTRAS_REQUEST_TYPE_KEY = "GET_PASSKEYS"
        private const val EXTRAS_REQUEST_TYPE_VALUE = "GET_PASSKEYS"
        private const val EXTRAS_PASSKEY_ID = "PASSKEY_ID"

        fun createIntent(context: Context, passkeyId: String): Intent {
            val intent = Intent(context, SelectPasskeyActivity::class.java)
                .setPackage(context.packageName)

            intent.putExtra(EXTRAS_REQUEST_TYPE_KEY, EXTRAS_REQUEST_TYPE_VALUE)
            intent.putExtra(EXTRAS_PASSKEY_ID, passkeyId)

            return intent
        }
    }
}
