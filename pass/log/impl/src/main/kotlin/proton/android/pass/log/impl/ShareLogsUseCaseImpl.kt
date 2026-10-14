/*
 * Copyright (c) 2023-2026 Proton AG
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

import android.annotation.SuppressLint
import android.content.Context
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import proton.android.pass.biometry.NeedsBiometricAuth
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.ClassHolder
import proton.android.pass.commonui.api.FileHandler
import proton.android.pass.data.api.usecases.GetPrimaryUserId
import proton.android.pass.log.api.LogExporter
import proton.android.pass.log.api.PassLogger
import proton.android.pass.log.api.ShareLogsConstants
import proton.android.pass.log.api.ShareLogsUseCase
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference
import javax.inject.Inject

class ShareLogsUseCaseImpl @Inject constructor(
    private val logExporter: LogExporter,
    private val getPrimaryUserId: GetPrimaryUserId,
    private val fileHandler: FileHandler,
    private val appDispatchers: AppDispatchers,
    private val needsBiometricAuth: NeedsBiometricAuth
) : ShareLogsUseCase {

    @SuppressLint("LogNotTimber")
    override suspend fun invoke(context: Context): Result<File> = runCatching {
        // Prepare file and URI on IO dispatcher
        val (tempFile, tempFileUri) = withContext(appDispatchers.io) {
            var userId = getPrimaryUserId()
            if (userId != null && needsBiometricAuth().first()) {
                userId = null // do not share private logs in that case
            }

            // Use share subdirectory which is configured in FileProvider
            val shareDir = File(context.cacheDir, "share")
            shareDir.mkdirs()
            val file = File(shareDir, "pass_logs_${System.currentTimeMillis()}.log")
            PassLogger.i(TAG, "Creating share log file: ${file.absolutePath}")

            logExporter.export(destination = file, userId = userId)
            file to file.toURI()
        }

        PassLogger.i(TAG, "Sharing log file: ${tempFile.name} with URI: $tempFileUri")
        fileHandler.shareFileWithEmail(
            contextHolder = ClassHolder(Some(WeakReference(context))),
            uri = tempFileUri,
            mimeType = "text/plain",
            chooserTitle = ShareLogsConstants.CHOOSER_TITLE,
            email = ShareLogsConstants.EMAIL,
            subject = ShareLogsConstants.SUBJECT
        )

        tempFile
    }.onFailure { e ->
        if (e is IOException) {
            PassLogger.w(TAG, "Could not share log file")
            PassLogger.w(TAG, e)
        } else {
            throw e
        }
    }

    companion object {
        private const val TAG = "ShareLogsUseCaseImpl"
    }
}
