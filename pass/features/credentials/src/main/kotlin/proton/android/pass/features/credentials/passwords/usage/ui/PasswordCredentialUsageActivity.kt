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

package proton.android.pass.features.credentials.passwords.usage.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
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
import kotlinx.coroutines.launch
import proton.android.pass.domain.credentials.PasswordCredentialItem
import proton.android.pass.features.credentials.passwords.usage.presentation.PasswordCredentialUsageRequest
import proton.android.pass.features.credentials.passwords.usage.presentation.PasswordCredentialUsageState
import proton.android.pass.features.credentials.passwords.usage.presentation.PasswordCredentialUsageViewModel
import proton.android.pass.log.api.PassLogger

@[AndroidEntryPoint RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)]
internal class PasswordCredentialUsageActivity : FragmentActivity() {

    private val viewModel: PasswordCredentialUsageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.onUpdateRequest(getPasswordUsageRequest())

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stateFlow.collect { state ->
                    when (state) {
                        PasswordCredentialUsageState.NotReady -> Unit
                        PasswordCredentialUsageState.Cancel -> {
                            onCancelAuthRequest()
                        }

                        is PasswordCredentialUsageState.Ready -> {
                            onProceedAuthRequest(id = state.id, password = state.password)
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

    private fun getPasswordUsageRequest(): PasswordCredentialUsageRequest? {
        val username = intent.getStringExtra(EXTRAS_CREDENTIAL_USERNAME) ?: run {
            PassLogger.w(TAG, "Password usage request does not contain username")
            return null
        }

        val encryptedPassword = intent.getStringExtra(EXTRAS_CREDENTIAL_PASSWORD) ?: run {
            PassLogger.w(TAG, "Password usage request does not contain password")
            return null
        }

        return PasswordCredentialUsageRequest(
            username = username,
            encryptedPassword = encryptedPassword
        )
    }

    private fun onCancelAuthRequest() {
        setResult(RESULT_CANCELED)

        finish()
    }

    private fun onProceedAuthRequest(id: String, password: String) {
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

        private const val TAG = "PasswordCredentialUsageActivity"

        private const val EXTRAS_CREDENTIAL_USERNAME = "CREDENTIAL_USERNAME"
        private const val EXTRAS_CREDENTIAL_PASSWORD = "CREDENTIAL_PASSWORD"

        internal fun createPasswordCredentialIntent(
            context: Context,
            passwordCredentialItem: PasswordCredentialItem
        ): Intent = Intent(
            context,
            PasswordCredentialUsageActivity::class.java
        ).apply {
            setPackage(context.packageName)

            putExtra(EXTRAS_CREDENTIAL_USERNAME, passwordCredentialItem.username)
            putExtra(EXTRAS_CREDENTIAL_PASSWORD, passwordCredentialItem.encryptedPassword)
        }

    }

}
