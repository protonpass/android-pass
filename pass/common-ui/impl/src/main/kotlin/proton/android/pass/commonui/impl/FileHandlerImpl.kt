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

package proton.android.pass.commonui.impl

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import kotlinx.coroutines.withContext
import me.proton.core.util.kotlin.takeIfNotBlank
import proton.android.pass.appconfig.api.AppConfig
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.commonui.api.ClassHolder
import proton.android.pass.commonui.api.FileHandler
import proton.android.pass.files.api.CacheDirectories
import proton.android.pass.log.api.PassLogger
import java.io.File
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileHandlerImpl @Inject constructor(
    private val appDispatchers: AppDispatchers,
    private val appConfig: AppConfig
) : FileHandler {

    private fun createContentUri(contextHolder: ClassHolder<Context>, file: File): Uri {
        val context = contextHolder.get().value()
            ?: throw IllegalStateException("Could not get context")
        return FileProvider.getUriForFile(
            context,
            "${appConfig.applicationId}.fileprovider",
            file
        )
    }

    private fun isContentUri(uri: URI?): Boolean = uri?.scheme == "content"

    private fun URI.toContentUri(contextHolder: ClassHolder<Context>): Uri = if (isContentUri(this)) {
        this.toString().toUri()
    } else {
        createContentUri(contextHolder, File(this))
    }

    private fun Uri.hasExtension(): Boolean = lastPathSegment?.contains(".") == true

    private fun String.hasExtension(): Boolean = contains(".") && substringAfterLast(".").isNotEmpty()

    private fun createTempFile(
        contextHolder: ClassHolder<Context>,
        fileTitle: String,
        contentUri: Uri,
        mimeType: String
    ): Uri {
        val context = contextHolder.get().value()
            ?: throw IllegalStateException("Could not get context")
        val cacheFolder = File(context.cacheDir, CacheDirectories.Share.value)
        if (!cacheFolder.exists()) cacheFolder.mkdirs()
        val fileName = fileTitle.takeIfNotBlank() ?: "share_file"
        val renamedFileName = if (!fileName.hasExtension()) {
            val extension = getExtensionFromMimeType(mimeType)
            "$fileName.$extension"
        } else {
            fileName
        }
        val renamedFile = File(cacheFolder, renamedFileName)
        context.contentResolver.openInputStream(contentUri)?.use { input ->
            renamedFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return FileProvider.getUriForFile(
            context,
            "${appConfig.applicationId}.fileprovider",
            renamedFile
        )
    }

    private fun getExtensionFromMimeType(mimeType: String): String =
        MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "bin"

    override fun openFile(
        contextHolder: ClassHolder<Context>,
        uri: URI,
        mimeType: String,
        chooserTitle: String
    ) {
        val contentUri = uri.toContentUri(contextHolder)
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(contentUri, mimeType)
        performFileAction(contextHolder, intent, chooserTitle)
    }

    override suspend fun shareFile(
        contextHolder: ClassHolder<Context>,
        fileTitle: String,
        uri: URI,
        mimeType: String,
        chooserTitle: String
    ) {
        val contentUri = uri.toContentUri(contextHolder)
        val shareContentUri = if (contentUri.hasExtension()) {
            contentUri
        } else {
            withContext(appDispatchers.io) {
                createTempFile(contextHolder, fileTitle, contentUri, mimeType)
            }
        }
        val intent = Intent(Intent.ACTION_SEND)
            .setDataAndType(shareContentUri, mimeType)
        val bundle = Bundle().apply {
            putParcelable(Intent.EXTRA_STREAM, shareContentUri)
        }
        performFileAction(
            contextHolder = contextHolder,
            intent = intent,
            chooserTitle = chooserTitle,
            extras = bundle
        )
    }

    override fun shareFileWithEmail(
        contextHolder: ClassHolder<Context>,
        uri: URI,
        mimeType: String,
        chooserTitle: String,
        email: String,
        subject: String
    ) {
        val contentUri = uri.toContentUri(contextHolder)
        val intent = Intent(Intent.ACTION_SEND)
            .setType(mimeType)
        val bundle = Bundle().apply {
            putStringArray(Intent.EXTRA_EMAIL, arrayOf(email))
            putString(Intent.EXTRA_SUBJECT, subject)
            putParcelable(Intent.EXTRA_STREAM, contentUri)
        }
        performFileAction(
            contextHolder = contextHolder,
            intent = intent,
            chooserTitle = chooserTitle,
            extras = bundle
        )
    }

    override fun performFileAction(
        contextHolder: ClassHolder<Context>,
        intent: Intent,
        chooserTitle: String,
        extras: Bundle
    ) {
        val intentWithExtras = intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .putExtras(extras)
        val chooserIntent = Intent.createChooser(intentWithExtras, chooserTitle)
        runCatching {
            contextHolder.get().value()?.startActivity(chooserIntent)
                ?: throw IllegalStateException("Could not get context")
        }.onFailure {
            PassLogger.w(TAG, "Could not start activity for intent")
        }
    }

    companion object {
        private const val TAG = "FileHandlerImpl"
    }
}
