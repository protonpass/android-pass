package proton.android.pass.commonui.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
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

    @Composable
    fun formatInstantText(now: Instant, toFormat: Instant, locale: Locale = Locale.getDefault()): String {
        val formatted = formatInstant(now, toFormat, TimeZone.currentSystemDefault())
        return when (formatted) {
            is FormatResult.Date -> {
                val pattern = stringResource(R.string.date_full_date_format_with_year)
                DateTimeFormatter.ofPattern(pattern)
                    .withLocale(locale)
                    .format(formatted.localDateTime.toJavaLocalDateTime())
            }
            is FormatResult.DateOfSameYear -> {
                val pattern = stringResource(R.string.date_full_date_format)
                DateTimeFormatter.ofPattern(pattern)
                    .withLocale(locale)
                    .format(formatted.localDateTime.toJavaLocalDateTime())
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
        val nowAsLocal = now.toLocalDateTime(timeZone)
        val instantAsLocal = toFormat.toLocalDateTime(timeZone)

        // If date is the same, it's a Today
        if (nowAsLocal.date.equals(instantAsLocal.date)) {
            return formatToday(instantAsLocal)
        }

        // Check if the instant is the day before
        val nowMinusOneDay = now.minus(24, DateTimeUnit.HOUR).toLocalDateTime(timeZone)
        if (nowMinusOneDay.date.equals(instantAsLocal.date)) {
            return formatYesterday(instantAsLocal)
        }

        // Check if the year is the same
        return if (nowAsLocal.year == instantAsLocal.year) {
            FormatResult.DateOfSameYear(instantAsLocal)
        } else {
            FormatResult.Date(localDateTime = instantAsLocal)
        }
    }

    private fun formatToday(instant: LocalDateTime): FormatResult.Today =
        FormatResult.Today(extractHour(instant))


    private fun formatYesterday(instant: LocalDateTime): FormatResult.Yesterday =
        FormatResult.Yesterday(extractHour(instant))

    private fun extractHour(instant: LocalDateTime): String {
        val hour = instant.hour.toString().padStart(2, '0')
        val minute = instant.minute.toString().padStart(2, '0')
        return "$hour:$minute"
    }

    sealed interface FormatResult {
        data class Today(val time: String) : FormatResult
        data class Yesterday(val time: String) : FormatResult
        data class DateOfSameYear(val localDateTime: LocalDateTime) : FormatResult
        data class Date(val localDateTime: LocalDateTime) : FormatResult
    }
}
