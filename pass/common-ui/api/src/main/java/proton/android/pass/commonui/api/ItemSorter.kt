package proton.android.pass.commonui.api

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import proton.android.pass.commonui.api.DateFormatUtils.Format.Last30Days
import proton.android.pass.commonui.api.DateFormatUtils.Format.Last60Days
import proton.android.pass.commonui.api.DateFormatUtils.Format.Last90Days
import proton.android.pass.commonui.api.DateFormatUtils.Format.LastTwoWeeks
import proton.android.pass.commonui.api.DateFormatUtils.Format.LastYear
import proton.android.pass.commonui.api.DateFormatUtils.Format.MoreThan1Year
import proton.android.pass.commonui.api.DateFormatUtils.Format.ThisWeek
import proton.android.pass.commonui.api.DateFormatUtils.Format.Today
import proton.android.pass.commonui.api.DateFormatUtils.Format.Yesterday
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.log.api.PassLogger
import java.time.format.DateTimeFormatter

sealed interface GroupingKeys {
    data class AlphabeticalKey(val character: Char) : GroupingKeys
    data class MonthlyKey(
        val monthKey: String,
        val instant: Instant
    ) : GroupingKeys

    data class MostRecentKey(
        val formatResultKey: DateFormatUtils.Format,
        val instant: Instant
    ) : GroupingKeys

    object NoGrouping : GroupingKeys
}

object AlphabeticalKeyComparator : Comparator<GroupingKeys.AlphabeticalKey> {
    override fun compare(
        o1: GroupingKeys.AlphabeticalKey?,
        o2: GroupingKeys.AlphabeticalKey?
    ): Int {
        if (o1 == null || o2 == null) {
            return 0
        }
        return o1.character.compareTo(o2.character)
    }
}

object MonthlyKeyComparator : Comparator<GroupingKeys.MonthlyKey> {
    override fun compare(o1: GroupingKeys.MonthlyKey?, o2: GroupingKeys.MonthlyKey?): Int {
        if (o1 == null || o2 == null) {
            return 0
        }
        return o1.instant.compareTo(o2.instant)
    }
}

object MostRecentKeyComparator : Comparator<GroupingKeys.MostRecentKey> {
    override fun compare(o1: GroupingKeys.MostRecentKey?, o2: GroupingKeys.MostRecentKey?): Int {
        if (o1 == null || o2 == null) {
            return 0
        }
        return o1.instant.compareTo(o2.instant)
    }
}

object ItemSorter {
    private val monthlyFormatter = DateTimeFormatter.ofPattern("LLLL yyyy")

    private fun List<ItemUiModel>.groupByFirstChar(): Map<GroupingKeys.AlphabeticalKey, List<ItemUiModel>> =
        groupBy { it.name.first().takeIf { char -> char.isLetter() }?.uppercaseChar() ?: '#' }
            .mapKeys { GroupingKeys.AlphabeticalKey(it.key) }

    fun List<ItemUiModel>.sortByTitleAsc(): Map<GroupingKeys, List<ItemUiModel>> =
        groupByFirstChar()
            .map { entry -> entry.key to entry.value.sortedBy { it.name.lowercase() } }
            .toMap()
            .toSortedMap(AlphabeticalKeyComparator)
            .mapKeys { it.key as GroupingKeys }

    fun List<ItemUiModel>.sortByTitleDesc(): Map<GroupingKeys, List<ItemUiModel>> =
        groupByFirstChar()
            .map { entry -> entry.key to entry.value.sortedByDescending { it.name.lowercase() } }
            .toMap()
            .toSortedMap(AlphabeticalKeyComparator.reversed())
            .mapKeys { it.key as GroupingKeys }

    private fun List<ItemUiModel>.groupByMonthAndYear() =
        groupBy {
            monthlyFormatter.format(
                it.createTime.toLocalDateTime(TimeZone.UTC).toJavaLocalDateTime()
            )
        }.mapKeys { GroupingKeys.MonthlyKey(it.key, it.value.first().createTime) }

    fun List<ItemUiModel>.sortByCreationAsc(): Map<GroupingKeys, List<ItemUiModel>> =
        groupByMonthAndYear()
            .map { entry -> entry.key to entry.value.sortedBy { it.createTime } }
            .toMap()
            .toSortedMap(MonthlyKeyComparator)
            .mapKeys { it.key as GroupingKeys }

    fun List<ItemUiModel>.sortByCreationDesc(): Map<GroupingKeys, List<ItemUiModel>> =
        groupByMonthAndYear()
            .map { entry -> entry.key to entry.value.sortedByDescending { it.createTime } }
            .toMap()
            .toSortedMap(MonthlyKeyComparator.reversed())
            .mapKeys { it.key as GroupingKeys }

    fun List<ItemUiModel>.sortByMostRecent(now: Instant): Map<GroupingKeys, List<ItemUiModel>> =
        groupBy { item ->
            DateFormatUtils.getFormat(
                now = now,
                toFormat = recentDate(item.modificationTime, item.lastAutofillTime),
                acceptedFormats = listOf(
                    Today,
                    Yesterday,
                    ThisWeek,
                    LastTwoWeeks,
                    Last30Days,
                    Last60Days,
                    Last90Days,
                    LastYear,
                    MoreThan1Year
                )
            )
        }
            .mapKeys { entry ->
                PassLogger.d("VicLog", entry.key.toString())
                GroupingKeys.MostRecentKey(
                    entry.key,
                    entry.value.first().let { recentDate(it.modificationTime, it.lastAutofillTime) }
                )
            }
            .map { entry ->
                entry.key to entry.value.sortedByDescending {
                    recentDate(it.modificationTime, it.lastAutofillTime)
                }
            }
            .toMap()
            .toSortedMap(MostRecentKeyComparator.reversed())
            .mapKeys { it.key as GroupingKeys }

    private fun recentDate(modificationTime: Instant, lastAutofillTime: Instant?): Instant =
        lastAutofillTime?.let { maxOf(it, modificationTime) } ?: modificationTime
}
