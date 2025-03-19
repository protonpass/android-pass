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

package proton.android.pass.features.item.details.qrviewer.presentation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.features.item.details.qrviewer.navigation.RawSVGNavArgId
import proton.android.pass.navigation.api.NavParamEncoder
import javax.inject.Inject

@HiltViewModel
class QRViewerViewModel @Inject constructor(
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    val rawSVG: String = savedStateHandleProvider.get()
        .require<String>(RawSVGNavArgId.key)
        .let { NavParamEncoder.decode(it) }
}
