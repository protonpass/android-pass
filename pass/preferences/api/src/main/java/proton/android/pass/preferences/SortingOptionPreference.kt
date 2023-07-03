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

package proton.android.pass.preferences

const val SORTING_MOST_RECENT = 1
const val SORTING_TITLE_AZ = 2
const val SORTING_TITLE_ZA = 3
const val SORTING_OLDEST_NEWEST = 4
const val SORTING_NEWEST_OLDEST = 5

enum class SortingOptionPreference(private val internalValue: Int) {
    MostRecent(SORTING_MOST_RECENT),
    TitleAZ(SORTING_TITLE_AZ),
    TitleZA(SORTING_TITLE_ZA),
    OldestNewest(SORTING_OLDEST_NEWEST),
    NewestOldest(SORTING_NEWEST_OLDEST);

    fun value() = internalValue

    companion object {
        fun fromValue(value: Int): SortingOptionPreference {
            return when (value) {
                SORTING_MOST_RECENT -> MostRecent
                SORTING_TITLE_AZ -> TitleAZ
                SORTING_TITLE_ZA -> TitleZA
                SORTING_OLDEST_NEWEST -> OldestNewest
                SORTING_NEWEST_OLDEST -> NewestOldest
                else -> MostRecent
            }
        }
    }
}
