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

package proton.android.pass.commonui.api

import org.junit.Test
import kotlin.test.assertTrue

class FileTypeMapperTest {

    @Test
    fun `no file type is present in another group`() {
        val allExtensions = FileType.entries.map { it.name to it.extensions }

        for (i in allExtensions.indices) {
            for (j in i + 1 until allExtensions.size) {
                val intersection = allExtensions[i].second.intersect(allExtensions[j].second)
                assertTrue(
                    intersection.isEmpty(),
                    "Duplicate file extensions found between ${allExtensions[i].first} and " +
                        "${allExtensions[j].first}: $intersection"
                )
            }
        }
    }

}
