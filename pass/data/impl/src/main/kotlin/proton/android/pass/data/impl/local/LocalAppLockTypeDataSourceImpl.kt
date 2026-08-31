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

package proton.android.pass.data.impl.local

import android.content.Context
import android.system.Os
import android.system.OsConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.AppLockTimePreference
import proton.android.pass.preferences.AppLockTypePreference
import java.io.File
import java.io.FileOutputStream
import java.util.zip.CRC32
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalAppLockTypeDataSourceImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val appDispatchers: AppDispatchers
) : LocalAppLockTypeDataSource {

    private val storeDir: File
        get() = context.noBackupFilesDir

    override suspend fun store(type: AppLockTypePreference, time: AppLockTimePreference): Result<Unit> =
        withContext(appDispatchers.io) {
            val file = File(storeDir, FILE_NAME)
            val tmp = File(storeDir, TMP_FILE_NAME)
            when (type) {
                AppLockTypePreference.None ->
                    safeRunCatching {
                        if (file.exists() && !file.delete()) error("Failed to delete app lock state file")
                    }.onSuccess { PassLogger.i(TAG, "Deleted app lock state file") }

                else ->
                    safeRunCatching {
                        val payload = "${type.name}:${time.name}"
                        writeDurably(tmp, file, "$payload:${crc(payload)}")
                    }.onSuccess { PassLogger.i(TAG, "Stored app lock state: type=$type, time=$time") }
            }
                .also { if (tmp.exists() && !tmp.delete()) PassLogger.w(TAG, "Failed to delete app lock state tmp") }
                .onFailure { PassLogger.w(TAG, it, "Failed to store app lock state: type=$type") }
        }

    override suspend fun read(): AppLockTypeRecord = withContext(appDispatchers.io) {
        val file = File(storeDir, FILE_NAME)
        if (!file.exists() || file.isDirectory) {
            PassLogger.i(TAG, "App lock state file missing (isDirectory=${file.isDirectory})")
            return@withContext AppLockTypeRecord.Absent
        }
        safeRunCatching {
            val parts = file.readText().trim().split(":")
            if (parts.size != EXPECTED_PARTS) {
                PassLogger.w(TAG, "App lock state malformed: expected $EXPECTED_PARTS parts, got ${parts.size}")
                AppLockTypeRecord.Corrupted
            } else {
                val (name, timeName, sum) = parts
                val type = AppLockTypePreference.valueOf(name)
                val time = AppLockTimePreference.valueOf(timeName)
                if (sum != crc("$name:$timeName")) {
                    PassLogger.w(TAG, "App lock state checksum mismatch")
                    AppLockTypeRecord.Corrupted
                } else if (type == AppLockTypePreference.None) {
                    PassLogger.w(TAG, "App lock state holds an unexpected None type")
                    AppLockTypeRecord.Corrupted
                } else {
                    AppLockTypeRecord.Valid(type, time)
                }
            }
        }.getOrElse {
            PassLogger.w(TAG, it, "Failed to read app lock state")
            AppLockTypeRecord.Corrupted
        }
    }

    private fun writeDurably(
        tmp: File,
        target: File,
        content: String
    ) {
        val bytes = content.toByteArray(Charsets.UTF_8)
        writeAndSync(tmp, bytes)
        if (!tmp.renameTo(target)) {
            PassLogger.i(TAG, "Atomic rename unavailable, falling back to a direct write")
            writeAndSync(target, bytes)
        }
        syncDir(target.parentFile)
    }

    private fun writeAndSync(file: File, bytes: ByteArray) {
        FileOutputStream(file).use { output ->
            output.write(bytes)
            output.fd.sync()
        }
    }

    private fun syncDir(dir: File?) {
        if (dir == null) return
        runCatching {
            val fd = Os.open(dir.absolutePath, OsConstants.O_RDONLY, 0)
            try {
                Os.fsync(fd)
            } finally {
                Os.close(fd)
            }
        }.onFailure { PassLogger.w(TAG, it, "Failed to sync app lock state directory") }
    }

    private fun crc(value: String): String {
        val crc = CRC32()
        crc.update(value.toByteArray(Charsets.UTF_8))
        return crc.value.toString()
    }

    private companion object {
        private const val FILE_NAME = "app_lock_state"
        private const val TMP_FILE_NAME = "$FILE_NAME.tmp"
        private const val TAG = "LocalAppLockTypeDataSourceImpl"
        private const val EXPECTED_PARTS = 3
    }
}
