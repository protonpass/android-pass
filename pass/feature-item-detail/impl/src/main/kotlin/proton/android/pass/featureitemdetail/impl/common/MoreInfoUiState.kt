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

package proton.android.pass.featureitemdetail.impl.common

import androidx.compose.runtime.Stable
import kotlinx.datetime.Instant
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option

@Stable
data class MoreInfoUiState(
    val now: Instant,
    val lastAutofilled: Option<Instant>,
    val lastModified: Instant,
    val numRevisions: Long,
    val createdTime: Instant
) {
    companion object {
        val Initial = MoreInfoUiState(
            now = Instant.fromEpochSeconds(0),
            lastAutofilled = None,
            lastModified = Instant.fromEpochSeconds(0),
            numRevisions = 0,
            createdTime = Instant.fromEpochSeconds(0)
        )
    }
}
