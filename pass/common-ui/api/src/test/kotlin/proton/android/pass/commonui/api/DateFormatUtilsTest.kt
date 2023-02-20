package proton.android.pass.commonui.api

import com.google.common.truth.Truth.assertThat
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.Month
import kotlinx.datetime.minus
import org.junit.Test

class DateFormatUtilsTest {

    @Test
    fun canFormatToday() {
        val now = Instant.fromEpochSeconds(NOW)
        val nowMinusOneHour = now.minus(1, DateTimeUnit.HOUR)
        val res = DateFormatUtils.formatInstant(now, nowMinusOneHour)
        assertThat(res).isInstanceOf(DateFormatUtils.FormatResult.Today::class.java)

        val asToday = res as DateFormatUtils.FormatResult.Today
        assertThat(asToday.time).isEqualTo("12:48")
    }

    @Test
    fun canFormatYesterday() {
        val now = Instant.fromEpochSeconds(NOW)
        val nowMinusOneDay = now.minus(24, DateTimeUnit.HOUR)
        val res = DateFormatUtils.formatInstant(now, nowMinusOneDay)
        assertThat(res).isInstanceOf(DateFormatUtils.FormatResult.Yesterday::class.java)

        val asYesterday = res as DateFormatUtils.FormatResult.Yesterday
        assertThat(asYesterday.time).isEqualTo("13:48")
    }

    @Test
    fun canFormatOneWeekAgo() {
        val now = Instant.fromEpochSeconds(NOW)
        val nowMinusOneWeek = now.minus(24 * 7, DateTimeUnit.HOUR)
        val res = DateFormatUtils.formatInstant(now, nowMinusOneWeek)
        assertThat(res).isInstanceOf(DateFormatUtils.FormatResult.DateOfSameYear::class.java)

        val asDate = res as DateFormatUtils.FormatResult.DateOfSameYear
        val date = asDate.localDateTime
        assertThat(date.dayOfMonth).isEqualTo(10)
        assertThat(date.month).isEqualTo(Month.FEBRUARY)
        assertThat(date.year).isEqualTo(2023)
        assertThat(date.time.hour).isEqualTo(13)
        assertThat(date.time.minute).isEqualTo(48)
    }

    @Test
    fun canFormatOneYearAgo() {
        val now = Instant.fromEpochSeconds(NOW)
        val nowMinusOneYear = now.minus(24 * 365, DateTimeUnit.HOUR)
        val res = DateFormatUtils.formatInstant(now, nowMinusOneYear)
        assertThat(res).isInstanceOf(DateFormatUtils.FormatResult.Date::class.java)

        val asDate = res as DateFormatUtils.FormatResult.Date
        val date = asDate.localDateTime
        assertThat(date.dayOfMonth).isEqualTo(17)
        assertThat(date.month).isEqualTo(Month.FEBRUARY)
        assertThat(date.year).isEqualTo(2022)
        assertThat(date.time.hour).isEqualTo(13)
        assertThat(date.time.minute).isEqualTo(48)
    }

    @Suppress("UnderscoresInNumericLiterals")
    companion object {
        private const val NOW = 1676641715L // Friday, February 17 2023 13:48:35 UTC
    }
}
