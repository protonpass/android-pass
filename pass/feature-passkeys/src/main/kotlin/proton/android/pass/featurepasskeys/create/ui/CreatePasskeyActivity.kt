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

package proton.android.pass.featurepasskeys.create.ui

import android.app.Activity
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
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CreatePublicKeyCredentialResponse
import androidx.credentials.provider.PendingIntentHandler
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.featurepasskeys.create.presentation.CreatePasskeyActivityViewModel
import proton.android.pass.featurepasskeys.create.presentation.CreatePasskeyRequest
import proton.android.pass.featurepasskeys.create.presentation.State
import proton.android.pass.log.api.PassLogger

@AndroidEntryPoint
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
class CreatePasskeyActivity : FragmentActivity() {

    private val viewModel: CreatePasskeyActivityViewModel by viewModels()

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
                    is State.Close -> sendResponse(null)
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
                        Text("Create Passkey")
                        Button(onClick = { viewModel.onButtonClick() }) {
                            Text("Create")
                        }
                        Button(onClick = { viewModel.clearPasskeys() }) {
                            Text("Clear passkeys")
                        }
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

    private fun sendResponse(responseJson: String?) {
        if (responseJson != null) {
            val response = CreatePublicKeyCredentialResponse(responseJson)

            val responseIntent = Intent()
            PendingIntentHandler.setCreateCredentialResponse(responseIntent, response)
            setResult(Activity.RESULT_OK, responseIntent)
        } else {
            setResult(Activity.RESULT_CANCELED)
        }

        finish()
    }

    companion object {
        private const val TAG = "CreatePasskeyActivity"
    }
}
