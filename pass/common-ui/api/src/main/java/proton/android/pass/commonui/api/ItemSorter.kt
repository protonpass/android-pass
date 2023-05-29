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
import java.time.format.DateTimeFormatter

data class GroupedItemList(
    val key: GroupingKeys,
    val items: List<ItemUiModel>
)

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
        groupBy { it.contents.title.firstOrNull()?.takeIf { char -> char.isLetter() }?.uppercaseChar() ?: '#' }
            .mapKeys { GroupingKeys.AlphabeticalKey(it.key) }

    fun List<ItemUiModel>.sortByTitleAsc(): List<GroupedItemList> =
        groupByFirstChar()
            .toSortedMap(AlphabeticalKeyComparator)
            .map { entry ->
                GroupedItemList(
                    entry.key,
                    entry.value.sortedBy { it.contents.title.lowercase() }
                )
            }

    fun List<ItemUiModel>.sortByTitleDesc(): List<GroupedItemList> =
        groupByFirstChar()
            .toSortedMap(AlphabeticalKeyComparator.reversed())
            .map { entry ->
                GroupedItemList(
                    entry.key,
                    entry.value.sortedByDescending { it.contents.title.lowercase() }
                )
            }

    private fun List<ItemUiModel>.groupByMonthAndYear() =
        groupBy {
            monthlyFormatter.format(
                it.createTime.toLocalDateTime(TimeZone.UTC).toJavaLocalDateTime()
            )
        }.mapKeys { GroupingKeys.MonthlyKey(it.key, it.value.first().createTime) }

    fun List<ItemUiModel>.sortByCreationAsc(): List<GroupedItemList> =
        groupByMonthAndYear()
            .toSortedMap(MonthlyKeyComparator)
            .map { entry -> GroupedItemList(entry.key, entry.value.sortedBy { it.createTime }) }

    fun List<ItemUiModel>.sortByCreationDesc(): List<GroupedItemList> =
        groupByMonthAndYear()
            .toSortedMap(MonthlyKeyComparator.reversed())
            .map { entry ->
                GroupedItemList(
                    entry.key,
                    entry.value.sortedByDescending { it.createTime }
                )
            }

    fun List<ItemUiModel>.sortByMostRecent(now: Instant): List<GroupedItemList> =
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
                GroupingKeys.MostRecentKey(
                    entry.key,
                    entry.value.first().let { recentDate(it.modificationTime, it.lastAutofillTime) }
                )
            }
            .toSortedMap(MostRecentKeyComparator.reversed())
            .map { entry ->
                GroupedItemList(
                    entry.key,
                    entry.value.sortedByDescending {
                        recentDate(it.modificationTime, it.lastAutofillTime)
                    }
                )
            }

    fun List<ItemUiModel>.sortSuggestionsByMostRecent(): List<ItemUiModel> =
        sortedByDescending { recentDate(it.modificationTime, it.lastAutofillTime) }

    private fun recentDate(modificationTime: Instant, lastAutofillTime: Instant?): Instant =
        lastAutofillTime?.let { maxOf(it, modificationTime) } ?: modificationTime
}
