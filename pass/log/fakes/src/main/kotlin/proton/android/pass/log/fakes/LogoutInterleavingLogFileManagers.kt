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

package proton.android.pass.log.fakes

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import me.proton.core.domain.entity.UserId
import proton.android.pass.log.api.LogFileManager
import java.io.File

class FakeLogoutInterleavingLogFileManager(
    private val authenticatedUserId: UserId,
    private val authenticatedFile: File,
    private val notAuthenticatedFile: File,
    private val authenticatedFileEnsured: CompletableDeferred<Unit>,
    private val resumeWriter: CompletableDeferred<Unit>
) : LogFileManager {

    override suspend fun <T> withLogFileLock(block: suspend () -> T): T = block()

    override suspend fun getLogFile(userId: UserId?): File = when (userId) {
        authenticatedUserId -> authenticatedFile
        null -> notAuthenticatedFile
        else -> error("Unexpected user ID")
    }

    override suspend fun initializeLogDirectory() = Unit

    override suspend fun ensureLogFileExists(file: File) {
        file.parentFile?.mkdirs()
        file.createNewFile()
        if (file == authenticatedFile) {
            authenticatedFileEnsured.complete(Unit)
            resumeWriter.await()
        }
    }

    override suspend fun getAllUserLogFiles(): List<File> = emptyList()

    override suspend fun deleteLogFile(file: File) = Unit
}

class FakePreEnsureLogoutInterleavingLogFileManager(
    private val authenticatedUserId: UserId,
    private val authenticatedFile: File,
    private val notAuthenticatedFile: File,
    private val initialDestinationResolved: CompletableDeferred<Unit>,
    private val releaseInitialDestination: CompletableDeferred<Unit>
) : LogFileManager {

    override suspend fun <T> withLogFileLock(block: suspend () -> T): T = block()

    override suspend fun getLogFile(userId: UserId?): File {
        val file = when (userId) {
            authenticatedUserId -> authenticatedFile
            null -> notAuthenticatedFile
            else -> error("Unexpected user ID")
        }
        if (file == authenticatedFile) {
            initialDestinationResolved.complete(Unit)
            releaseInitialDestination.await()
        }
        return file
    }

    override suspend fun initializeLogDirectory() = Unit

    override suspend fun ensureLogFileExists(file: File) {
        file.parentFile?.mkdirs()
        file.createNewFile()
    }

    override suspend fun getAllUserLogFiles(): List<File> = emptyList()

    override suspend fun deleteLogFile(file: File) = Unit
}

class FakeBatchedLogoutInterleavingLogFileManager(
    private val authenticatedUserId: UserId,
    private val authenticatedFile: File,
    private val notAuthenticatedFile: File,
    private val secondEntryStartedResolving: CompletableDeferred<Unit>,
    private val resumeSecondEntryResolution: CompletableDeferred<Unit>
) : LogFileManager {

    private var authenticatedFileResolutions = 0

    override suspend fun <T> withLogFileLock(block: suspend () -> T): T = block()

    override suspend fun getLogFile(userId: UserId?): File = when (userId) {
        authenticatedUserId -> {
            authenticatedFileResolutions++
            if (authenticatedFileResolutions == 2) {
                secondEntryStartedResolving.complete(Unit)
                resumeSecondEntryResolution.await()
            }
            authenticatedFile
        }
        null -> notAuthenticatedFile
        else -> error("Unexpected user ID")
    }

    override suspend fun initializeLogDirectory() = Unit

    override suspend fun ensureLogFileExists(file: File) {
        file.parentFile?.mkdirs()
        file.createNewFile()
    }

    override suspend fun getAllUserLogFiles(): List<File> = emptyList()

    override suspend fun deleteLogFile(file: File) = Unit
}

class FakeFinalValidationLogoutInterleavingLogFileManager(
    private val authenticatedUserId: UserId,
    private val authenticatedFile: File,
    private val notAuthenticatedFile: File,
    private val authenticatedFileReadyForWriter: CompletableDeferred<Unit>,
    private val releaseWriter: CompletableDeferred<Unit>
) : LogFileManager {

    private val logFileMutex = Mutex()
    private var authenticatedEnsureCount = 0

    override suspend fun <T> withLogFileLock(block: suspend () -> T): T {
        logFileMutex.lock()
        return try {
            block()
        } finally {
            logFileMutex.unlock()
        }
    }

    override suspend fun getLogFile(userId: UserId?): File = when (userId) {
        authenticatedUserId -> authenticatedFile
        null -> notAuthenticatedFile
        else -> error("Unexpected user ID")
    }

    override suspend fun initializeLogDirectory() = Unit

    override suspend fun ensureLogFileExists(file: File) {
        file.parentFile?.mkdirs()
        file.createNewFile()
        if (file == authenticatedFile && ++authenticatedEnsureCount == 2) {
            authenticatedFileReadyForWriter.complete(Unit)
            releaseWriter.await()
        }
    }

    override suspend fun getAllUserLogFiles(): List<File> = emptyList()

    override suspend fun deleteLogFile(file: File) {
        logFileMutex.lock()
        try {
            file.delete()
        } finally {
            logFileMutex.unlock()
        }
    }
}
