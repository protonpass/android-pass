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

package proton.android.pass.commonui.api

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateFormatUtils {

    fun formatDateFromMillis(pattern: String, epochMillis: Long): String = runCatching {
        DateTimeFormatter.ofPattern(pattern)
            .withLocale(Locale.getDefault())
            .format(
                Instant.fromEpochMilliseconds(epochMillis)
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .toJavaLocalDateTime()
            )
    }.getOrDefault("")

    fun getFormat(
        now: Instant,
        toFormat: Instant,
        acceptedFormats: List<Format>,
        timeZone: TimeZone = TimeZone.UTC
    ): Format {
        val nowAsLocal = now.toLocalDateTime(timeZone)
        val instantAsLocal = toFormat.toLocalDateTime(timeZone)
        val nowMinusOneDay = now.minus(24, DateTimeUnit.HOUR).toLocalDateTime(timeZone)
        val nowMinus1Week = now.minus(1, DateTimeUnit.WEEK, timeZone).toLocalDateTime(timeZone)
        val nowMinus2Week = now.minus(2, DateTimeUnit.WEEK, timeZone).toLocalDateTime(timeZone)
        val nowMinus30Days = now.minus(30, DateTimeUnit.DAY, timeZone).toLocalDateTime(timeZone)
        val nowMinus60Days = now.minus(60, DateTimeUnit.DAY, timeZone).toLocalDateTime(timeZone)
        val nowMinus90Days = now.minus(90, DateTimeUnit.DAY, timeZone).toLocalDateTime(timeZone)
        val nowMinus1Year = now.minus(1, DateTimeUnit.YEAR, timeZone).toLocalDateTime(timeZone)

        return when {
            isToday(acceptedFormats, nowAsLocal, instantAsLocal) -> Format.Today
            isYesterday(acceptedFormats, nowMinusOneDay, instantAsLocal) -> Format.Yesterday
            isThisWeek(acceptedFormats, nowMinus1Week, instantAsLocal) -> Format.ThisWeek
            isLastTwoWeeks(acceptedFormats, nowMinus2Week, instantAsLocal) -> Format.LastTwoWeeks
            isLast30Days(acceptedFormats, nowMinus30Days, instantAsLocal) -> Format.Last30Days
            isLast60Days(acceptedFormats, nowMinus60Days, instantAsLocal) -> Format.Last60Days
            isLast90Days(acceptedFormats, nowMinus90Days, instantAsLocal) -> Format.Last90Days
            isLastYear(acceptedFormats, nowMinus1Year, instantAsLocal) -> Format.LastYear
            isMoreThan1Year(acceptedFormats, nowMinus1Year, instantAsLocal) -> Format.MoreThan1Year
            isDateOfSameYear(acceptedFormats, nowAsLocal, instantAsLocal) -> Format.DateOfSameYear
            acceptedFormats.contains(Format.Date) -> Format.Date
            else -> throw IllegalStateException("Unexpected date format")
        }
    }

    fun getTime(instant: LocalDateTime): String {
        val hour = instant.hour.toString().padStart(2, '0')
        val minute = instant.minute.toString().padStart(2, '0')
        return "$hour:$minute"
    }

    private fun isMoreThan1Year(
        acceptedFormats: List<Format>,
        nowMinus1Year: LocalDateTime,
        instantAsLocal: LocalDateTime
    ) = acceptedFormats.contains(Format.MoreThan1Year) && nowMinus1Year.date >= instantAsLocal.date

    private fun isLastYear(
        acceptedFormats: List<Format>,
        nowMinus1Year: LocalDateTime,
        instantAsLocal: LocalDateTime
    ) = acceptedFormats.contains(Format.LastYear) && nowMinus1Year.date < instantAsLocal.date

    private fun isLast90Days(
        acceptedFormats: List<Format>,
        nowMinus90Days: LocalDateTime,
        instantAsLocal: LocalDateTime
    ) = acceptedFormats.contains(Format.Last90Days) && nowMinus90Days.date < instantAsLocal.date

    private fun isLast60Days(
        acceptedFormats: List<Format>,
        nowMinus60Days: LocalDateTime,
        instantAsLocal: LocalDateTime
    ) = acceptedFormats.contains(Format.Last60Days) && nowMinus60Days.date < instantAsLocal.date

    private fun isLast30Days(
        acceptedFormats: List<Format>,
        nowMinus30Days: LocalDateTime,
        instantAsLocal: LocalDateTime
    ) = acceptedFormats.contains(Format.Last30Days) && nowMinus30Days.date < instantAsLocal.date

    private fun isLastTwoWeeks(
        acceptedFormats: List<Format>,
        nowMinus2Week: LocalDateTime,
        instantAsLocal: LocalDateTime
    ) = acceptedFormats.contains(Format.LastTwoWeeks) && nowMinus2Week.date < instantAsLocal.date

    private fun isThisWeek(
        acceptedFormats: List<Format>,
        nowMinus1Week: LocalDateTime,
        instantAsLocal: LocalDateTime
    ) = acceptedFormats.contains(Format.ThisWeek) && nowMinus1Week.date < instantAsLocal.date

    private fun isDateOfSameYear(
        acceptedFormats: List<Format>,
        nowAsLocal: LocalDateTime,
        instantAsLocal: LocalDateTime
    ) = acceptedFormats.contains(Format.DateOfSameYear) && nowAsLocal.year == instantAsLocal.year

    private fun isYesterday(
        acceptedFormats: List<Format>,
        nowMinusOneDay: LocalDateTime,
        instantAsLocal: LocalDateTime
    ) = acceptedFormats.contains(Format.Yesterday) && nowMinusOneDay.date == instantAsLocal.date

    private fun isToday(
        acceptedFormats: List<Format>,
        nowAsLocal: LocalDateTime,
        instantAsLocal: LocalDateTime
    ) = acceptedFormats.contains(Format.Today) && nowAsLocal.date == instantAsLocal.date

    enum class Format {
        Today,
        Yesterday,
        ThisWeek,
        LastTwoWeeks,
        Last30Days,
        Last60Days,
        Last90Days,
        LastYear,
        MoreThan1Year,
        DateOfSameYear,
        Date
    }
}
