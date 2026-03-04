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

package proton.android.pass.autofill.autofillhealth.service

import android.Manifest
import proton.android.pass.autofill.autofillhealth.model.LogcatEntry
import android.content.Context
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import proton.android.pass.log.api.PassLogger
import java.io.BufferedReader
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AutofillLogcatReader @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val _entries = MutableStateFlow<List<LogcatEntry>>(emptyList())
    val entries: StateFlow<List<LogcatEntry>> = _entries

    private val _hasReadLogsPermission = MutableStateFlow(false)
    val hasReadLogsPermission: StateFlow<Boolean> = _hasReadLogsPermission

    private val _isVerbosePropsEnabled = MutableStateFlow(false)
    val isVerbosePropsEnabled: StateFlow<Boolean> = _isVerbosePropsEnabled

    private var process: Process? = null
    private var readJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private val autofillTagFilter = Regex("autofill", RegexOption.IGNORE_CASE)

    private val myPid = android.os.Process.myPid()
    private val uidToPackageName =
        object : LinkedHashMap<Int, String>(MAX_CACHED_UIDS, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Int, String>): Boolean =
                size > MAX_CACHED_UIDS
        }

    private val logcatRegex = Regex(
        """^(\d{2}-\d{2}\s+\d{2}:\d{2}:\d{2}\.\d{3})\s+(\d+)\s+(\d+)\s+\d+\s+([VDIWEF])\s+(.+?)\s*:\s+(.*)$"""
    )

    fun start() {
        if (process != null) return
        refreshReadLogsPermission()
        try {
            process = if (_hasReadLogsPermission.value) {
                // READ_LOGS granted: read ALL logcat, filter in code
                Runtime.getRuntime().exec(
                    arrayOf("logcat", "-v", "uid,threadtime", "-T", "1")
                )
            } else {
                // No READ_LOGS: can only read own process, use tag filter
                Runtime.getRuntime().exec(
                    arrayOf(
                        "logcat",
                        "-v", "uid,threadtime",
                        "-T", "1",
                        "-s",
                        "AutofillManager:*",
                        "AutofillService:*",
                        "Autofill:*",
                        "AutoFillHandler:*",
                        "AutofillSession:*",
                        "AutofillInlineSuggestionsRequestCallback:*"
                    )
                )
            }
            val filterInCode = _hasReadLogsPermission.value
            readJob = scope.launch {
                val reader: BufferedReader =
                    process?.inputStream?.bufferedReader() ?: return@launch
                try {
                    var line = reader.readLine()
                    while (isActive && line != null) {
                        parseLine(line)?.let { entry ->
                            val matchesFilter = !filterInCode ||
                                autofillTagFilter.containsMatchIn(entry.tag)
                            if (matchesFilter) {
                                val current = _entries.value
                                _entries.value = if (current.size >= MAX_ENTRIES) {
                                    current.drop(1) + entry
                                } else {
                                    current + entry
                                }
                            }
                        }
                        line = reader.readLine()
                    }
                } catch (_: IOException) {
                    // Process was destroyed
                }
            }
        } catch (e: IOException) {
            PassLogger.w(TAG, e)
        }
    }

    fun stop() {
        readJob?.cancel()
        readJob = null
        process?.destroy()
        process = null
    }

    fun clear() {
        _entries.value = emptyList()
    }

    fun refreshState() {
        val wasGranted = _hasReadLogsPermission.value
        refreshReadLogsPermission()
        refreshVerboseProps()
        if (!wasGranted && _hasReadLogsPermission.value) {
            stop()
            start()
        }
    }

    private fun refreshReadLogsPermission() {
        _hasReadLogsPermission.value = context.checkSelfPermission(
            Manifest.permission.READ_LOGS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun refreshVerboseProps() {
        _isVerbosePropsEnabled.value = try {
            val proc = Runtime.getRuntime().exec(
                arrayOf("getprop", "log.tag.AutofillManager")
            )
            val output = proc.inputStream.bufferedReader().use { it.readText().trim() }
            proc.waitFor()
            output.equals("VERBOSE", ignoreCase = true)
        } catch (_: IOException) {
            false
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
            false
        }
    }

    private fun parseLine(line: String): LogcatEntry? {
        val match = logcatRegex.matchEntire(line) ?: return null
        val uid = match.groupValues[2].toIntOrNull() ?: 0
        val pid = match.groupValues[3].toIntOrNull() ?: 0
        val isOwn = pid == myPid
        return LogcatEntry(
            timestamp = match.groupValues[1],
            level = match.groupValues[4][0],
            tag = match.groupValues[5].trim(),
            message = match.groupValues[6],
            isOwnProcess = isOwn,
            processName = if (isOwn) "" else resolvePackageName(uid)
        )
    }

    private fun resolvePackageName(uid: Int): String = uidToPackageName.getOrPut(uid) {
        context.packageManager.getPackagesForUid(uid)
            ?.firstOrNull()
            ?: "uid:$uid"
    }

    private companion object {
        const val TAG = "AutofillLogcatReader"
        const val MAX_ENTRIES = 500
        const val MAX_CACHED_UIDS = 100
    }
}
