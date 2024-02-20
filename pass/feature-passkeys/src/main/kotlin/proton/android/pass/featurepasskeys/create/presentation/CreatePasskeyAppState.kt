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

package proton.android.pass.featurepasskeys.create.presentation

import androidx.compose.runtime.Immutable
import proton.android.pass.preferences.ThemePreference

@Immutable
data class CreatePasskeyRequestData(
    val domain: String,
    val origin: String,
    val username: String,
    val request: String,
    val rpName: String
)

@Immutable
sealed interface CreatePasskeyAppState {

    @Immutable
    object NotReady : CreatePasskeyAppState

    @Immutable
    object Close : CreatePasskeyAppState

    @Immutable
    data class Ready(
        val theme: ThemePreference,
        val needsAuth: Boolean,
        val data: CreatePasskeyRequestData
    ) : CreatePasskeyAppState
}
