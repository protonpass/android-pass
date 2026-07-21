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

package proton.android.pass.data.impl.fakes

import proton.android.pass.data.impl.remote.PrefixQueryResult
import proton.android.pass.data.impl.remote.RemoteCompromisedPasswordDataSource

class FakeRemoteCompromisedPasswordDataSource : RemoteCompromisedPasswordDataSource {

    var lastChangeValue: Long? = 0L
    var lastChangeCallCount: Int = 0

    data class SuffixCall(val prefix: String, val etag: String?)

    val suffixCalls: MutableList<SuffixCall> = mutableListOf()

    var suffixResultByKey: MutableMap<Pair<String, String?>, PrefixQueryResult> = mutableMapOf()
    var defaultSuffixResult: PrefixQueryResult = PrefixQueryResult.Ok(etag = null, suffixes = emptySet())

    override suspend fun getLastChange(): Long? {
        lastChangeCallCount++
        return lastChangeValue
    }

    override suspend fun getCompromisedSuffixes(prefix: String, etag: String?): PrefixQueryResult {
        suffixCalls += SuffixCall(prefix, etag)
        return suffixResultByKey[prefix to etag] ?: defaultSuffixResult
    }
}
