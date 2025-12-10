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

package proton.android.pass.autofill.sample.passwordcredentials

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.PasswordCredential
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import proton.android.pass.autofill.sample.databinding.ActivityPasswordCredentialsBinding
import proton.android.pass.autofill.sample.utils.enableEdgeToEdgeProtonPassCompat

internal class PasswordCredentialsActivity : AppCompatActivity() {

    private val credentialManager by lazy {
        CredentialManager.create(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityPasswordCredentialsBinding.inflate(layoutInflater)
        enableEdgeToEdgeProtonPassCompat(view = binding.root)

        binding.registerButton.setOnClickListener {
            updateStatusMessage(binding, "")

            createPasswordCredential(
                email = binding.loginEditText.text.toString(),
                password = binding.passwordEditText.text.toString(),
                onFailure = { failureMessage ->
                    updateStatusMessage(binding, failureMessage, Color.RED)
                },
                onSuccess = { successMessage ->
                    updateStatusMessage(binding, successMessage, Color.GREEN)
                }
            )
        }

        binding.authenticateButton.setOnClickListener {
            updateStatusMessage(binding, "")

            getPasswordCredential(
                onFailure = { failureMessage ->
                    updateStatusMessage(binding, failureMessage, Color.RED)
                },
                onSuccess = { successMessage ->
                    updateStatusMessage(binding, successMessage, Color.GREEN)
                }
            )
        }

        setContentView(binding.root)
    }

    private fun updateStatusMessage(
        binding: ActivityPasswordCredentialsBinding,
        message: String,
        color: Int? = null
    ) {
        binding.statusTextView.apply {
            text = message
            color?.let(::setTextColor)
        }
    }

    private fun createPasswordCredential(
        email: String,
        password: String,
        onFailure: (String) -> Unit,
        onSuccess: (String) -> Unit
    ) {
        lifecycleScope.launch {
            runCatching {
                credentialManager.createCredential(
                    context = this@PasswordCredentialsActivity,
                    request = CreatePasswordRequest(
                        id = email,
                        password = password,
                        isAutoSelectAllowed = false,
                        preferImmediatelyAvailableCredentials = false,
                        origin = null
                    )
                )
            }.onFailure { error ->
                Log.w(TAG, "Error creating password credential")
                Log.w(TAG, error)

                buildString {
                    append("CREATION FAILED")
                    append("\n\n")
                    append("Error creating password credential: ${error.message}")
                }.also(onFailure)

            }.onSuccess { createCredentialResponse ->
                Log.i(TAG, "Password credential created: $createCredentialResponse")

                buildString {
                    append("CREATION SUCCESSFUL")
                    append("\n\n")
                    append("Password credential created type: ${createCredentialResponse.type}")
                }.also(onSuccess)
            }
        }
    }

    private fun getPasswordCredential(onFailure: (String) -> Unit, onSuccess: (String) -> Unit) {
        lifecycleScope.launch {
            runCatching {
                credentialManager.getCredential(
                    context = this@PasswordCredentialsActivity,
                    request = GetCredentialRequest(
                        credentialOptions = listOf(
                            GetPasswordOption()
                        )
                    )
                )
            }.onFailure { error ->
                Log.w(TAG, "Error getting password credential")
                Log.w(TAG, error)

                buildString {
                    append("ACCESS DENIED")
                    append("\n\n")
                    append("Error getting password credential: ${error.message}")
                }.also(onFailure)
            }.onSuccess { getCredentialResponse ->
                when (val credential = getCredentialResponse.credential) {
                    is PasswordCredential -> {
                        Log.i(TAG, "Password credential retrieved: $credential")

                        buildString {
                            append("ACCESS GRANTED")
                            append("\n\n")
                            append("Email or Username: ${credential.id}")
                            append("\n")
                            append("Password: ${credential.password}")
                        }.also(onSuccess)
                    }

                    else -> {
                        Log.w(TAG, "Invalid credential type")

                        buildString {
                            append("ACCESS DENIED")
                            append("\n\n")
                            append("Invalid credential type: $credential")
                        }.also(onFailure)
                    }
                }
            }
        }
    }

    private companion object {

        private const val TAG = "PasswordCredentialsActivity"

    }

}
