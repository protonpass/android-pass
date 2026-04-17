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

package proton.android.pass.data.fakes.url

import proton.android.pass.data.api.url.HostInfo
import proton.android.pass.data.api.url.HostParser
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeHostParser @Inject constructor() : HostParser {

    private var result: Result<HostInfo> = Result.failure(NotImplementedError("FakeHostParser not configured"))

    fun setResult(value: Result<HostInfo>) {
        result = value
    }

    override fun parse(url: String): Result<HostInfo> = result
}
