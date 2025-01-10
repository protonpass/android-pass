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

package proton.android.pass.common.api

import java.util.Locale
import kotlin.math.ln
import kotlin.math.pow

object FileSizeUtil {
    fun toHumanReadableSize(sizeInBytes: Long): String {
        if (sizeInBytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = minOf((ln(sizeInBytes.toDouble()) / ln(1024.0)).toInt(), units.size - 1)
        return String.format(
            Locale.getDefault(),
            "%.1f %s",
            sizeInBytes.toDouble() / 1024.0.pow(digitGroups.toDouble()),
            units[digitGroups]
        )
    }
}
