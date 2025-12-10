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

package proton.android.pass.autofill.sample.passkeys

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PublicKeyCredential
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import proton.android.pass.autofill.sample.databinding.ActivityPasskeysBinding
import proton.android.pass.autofill.sample.utils.enableEdgeToEdgeProtonPassCompat

class PasskeysActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityPasskeysBinding.inflate(layoutInflater)
        enableEdgeToEdgeProtonPassCompat(view = binding.root)

        binding.registerButton.setOnClickListener {
            createPasskey(binding.usernameEditText.text.toString()) { res ->
                binding.statusTextView.text = res
            }
        }

        binding.authenticateButton.setOnClickListener {
            authenticateWithPasskey { res ->
                binding.statusTextView.text = res
            }
        }

        setContentView(binding.root)
    }

    private fun createPasskey(username: String, cb: (String) -> Unit) {
        val ctx = this
        lifecycleScope.launch {
            runCatching {
                val manager = CredentialManager.create(ctx)
                val requestJson = createRequestJson(domain = DOMAIN, username = username)

                manager.createCredential(
                    context = ctx,
                    request = CreatePublicKeyCredentialRequest(
                        requestJson = requestJson,
                        clientDataHash = null,
                        preferImmediatelyAvailableCredentials = false,
                        origin = null,
                        isAutoSelectAllowed = false
                    )
                )
            }.onSuccess { credential ->
                Log.i(TAG, "Credential created: $credential")
                cb("Success")
            }.onFailure {
                Log.w(TAG, "Error creating credential")
                Log.w(TAG, it)
                cb(it.message ?: "Error")
            }

        }
    }

    private fun authenticateWithPasskey(cb: (String) -> Unit) {
        val ctx = this
        lifecycleScope.launch {
            runCatching {
                val manager = CredentialManager.create(ctx)
                manager.getCredential(
                    context = ctx,
                    request = GetCredentialRequest(
                        credentialOptions = listOf(
                            GetPublicKeyCredentialOption(
                                requestJson = getRequestJson(DOMAIN)
                            )
                        )
                    )
                )

            }.onSuccess { credential ->
                Log.i(TAG, "Credential retrieved: $credential")
                when (val cred = credential.credential) {
                    is PublicKeyCredential -> {
                        val res = buildString {
                            append("Type: ${cred.type}\n")
                            append("JSON: ${cred.authenticationResponseJson}\n")
                            append("Data: ${cred.data}")
                        }
                        cb(res)
                    }
                    else -> {
                        Log.w(TAG, "Invalid credential type")
                        cb("Invalid credential type")
                    }
                }
            }.onFailure {
                Log.w(TAG, "Error creating credential")
                Log.w(TAG, it)
                cb(it.message ?: "Error")
            }
        }
    }

    companion object {
        private const val TAG = "PasskeysActivity"
        private const val DOMAIN = "protonpass.github.io"

        @Suppress("MaxLineLength")
        private fun getRequestJson(domain: String) =
            "{\"allowCredentials\":[],\"challenge\":\"xh0nvw0T_PQFx-FuNuxQXuJYgAvIdjMv5DlTZ_i5vzaQi8QzbzybTTvwTjRmBX0Oml3la9kkXCxMk12_605U4g\",\"rpId\":\"$domain\",\"userVerification\":\"preferred\"}"

        @Suppress("MaxLineLength")
        private fun createRequestJson(domain: String, username: String) =
            "{\"attestation\":\"none\",\"authenticatorSelection\":{\"residentKey\":\"preferred\",\"userVerification\":\"preferred\"},\"challenge\":\"qEb-L-3-cp65J8-VJlZACfzVeB98j2AUY-JexPTBiqBrLyec9XozWpy3SHo84UTtEAztuUVuRCwg0aF9zaE1JA\",\"excludeCredentials\":[],\"extensions\":{\"credProps\":true},\"pubKeyCredParams\":[{\"alg\":-7,\"type\":\"public-key\"},{\"alg\":-257,\"type\":\"public-key\"}],\"rp\":{\"id\":\"$domain\",\"name\":\"$domain\"},\"user\":{\"displayName\":\"$username\",\"id\":\"Y21WeVpYSmw\",\"name\":\"$username\"}}"
    }

}
