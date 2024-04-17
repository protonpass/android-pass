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

package proton.android.pass.features.security.center.breachdetail.ui

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {
    const val SECURITY_CENTER_DATE_PATTERN = "MMM dd, yyyy"

    private val isoDateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME
    private val customDateFormatter =
        DateTimeFormatter.ofPattern(SECURITY_CENTER_DATE_PATTERN, Locale.getDefault())

    fun formatDate(date: LocalDate): String = DateTimeFormatter.ofPattern(
        SECURITY_CENTER_DATE_PATTERN,
        Locale.getDefault()
    ).format(date.toJavaLocalDate())

    fun formatDate(isoDateString: String): Result<String> = runCatching {
        val date = isoDateTimeFormatter.parse(isoDateString)
        customDateFormatter.format(date)
    }
}
