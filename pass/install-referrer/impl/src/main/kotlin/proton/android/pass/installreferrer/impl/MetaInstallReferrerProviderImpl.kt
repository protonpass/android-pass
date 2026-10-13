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

package proton.android.pass.installreferrer.impl

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import proton.android.pass.appconfig.api.AppConfig
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.log.api.PassLogger
import proton.android.pass.telemetry.api.MetaInstallReferrer
import proton.android.pass.telemetry.api.MetaInstallReferrerProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MetaInstallReferrerProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appConfig: AppConfig,
    private val appDispatchers: AppDispatchers
) : MetaInstallReferrerProvider {

    private var cachedReferrer: MetaInstallReferrer? = null

    override suspend fun getMetaInstallReferrer(): MetaInstallReferrer {
        cachedReferrer?.let { return it }
        return withContext(appDispatchers.io) {
            queryProviders()
        }.also { cachedReferrer = it }
    }

    private fun queryProviders(): MetaInstallReferrer {
        val metaAppId = appConfig.metaAppId
        if (metaAppId.isNullOrBlank()) {
            PassLogger.i(TAG, "Meta install referrer skipped (no Meta App ID configured)")
            return MetaInstallReferrer()
        }
        return INSTALL_REFERRER_PROVIDERS.firstNotNullOfOrNull { authority ->
            queryProvider(authority, metaAppId)
        } ?: MetaInstallReferrer()
    }

    private fun queryProvider(authority: String, metaAppId: String): MetaInstallReferrer? {
        return runCatching {
            val resolved = context.packageManager.resolveContentProvider(authority, 0)
                ?: return@runCatching null
            val uri = Uri.parse("content://${resolved.authority}/$metaAppId")
            context.contentResolver.query(uri, PROJECTION, null, null, null)?.use { cursor ->
                if (!cursor.moveToFirst()) return@use null
                val installReferrer = cursor.getColumnIndex(INSTALL_REFERRER_COLUMN)
                    .takeIf { it >= 0 }
                    ?.let(cursor::getString)
                    ?.takeUnless { it.isBlank() }
                val actualTimestamp = cursor.getColumnIndex(ACTUAL_TIMESTAMP_COLUMN)
                    .takeIf { it >= 0 && !cursor.isNull(it) }
                    ?.let(cursor::getLong)
                val isClickThrough = cursor.getColumnIndex(IS_CT_COLUMN)
                    .takeIf { it >= 0 && !cursor.isNull(it) }
                    ?.let { cursor.getInt(it) == IS_CLICK_THROUGH }
                installReferrer?.let {
                    MetaInstallReferrer(
                        encryptedData = it,
                        attributionTimestampSeconds = actualTimestamp,
                        isClickThrough = isClickThrough
                    )
                }
            }
        }.onFailure {
            PassLogger.w(TAG, "Error querying $authority")
            PassLogger.w(TAG, it)
        }.getOrNull()
    }

    private companion object {
        const val TAG = "MetaInstallReferrerProvider"
        val INSTALL_REFERRER_PROVIDERS = listOf(
            "com.facebook.katana.provider.InstallReferrerProvider",
            "com.instagram.contentprovider.InstallReferrerProvider",
            "com.facebook.lite.provider.InstallReferrerProvider"
        )
        val PROJECTION = arrayOf(INSTALL_REFERRER_COLUMN, IS_CT_COLUMN, ACTUAL_TIMESTAMP_COLUMN)
        const val INSTALL_REFERRER_COLUMN = "install_referrer"
        const val IS_CT_COLUMN = "is_ct"
        const val ACTUAL_TIMESTAMP_COLUMN = "actual_timestamp"
        const val IS_CLICK_THROUGH = 1
    }
}
