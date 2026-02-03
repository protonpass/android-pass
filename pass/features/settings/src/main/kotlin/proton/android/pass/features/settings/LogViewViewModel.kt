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

package proton.android.pass.features.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.commonui.api.ClassHolder
import proton.android.pass.log.api.LogFileManager
import proton.android.pass.log.api.PassLogger
import proton.android.pass.log.api.ShareLogsUseCase
import java.io.FileNotFoundException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class LogViewViewModel @Inject constructor(
    private val logFileManager: LogFileManager,
    private val accountManager: AccountManager,
    private val shareLogsUseCase: ShareLogsUseCase,
    private val appDispatchers: AppDispatchers
) : ViewModel() {

    private val _state: MutableStateFlow<String> = MutableStateFlow("")
    val state: StateFlow<String> = _state

    fun loadLogFile() = viewModelScope.launch(appDispatchers.io) {
        try {
            val userId = accountManager.getPrimaryUserId().firstOrNull()
            val logFile = logFileManager.getLogFile(userId)
            if (logFile.exists()) {
                logFile.bufferedReader().use { br ->
                    _state.update {
                        br.readLines()
                            .takeLast(TAIL_LOG_SIZE)
                            .reversed()
                            .joinToString("\n")
                    }
                }
            }
        } catch (e: IOException) {
            PassLogger.w(TAG, "Could not read log file")
            PassLogger.w(TAG, e)
        } catch (e: FileNotFoundException) {
            PassLogger.w(TAG, "Could not find log file")
            PassLogger.w(TAG, e)
        }
    }

    fun startShareIntent(contextHolder: ClassHolder<Context>) = viewModelScope.launch {
        contextHolder.get().value()?.let { context ->
            shareLogsUseCase(context)
        }
    }

    companion object {
        private const val TAIL_LOG_SIZE = 100
        private const val TAG = "LogViewViewModel"
    }
}
