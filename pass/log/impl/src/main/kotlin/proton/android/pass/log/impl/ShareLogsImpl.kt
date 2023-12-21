/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.log.impl

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import proton.android.pass.appconfig.api.AppConfig
import proton.android.pass.log.api.ShareLogs
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShareLogsImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appConfig: AppConfig
): ShareLogs {
    override suspend fun createIntent(): Intent? = withContext(Dispatchers.IO) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("pass@protonme.zendesk.com"))
        intent.putExtra(Intent.EXTRA_SUBJECT, "Proton Pass: Share Logs")
        val cacheFile = File(context.cacheDir, "logs/pass.log")

        val contentUri: Uri = FileProvider.getUriForFile(
            context,
            "${appConfig.applicationId}.fileprovider",
            cacheFile
        )
        intent.putExtra(Intent.EXTRA_STREAM, contentUri)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        Intent.createChooser(intent, "Share log")
    }
}
