/*
 * Copyright (c) 2025-2026 Proton AG
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

package proton.android.pass.features.password.history.model

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import proton.android.pass.domain.PasswordHistoryEntry
import proton.android.pass.domain.PasswordHistoryEntryId
import proton.android.pass.features.itemcreate.common.UIHiddenState

sealed interface PasswordDateLabel {
    val time: String

    data class Today(override val time: String) : PasswordDateLabel
    data class Yesterday(override val time: String) : PasswordDateLabel
    data class DaysAgo(val days: Int, override val time: String) : PasswordDateLabel
}

@Stable
data class PasswordHistoryItemUiState(
    val passwordHistoryEntryId: PasswordHistoryEntryId,
    val value: UIHiddenState,
    val dateLabel: PasswordDateLabel
)

@Stable
data class PasswordHistoryUiState(
    val isLoading: Boolean = false,
    val items: ImmutableList<PasswordHistoryItemUiState> = persistentListOf()
) {
    val isOptionsMenuVisible: Boolean = items.isNotEmpty()
}

internal fun PasswordHistoryEntry.toUiModel(
    clock: Clock,
    defaultUIHiddenState: UIHiddenState
): PasswordHistoryItemUiState {
    val now = clock.now()
    val currentDate = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
    val createdDateTime = Instant.fromEpochSeconds(createdTime)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val createdDate = createdDateTime.date

    val timePart = "%02d:%02d".format(createdDateTime.time.hour, createdDateTime.time.minute)
    val diffDays = currentDate.daysUntil(createdDate)

    val dateLabel = when {
        diffDays == 0 -> PasswordDateLabel.Today(timePart)
        diffDays == -1 -> PasswordDateLabel.Yesterday(timePart)
        diffDays < -1 -> PasswordDateLabel.DaysAgo(-diffDays, timePart)
        else -> throw IllegalStateException("date in the future createdDate $createdDate")
    }

    return PasswordHistoryItemUiState(
        value = defaultUIHiddenState,
        dateLabel = dateLabel,
        passwordHistoryEntryId = passwordHistoryEntryId
    )
}
