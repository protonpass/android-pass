package proton.android.pass.commonui.api

import com.google.common.truth.Truth.assertThat
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import org.junit.Test

class DateFormatUtilsTest {

    @Test(expected = IllegalStateException::class)
    fun `given no format should throw error`() {
        val now = Instant.fromEpochSeconds(NOW)
        DateFormatUtils.getFormat(now, now, emptyList())
    }

    @Test
    fun `given today can format today`() {
        val now = Instant.fromEpochSeconds(NOW)
        val nowMinusOneHour = now.minus(1, DateTimeUnit.HOUR)
        val res = DateFormatUtils.getFormat(
            now,
            nowMinusOneHour,
            listOf(DateFormatUtils.Format.Today)
        )
        assertThat(res).isInstanceOf(DateFormatUtils.Format.Today::class.java)
    }

    @Test
    fun `given yesterday can format yesterday`() {
        val now = Instant.fromEpochSeconds(NOW)
        val nowMinusOneDay = now.minus(24, DateTimeUnit.HOUR)
        val res = DateFormatUtils.getFormat(
            now,
            nowMinusOneDay,
            listOf(DateFormatUtils.Format.Yesterday)
        )
        assertThat(res).isInstanceOf(DateFormatUtils.Format.Yesterday::class.java)
    }

    @Test
    fun `given 3 days ago can format ThisWeek`() {
        val now = Instant.fromEpochSeconds(NOW)
        val nowMinusOneDay = now.minus(3, DateTimeUnit.DAY, TimeZone.UTC)
        val res = DateFormatUtils.getFormat(
            now,
            nowMinusOneDay,
            listOf(DateFormatUtils.Format.ThisWeek)
        )
        assertThat(res).isInstanceOf(DateFormatUtils.Format.ThisWeek::class.java)
    }

    @Test
    fun `given last 9 days ago can format LastTwoWeeks`() {
        val now = Instant.fromEpochSeconds(NOW)
        val nowMinusOneDay = now.minus(9, DateTimeUnit.DAY, TimeZone.UTC)
        val res = DateFormatUtils.getFormat(
            now,
            nowMinusOneDay,
            listOf(DateFormatUtils.Format.LastTwoWeeks)
        )
        assertThat(res).isInstanceOf(DateFormatUtils.Format.LastTwoWeeks::class.java)
    }

    @Test
    fun `given last 20 days ago can format Last30Days`() {
        val now = Instant.fromEpochSeconds(NOW)
        val nowMinusOneDay = now.minus(20, DateTimeUnit.DAY, TimeZone.UTC)
        val res = DateFormatUtils.getFormat(
            now,
            nowMinusOneDay,
            listOf(DateFormatUtils.Format.Last30Days)
        )
        assertThat(res).isInstanceOf(DateFormatUtils.Format.Last30Days::class.java)
    }

    @Test
    fun `given last 40 days ago can format Last60Days`() {
        val now = Instant.fromEpochSeconds(NOW)
        val nowMinusOneDay = now.minus(40, DateTimeUnit.DAY, TimeZone.UTC)
        val res = DateFormatUtils.getFormat(
            now,
            nowMinusOneDay,
            listOf(DateFormatUtils.Format.Last60Days)
        )
        assertThat(res).isInstanceOf(DateFormatUtils.Format.Last60Days::class.java)
    }

    @Test
    fun `given last 80 days ago can format Last60Days`() {
        val now = Instant.fromEpochSeconds(NOW)
        val nowMinusOneDay = now.minus(80, DateTimeUnit.DAY, TimeZone.UTC)
        val res = DateFormatUtils.getFormat(
            now,
            nowMinusOneDay,
            listOf(DateFormatUtils.Format.Last90Days)
        )
        assertThat(res).isInstanceOf(DateFormatUtils.Format.Last90Days::class.java)
    }

    @Test
    fun `given last 91 days ago can format LastYear`() {
        val now = Instant.fromEpochSeconds(NOW)
        val nowMinusOneDay = now.minus(91, DateTimeUnit.DAY, TimeZone.UTC)
        val res = DateFormatUtils.getFormat(
            now,
            nowMinusOneDay,
            listOf(DateFormatUtils.Format.LastYear)
        )
        assertThat(res).isInstanceOf(DateFormatUtils.Format.LastYear::class.java)
    }

    @Test
    fun `given 1 week ago can format DateOfSameYear`() {
        val now = Instant.fromEpochSeconds(NOW)
        val nowMinusOneWeek = now.minus(24 * 7, DateTimeUnit.HOUR)
        val res = DateFormatUtils.getFormat(
            now,
            nowMinusOneWeek,
            listOf(DateFormatUtils.Format.DateOfSameYear)
        )
        assertThat(res).isInstanceOf(DateFormatUtils.Format.DateOfSameYear::class.java)
    }

    @Test
    fun `given 1 year ago can format Date`() {
        val now = Instant.fromEpochSeconds(NOW)
        val nowMinusOneYear = now.minus(24 * 365, DateTimeUnit.HOUR)
        val res = DateFormatUtils.getFormat(
            now,
            nowMinusOneYear,
            listOf(DateFormatUtils.Format.Date)
        )
        assertThat(res).isInstanceOf(DateFormatUtils.Format.Date::class.java)
    }

    @Suppress("UnderscoresInNumericLiterals")
    companion object {
        private const val NOW = 1676641715L // Friday, February 17 2023 13:48:35 UTC
    }
}
