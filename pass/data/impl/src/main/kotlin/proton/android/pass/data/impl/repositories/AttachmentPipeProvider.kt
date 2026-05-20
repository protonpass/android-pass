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

package proton.android.pass.data.impl.repositories

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.files.api.FilesDirectories
import proton.android.pass.files.api.FileUriGenerator.Companion.ATTACHMENT_PIPE_MIME_PARAM
import proton.android.pass.files.api.FileUriGenerator.Companion.ATTACHMENT_PIPE_PATH
import proton.android.pass.log.api.PassLogger
import android.system.ErrnoException
import android.system.OsConstants
import java.io.DataInputStream
import java.io.EOFException
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

class AttachmentPipeProvider : ContentProvider(), ContentProvider.PipeDataWriter<File> {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AttachmentPipeProviderEntryPoint {
        fun encryptionContextProvider(): EncryptionContextProvider
    }

    private lateinit var appContext: Context
    private lateinit var encryptionContextProvider: EncryptionContextProvider

    override fun onCreate(): Boolean {
        appContext = checkNotNull(context) { "ContentProvider context is null" }
        encryptionContextProvider = EntryPointAccessors.fromApplication(
            appContext,
            AttachmentPipeProviderEntryPoint::class.java
        ).encryptionContextProvider()
        return true
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        val segments = uri.pathSegments
        if (segments.size != PATH_SEGMENT_COUNT || segments[0] != ATTACHMENT_PIPE_PATH) {
            throw FileNotFoundException("Invalid attachment URI: $uri")
        }
        val (_, userId, shareId, itemId, persistentId) = segments
        val pathSegments = listOf(userId, shareId, itemId, persistentId)
        if (pathSegments.any { it.contains('/') || it.contains("..") || it.contains(File.separator) }) {
            throw FileNotFoundException("Path traversal detected in attachment URI")
        }
        val encryptedFile = File(
            appContext.filesDir,
            "${FilesDirectories.AttachmentsEnc.value}/$userId/$shareId/$itemId/$persistentId"
        )
        val mimeType = uri.getQueryParameter(ATTACHMENT_PIPE_MIME_PARAM) ?: "*/*"
        return openPipeHelper(uri, mimeType, null, encryptedFile, this)
    }

    override fun writeDataToPipe(
        output: ParcelFileDescriptor,
        uri: Uri,
        mimeType: String,
        opts: Bundle?,
        encryptedFile: File?
    ) {
        val file = encryptedFile ?: return
        ParcelFileDescriptor.AutoCloseOutputStream(output).use { outputStream ->
            runCatching {
                DataInputStream(file.inputStream().buffered()).use { dis ->
                    encryptionContextProvider.withEncryptionContext {
                        try {
                            while (true) {
                                val len = dis.readInt()
                                check(len in 1..MAX_ENCRYPTED_CHUNK_SIZE) {
                                    "Corrupt cache: invalid chunk length $len — stale format?"
                                }
                                val encBytes = ByteArray(len)
                                dis.readFully(encBytes)
                                outputStream.write(decrypt(EncryptedByteArray(encBytes)))
                            }
                        } catch (_: EOFException) {}
                    }
                }
            }.onFailure { e ->
                when {
                    e is IOException && (e.cause as? ErrnoException)?.errno == OsConstants.EPIPE ->
                        return@use
                    e is FileNotFoundException -> {
                        PassLogger.w(TAG, "Encrypted attachment not cached, re-download needed: ${file.path}")
                        runCatching { output.closeWithError("attachment not cached") }
                        return@use
                    }
                    else -> {
                        PassLogger.e(TAG, e, "Failed to stream decrypted attachment")
                        runCatching { output.closeWithError(e.message ?: "decryption failed") }
                        file.delete()
                        return@use
                    }
                }
            }
        }
    }

    override fun getType(uri: Uri): String? = uri.getQueryParameter(ATTACHMENT_PIPE_MIME_PARAM)

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0

    companion object {
        private const val TAG = "AttachmentPipeProvider"
        private const val MAX_ENCRYPTED_CHUNK_SIZE = 10 * 1024 * 1024 + 64
        private const val PATH_SEGMENT_COUNT = 5
    }
}
