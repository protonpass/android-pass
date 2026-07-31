/*
 * Copyright (c) 2026 Proton AG
 * This file is part of Proton Pass.
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

package proton.android.pass.features.credentials.passwords.selection.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.RequiresApi
import androidx.credentials.provider.BeginGetCredentialRequest
import androidx.credentials.provider.BeginGetPasswordOption
import androidx.credentials.provider.CallingAppInfo
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PasswordCredentialSelectionActivityRequestTest {

    // PackageManager.GET_SIGNING_CERTIFICATES and android.content.pm.SigningInfo were both
    // introduced in API 28; CallingAppInfo requires a SigningInfo instance, so this scenario
    // cannot be exercised below that floor even though the app's minSdk is 27.
    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.P)
    @Test
    fun extractsCallingAppInfoFromFrameworkBeginGetRequest() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val signingInfo = context.packageManager
            .getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            .signingInfo
            ?: error("Test app must have signing information")
        val callingAppInfo = CallingAppInfo(
            packageName = context.packageName,
            signingInfo = signingInfo
        )
        val beginRequest = BeginGetCredentialRequest(
            beginGetCredentialOptions = listOf(
                BeginGetPasswordOption(
                    allowedUserIds = emptySet(),
                    candidateQueryData = Bundle(),
                    id = "password-option"
                )
            ),
            callingAppInfo = callingAppInfo
        )
        val intent = createPendingIntent(beginRequest)

        val extracted = extractPasswordCallingAppInfo(intent)

        assertThat(extracted?.packageName).isEqualTo(callingAppInfo.packageName)
        assertThat(extracted?.signingInfoCompat).isEqualTo(callingAppInfo.signingInfoCompat)
    }

    private fun createPendingIntent(beginRequest: BeginGetCredentialRequest): Intent = Intent().apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            putExtra(BEGIN_GET_CREDENTIAL_REQUEST_EXTRA, Api34.toFrameworkRequest(beginRequest))
        } else {
            putExtra(BEGIN_GET_CREDENTIAL_REQUEST_EXTRA, BeginGetCredentialRequest.asBundle(beginRequest))
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private object Api34 {

        fun toFrameworkRequest(beginRequest: BeginGetCredentialRequest): Parcelable {
            val bundle = BeginGetCredentialRequest.asBundle(beginRequest)
            val key = bundle.keySet().single()
            return requireNotNull(
                bundle.getParcelable(key, android.service.credentials.BeginGetCredentialRequest::class.java)
            )
        }
    }

    private companion object {
        const val BEGIN_GET_CREDENTIAL_REQUEST_EXTRA =
            "android.service.credentials.extra.BEGIN_GET_CREDENTIAL_REQUEST"
    }
}
