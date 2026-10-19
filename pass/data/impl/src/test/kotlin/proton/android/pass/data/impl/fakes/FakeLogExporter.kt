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

package proton.android.pass.data.impl.fakes

import proton.android.pass.domain.UserId
import proton.android.pass.log.api.LogExporter
import java.io.File

class FakeLogExporter : LogExporter {

    private var result: Result<String> = Result.success(DEFAULT_CONTENT)

    private val mutableExportedFiles = mutableListOf<File>()
    val exportedFiles: List<File> = mutableExportedFiles

    fun setResult(value: Result<String>) {
        result = value
    }

    override suspend fun export(destination: File, userId: UserId?) {
        mutableExportedFiles.add(destination)
        destination.writeText(result.getOrThrow())
    }

    companion object {
        const val DEFAULT_CONTENT = "fake logs"
    }
}
