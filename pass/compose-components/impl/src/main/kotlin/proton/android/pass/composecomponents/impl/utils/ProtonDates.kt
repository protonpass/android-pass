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

package proton.android.pass.composecomponents.impl.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import proton.android.pass.commonui.api.DateFormatUtils
import proton.android.pass.composecomponents.impl.R
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val DEFAULT_DATE_TEXT = ""

@Composable
fun protonFormattedDateText(
    endInstant: Instant,
    startInstant: Instant = Instant.fromEpochSeconds(epochSeconds = 0),
    locale: Locale = Locale.getDefault(),
): String = when (
    DateFormatUtils.getFormat(
        now = startInstant,
        toFormat = endInstant,
        timeZone = TimeZone.currentSystemDefault(),
        acceptedFormats = listOf(
            DateFormatUtils.Format.Today,
            DateFormatUtils.Format.Yesterday,
            DateFormatUtils.Format.DateOfSameYear,
            DateFormatUtils.Format.Date,
        )
    )
) {
    DateFormatUtils.Format.Date -> runCatching {
        DateTimeFormatter.ofPattern(stringResource(R.string.date_full_date_format_with_year))
            .withLocale(locale)
            .format(
                endInstant.toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime()
            )
    }.getOrDefault(DEFAULT_DATE_TEXT)

    DateFormatUtils.Format.DateOfSameYear -> runCatching {
        DateTimeFormatter.ofPattern(stringResource(R.string.date_full_date_format))
            .withLocale(locale)
            .format(
                endInstant.toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime()
            )
    }.getOrDefault(DEFAULT_DATE_TEXT)

    DateFormatUtils.Format.Today -> runCatching {
        stringResource(
            R.string.date_today,
            DateFormatUtils.getTime(endInstant.toLocalDateTime(TimeZone.currentSystemDefault())),
        )
    }.getOrDefault(DEFAULT_DATE_TEXT)

    DateFormatUtils.Format.Yesterday -> runCatching {
        stringResource(
            R.string.date_yesterday,
            DateFormatUtils.getTime(endInstant.toLocalDateTime(TimeZone.currentSystemDefault())),
        )
    }.getOrDefault(DEFAULT_DATE_TEXT)

    else -> throw IllegalStateException("Unexpected date format")
}
