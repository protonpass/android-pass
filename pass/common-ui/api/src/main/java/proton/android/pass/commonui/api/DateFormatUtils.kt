package proton.android.pass.commonui.api

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

object DateFormatUtils {

    @Composable
    fun formatInstantText(now: Instant, toFormat: Instant): String {
        val formatted = formatInstant(now, toFormat, TimeZone.currentSystemDefault())
        return when (formatted) {
            is FormatResult.Date -> {
                val month = stringResource(stringForMonth(formatted.month))

                "${formatted.day} $month ${formatted.year} ${formatted.time}"
            }
            is FormatResult.Today -> {
                val today = stringResource(R.string.date_today)
                "$today ${formatted.time}"
            }
            is FormatResult.Yesterday -> {
                val yesterday = stringResource(R.string.date_yesterday)
                "$yesterday ${formatted.time}"
            }
        }
    }

    fun formatInstant(
        now: Instant,
        toFormat: Instant,
        timeZone: TimeZone = TimeZone.UTC
    ): FormatResult {
        val nowAsUtc = now.toLocalDateTime(timeZone)
        val instantAsUtc = toFormat.toLocalDateTime(timeZone)

        // If date is the same, it's a Today
        if (nowAsUtc.date.equals(instantAsUtc.date)) {
            return formatToday(instantAsUtc)
        }

        // Check if the instant is the day before
        val nowMinusOneDay = now.minus(24, DateTimeUnit.HOUR).toLocalDateTime(timeZone)
        if (nowMinusOneDay.date.equals(instantAsUtc.date)) {
            return formatYesterday(instantAsUtc)
        }

        // It's neither today nor yesterday, format it as a regular date
        return formatRegularDate(instantAsUtc)
    }

    @StringRes
    fun stringForMonth(month: Month): Int =
        when (month) {
            Month.JANUARY -> R.string.month_january
            Month.FEBRUARY -> R.string.month_february
            Month.MARCH -> R.string.month_march
            Month.APRIL -> R.string.month_april
            Month.MAY -> R.string.month_may
            Month.JUNE -> R.string.month_june
            Month.JULY -> R.string.month_july
            Month.AUGUST -> R.string.month_august
            Month.SEPTEMBER -> R.string.month_september
            Month.OCTOBER -> R.string.month_october
            Month.NOVEMBER -> R.string.month_november
            Month.DECEMBER -> R.string.month_december
        }


    private fun formatToday(instant: LocalDateTime): FormatResult.Today =
        FormatResult.Today(extractHour(instant))


    private fun formatYesterday(instant: LocalDateTime): FormatResult.Yesterday =
        FormatResult.Yesterday(extractHour(instant))

    private fun formatRegularDate(instant: LocalDateTime): FormatResult.Date {
        return FormatResult.Date(
            day = instant.dayOfMonth,
            month = instant.month,
            year = instant.year,
            time = extractHour(instant)
        )
    }

    private fun extractHour(instant: LocalDateTime): String {
        val hour = instant.hour.toString().padStart(2, '0')
        val minute = instant.minute.toString().padStart(2, '0')
        return "$hour:$minute"
    }

    sealed interface FormatResult {
        data class Today(val time: String) : FormatResult
        data class Yesterday(val time: String) : FormatResult
        data class Date(
            val day: Int,
            val month: Month,
            val year: Int,
            val time: String
        ) : FormatResult
    }
}
