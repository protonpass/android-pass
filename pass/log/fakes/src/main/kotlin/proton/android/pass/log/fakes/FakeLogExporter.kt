/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.log.fakes

import me.proton.core.domain.entity.UserId
import proton.android.pass.log.api.LogExporter
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeLogExporter @Inject constructor() : LogExporter {

    override suspend fun export(destination: File, userId: UserId?) {
        destination.writeText(DEFAULT_CONTENT)
    }

    private companion object {
        private const val DEFAULT_CONTENT = "fake logs"
    }
}
