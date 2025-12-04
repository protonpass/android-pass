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

package proton.android.pass.data.impl.util

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore

import proton.android.pass.common.api.safeRunCatching
fun maxParallelAsyncCalls(): Int {
    val availableCores = Runtime.getRuntime().availableProcessors()
    return (availableCores / 2).coerceAtLeast(1)
}

suspend fun <T, R> runConcurrently(
    maxParallelCalls: Int = maxParallelAsyncCalls(),
    items: Iterable<T>,
    block: suspend (T) -> R,
    onSuccess: (T, R) -> Unit = { _, _ -> },
    onFailure: (T, Throwable) -> Unit = { _, _ -> }
): List<Result<R>> {
    val semaphore = Semaphore(maxParallelCalls)
    return coroutineScope {
        items.map { item ->
            async {
                semaphore.acquire()
                val res = safeRunCatching {
                    block(item)
                }.onSuccess { res ->
                    onSuccess(item, res)
                }.onFailure { err ->
                    onFailure(item, err)
                }
                semaphore.release()
                res
            }
        }.awaitAll()
    }
}
