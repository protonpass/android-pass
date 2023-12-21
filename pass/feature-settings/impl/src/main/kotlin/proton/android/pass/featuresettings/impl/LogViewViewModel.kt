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

package proton.android.pass.featuresettings.impl

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.log.api.PassLogger
import proton.android.pass.log.api.ShareLogs
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class LogViewViewModel @Inject constructor(
    private val shareLogs: ShareLogs
) : ViewModel() {

    private val _state: MutableStateFlow<String> = MutableStateFlow("")
    val state: StateFlow<String> = _state

    fun loadLogFile(context: Context) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val cacheDir = File(context.cacheDir, "logs")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            val cacheFile = File(cacheDir, "pass.log")
            if (cacheFile.exists()) {
                cacheFile.bufferedReader().use { br ->
                    _state.update {
                        br.readLines()
                            .takeLast(TAIL_LOG_SIZE)
                            .reversed()
                            .joinToString("\n")
                    }
                }
            }
        } catch (e: IOException) {
            PassLogger.e(TAG, e, "Could not read log file")
        } catch (e: FileNotFoundException) {
            PassLogger.e(TAG, e, "Could not find log file")
        }
    }

    fun startShareIntent(context: Context) = viewModelScope.launch {
        val intent = shareLogs.createIntent()
        if (intent != null) {
            context.startActivity(intent)
        }
    }

    companion object {
        private const val TAIL_LOG_SIZE = 100
        private const val TAG = "LogViewViewModel"
    }
}
