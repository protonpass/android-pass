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

package proton.android.pass.features.secure.links.create.presentation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import proton.android.pass.common.api.None
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.navigation.api.CommonNavArgId
import javax.inject.Inject

@HiltViewModel
class SecureLinksCreateViewModel @Inject constructor(
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val shareId: ShareId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ShareId.key)
        .let(::ShareId)

    private val itemId: ItemId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ItemId.key)
        .let(::ItemId)

    private var _state = MutableStateFlow(SecureLinksCreateState.Initial)
    internal val state: StateFlow<SecureLinksCreateState> = _state.asStateFlow()

    internal fun onMaxViewsEnabled() {
        _state.update { currentState ->
            currentState.copy(maxViewsAllowedOption = MIN_SECURE_LINK_VIEWS_ALLOWED.toOption())
        }
    }

    internal fun onMaxViewsDisabled() {
        _state.update { currentState ->
            currentState.copy(maxViewsAllowedOption = None)
        }
    }

    internal fun onMaxViewsDecreased() {
        _state.update { currentState ->
            currentState.copy(
                maxViewsAllowedOption = currentState.maxViewsAllowed
                    .minus(1)
                    .coerceAtLeast(MIN_SECURE_LINK_VIEWS_ALLOWED)
                    .toOption()
            )
        }
    }

    internal fun onMaxViewsIncreased() {
        _state.update { currentState ->
            currentState.copy(
                maxViewsAllowedOption = currentState.maxViewsAllowed
                    .plus(1)
                    .toOption()
            )
        }
    }

    private companion object {

        private const val MIN_SECURE_LINK_VIEWS_ALLOWED = 1

    }

}
