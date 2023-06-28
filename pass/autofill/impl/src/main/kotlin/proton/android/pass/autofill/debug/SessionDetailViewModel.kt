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

package proton.android.pass.autofill.debug

import android.content.Context
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import proton.android.pass.autofill.debug.DebugUtils.autofillDumpDir
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SessionDetailViewModel @Inject constructor(
    @ApplicationContext context: Context,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val sessionName = savedStateHandleProvider.get().require<String>(SessionDetailId.key)

    val state: StateFlow<DetailContent> = getEntry(context)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = DetailContent.Loading
        )

    private fun getEntry(context: Context): Flow<DetailContent.Success> = flow {
        val fileContent = withContext(Dispatchers.IO) {
            val dir = autofillDumpDir(context)
            val file = File(dir, sessionName)
            file.readText()
        }
        val entry: AutofillDebugSaver.DebugAutofillEntry = Json.decodeFromString(fileContent)
        emit(DetailContent.Success(entry))
    }
}

@Stable
sealed interface DetailContent {
    @Stable
    object Loading : DetailContent

    @Stable
    data class Success(val content: AutofillDebugSaver.DebugAutofillEntry) : DetailContent
}
