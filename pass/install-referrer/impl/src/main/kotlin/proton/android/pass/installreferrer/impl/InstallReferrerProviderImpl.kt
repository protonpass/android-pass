/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.installreferrer.impl

import android.content.Context
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import proton.android.pass.log.api.PassLogger
import proton.android.pass.telemetry.api.InstallReferrerProvider
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.seconds

@Singleton
class InstallReferrerProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : InstallReferrerProvider {

    private var cachedReferrer: String? = null

    override suspend fun getInstallReferrer(): String? {
        cachedReferrer?.let { return it }
        return withTimeoutOrNull(TIMEOUT) {
            fetchReferrer()
        }.also { cachedReferrer = it }
    }

    private suspend fun fetchReferrer(): String? = suspendCancellableCoroutine { continuation ->
        val client = InstallReferrerClient.newBuilder(context).build()

        continuation.invokeOnCancellation {
            runCatching { client.endConnection() }
        }

        client.startConnection(object : InstallReferrerStateListener {
            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                val referrer = when (responseCode) {
                    InstallReferrerClient.InstallReferrerResponse.OK -> {
                        runCatching {
                            client.installReferrer.installReferrer
                        }.onFailure {
                            PassLogger.w(TAG, "Error reading install referrer")
                            PassLogger.w(TAG, it)
                        }.getOrNull()
                    }
                    InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                        PassLogger.i(TAG, "Install referrer not supported (no Play Store)")
                        null
                    }
                    InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                        PassLogger.i(TAG, "Install referrer service unavailable")
                        null
                    }
                    else -> {
                        PassLogger.w(TAG, "Install referrer unknown response: $responseCode")
                        null
                    }
                }
                runCatching { client.endConnection() }
                if (continuation.isActive) {
                    continuation.resume(referrer)
                }
            }

            override fun onInstallReferrerServiceDisconnected() {
                PassLogger.w(TAG, "Install referrer service disconnected")
                if (continuation.isActive) {
                    continuation.resume(null)
                }
            }
        })
    }

    private companion object {
        const val TAG = "InstallReferrerProviderImpl"
        val TIMEOUT = 5.seconds
    }
}
