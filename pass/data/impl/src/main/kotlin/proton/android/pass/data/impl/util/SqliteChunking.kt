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

package proton.android.pass.data.impl.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf

// SQLITE_MAX_VARIABLE_NUMBER is 999 on the SQLite versions bundled before API 31.
// otherBoundArgs covers the scalar parameters the query binds alongside the IN list.
private const val SQLITE_MAX_VARIABLES = 999

internal fun <T> List<T>.sqliteChunks(otherBoundArgs: Int): List<List<T>> = when {
    isEmpty() -> emptyList()
    else -> chunked(SQLITE_MAX_VARIABLES - otherBoundArgs)
}

internal fun <T, R> List<T>.flowPerSqliteChunk(otherBoundArgs: Int, query: (List<T>) -> Flow<List<R>>): Flow<List<R>> {
    val chunks = sqliteChunks(otherBoundArgs)
    return when {
        chunks.isEmpty() -> flowOf(emptyList())
        chunks.size == 1 -> query(chunks.first())
        else -> combine(chunks.map(query)) { results -> results.flatMap { it } }
    }
}

internal fun <T> List<T>.countPerSqliteChunk(otherBoundArgs: Int, query: (List<T>) -> Flow<Int>): Flow<Int> {
    val chunks = sqliteChunks(otherBoundArgs)
    return when {
        chunks.isEmpty() -> flowOf(0)
        chunks.size == 1 -> query(chunks.first())
        else -> combine(chunks.map(query)) { counts -> counts.sum() }
    }
}
