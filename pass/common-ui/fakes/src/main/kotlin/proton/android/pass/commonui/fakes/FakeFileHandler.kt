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

package proton.android.pass.commonui.fakes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import proton.android.pass.commonui.api.ClassHolder
import proton.android.pass.commonui.api.FileHandler
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeFileHandler @Inject constructor() : FileHandler {
    override suspend fun shareFile(
        contextHolder: ClassHolder<Context>,
        fileTitle: String,
        uri: URI,
        mimeType: String,
        chooserTitle: String
    ) {
        // no-op
    }

    override fun shareFileWithEmail(
        contextHolder: ClassHolder<Context>,
        uri: URI,
        mimeType: String,
        chooserTitle: String,
        email: String,
        subject: String
    ) {
        // no-op
    }

    override fun openFile(
        contextHolder: ClassHolder<Context>,
        uri: URI,
        mimeType: String,
        chooserTitle: String
    ) {
        // no-op
    }

    override fun performFileAction(
        contextHolder: ClassHolder<Context>,
        intent: Intent,
        chooserTitle: String,
        extras: Bundle
    ) {
        // no-op
    }
}
