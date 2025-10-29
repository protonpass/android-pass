/*
 * Copyright (c) 2025 Proton AG
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

import android.content.Context
import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import proton.android.pass.domain.PasswordHistoryEntry
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import proton.android.pass.domain.PasswordHistoryEntryId
import proton.android.pass.features.itemcreate.common.UIHiddenState
import proton.android.pass.features.password.R

@Stable
data class PasswordHistoryItemUiState(
    val passwordHistoryEntryId: PasswordHistoryEntryId,
    val value: UIHiddenState,
    val date: String
)

@Stable
data class PasswordHistoryUiState(
    val isLoading: Boolean = false,
    val items: ImmutableList<PasswordHistoryItemUiState> = persistentListOf()
)


internal fun PasswordHistoryEntry.toUiModel(
    context: Context,
    clock: Clock,
    defaultUIHiddenState: UIHiddenState
): PasswordHistoryItemUiState {

    val now = clock.now()
    val currentDateTime = now.toLocalDateTime(TimeZone.currentSystemDefault())
    val createdDateTime =
        Instant.fromEpochSeconds(createdTime).toLocalDateTime(TimeZone.currentSystemDefault())

    val currentDate = currentDateTime.date
    val createdDate = createdDateTime.date

    val diffDays = currentDate.daysUntil(createdDate)
    val timePart = createdDateTime.time.let {
        "%02d:%02d".format(it.hour, it.minute)
    }

    val dateText = when {
        diffDays == 0 -> context.getString(
            R.string.password_history_today, timePart
        )

        diffDays == -1 -> context.getString(
            R.string.password_history_yesterday, timePart
        )

        diffDays < -1 -> context.getString(
            R.string.password_history_before_yesterday,
            -diffDays,
            timePart
        )

        else -> throw IllegalStateException("date in the future createdDate $createdDate")
    }

    return PasswordHistoryItemUiState(
        value = defaultUIHiddenState,
        date = dateText,
        passwordHistoryEntryId = passwordHistoryEntryId
    )
}
