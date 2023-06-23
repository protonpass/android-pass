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

package proton.android.pass.inappupdates.api

sealed interface InAppUpdateState {
    object Idle : InAppUpdateState

    @JvmInline
    value class Downloading(val progress: Float) : InAppUpdateState
    object Downloaded : InAppUpdateState
    object Installing : InAppUpdateState
    object Installed : InAppUpdateState
    object Failed : InAppUpdateState
    object Pending : InAppUpdateState
    object Cancelled : InAppUpdateState
    object Unknown : InAppUpdateState
}
