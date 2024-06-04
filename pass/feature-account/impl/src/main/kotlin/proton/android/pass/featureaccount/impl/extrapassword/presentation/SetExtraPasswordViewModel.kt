/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.featureaccount.impl.extrapassword.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import dagger.hilt.android.lifecycle.HiltViewModel
import proton.android.pass.commonui.api.SavedStateHandleProvider
import javax.inject.Inject

@HiltViewModel
class SetExtraPasswordViewModel @Inject constructor(
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {
    @OptIn(SavedStateHandleSaveableApi::class)
    private var mutableExtraPasswordState: SetExtraPasswordState by savedStateHandleProvider.get()
        .saveable { mutableStateOf(SetExtraPasswordState.EMPTY) }

    fun getExtraPasswordState(): SetExtraPasswordState = mutableExtraPasswordState

    fun onExtraPasswordRepeatValueChanged(value: String) {
        mutableExtraPasswordState = mutableExtraPasswordState.copy(repeatPassword = value)
    }

    fun onExtraPasswordValueChanged(value: String) {
        mutableExtraPasswordState = mutableExtraPasswordState.copy(password = value)
    }

    fun submit() {
        // to implement
    }

}
