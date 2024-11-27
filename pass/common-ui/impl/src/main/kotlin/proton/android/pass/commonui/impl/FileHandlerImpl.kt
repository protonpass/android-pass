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
import androidx.core.content.FileProvider
import proton.android.pass.appconfig.api.AppConfig
import proton.android.pass.commonui.api.FileHandler
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileHandlerImpl @Inject constructor(
    private val appConfig: AppConfig
) : FileHandler {

    private fun createContentUri(context: Context, file: File): Uri = FileProvider.getUriForFile(
        context,
        "${appConfig.applicationId}.fileprovider",
        file
    )

    private fun createIntent(
        action: String,
        mimeType: String,
        extras: Bundle = Bundle()
    ): Intent = Intent(action).apply {
        setType(mimeType)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        putExtras(extras)
    }

    override fun openFile(
        context: Context,
        file: File,
        mimeType: String,
        chooserTitle: String
    ) {
        val contentUri = createContentUri(context, file)
        performFileAction(
            context = context,
            action = Intent.ACTION_VIEW,
            mimeType = mimeType,
            chooserTitle = chooserTitle,
            extras = Bundle().apply {
                putParcelable(Intent.EXTRA_STREAM, contentUri)
            }
        )
    }

    override fun shareFile(
        context: Context,
        file: File,
        mimeType: String,
        chooserTitle: String
    ) {
        val contentUri = createContentUri(context, file)
        performFileAction(
            context = context,
            action = Intent.ACTION_SEND,
            mimeType = mimeType,
            chooserTitle = chooserTitle,
            extras = Bundle().apply {
                putParcelable(Intent.EXTRA_STREAM, contentUri)
            }
        )
    }

    override fun shareFileWithEmail(
        context: Context,
        file: File,
        mimeType: String,
        chooserTitle: String,
        email: String,
        subject: String
    ) {
        val contentUri = createContentUri(context, file)
        performFileAction(
            context = context,
            action = Intent.ACTION_SEND,
            mimeType = mimeType,
            chooserTitle = chooserTitle,
            extras = Bundle().apply {
                putStringArray(Intent.EXTRA_EMAIL, arrayOf(email))
                putString(Intent.EXTRA_SUBJECT, subject)
                putParcelable(Intent.EXTRA_STREAM, contentUri)
            }
        )
    }

    override fun performFileAction(
        context: Context,
        action: String,
        mimeType: String,
        chooserTitle: String,
        extras: Bundle
    ) {
        val intent = createIntent(action, mimeType, extras)
        val chooserIntent = Intent.createChooser(intent, chooserTitle)
        context.startActivity(chooserIntent)
    }
}
