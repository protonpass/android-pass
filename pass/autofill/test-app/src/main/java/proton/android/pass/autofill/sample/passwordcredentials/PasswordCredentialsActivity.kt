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

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import proton.android.pass.autofill.sample.databinding.ActivityPasswordCredentialsBinding

internal class PasswordCredentialsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityPasswordCredentialsBinding.inflate(layoutInflater)

        binding.loginButton.setOnClickListener {
            val email = binding.loginEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            createPasswordCredential(email, password)
        }

        setContentView(binding.root)
    }

    private fun createPasswordCredential(email: String, password: String) {
        val credentialManager = CredentialManager.create(this)

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
            }.onSuccess { createCredentialResponse ->
                Log.i(TAG, "Credential created: $createCredentialResponse")
            }
        }
    }

    private companion object {

        private const val TAG = "PasswordCredentialsActivity"

    }

}
