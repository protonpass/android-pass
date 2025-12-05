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

package proton.android.pass.data.fakes.repositories

import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.android.pass.data.api.repositories.DraftRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeDraftRepository @Inject constructor() : DraftRepository {
    private val storeFlow = MutableStateFlow(persistentMapOf<String, Any>())

    override fun save(key: String, value: Any) {
        storeFlow.update {
            it.toMutableMap().apply {
                set(key, value)
            }.toPersistentMap()
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> get(key: String): Flow<Option<T>> = storeFlow
        .map {
            val value = it[key] as? T
            value.toOption()
        }
        .distinctUntilChanged()

    @Suppress("UNCHECKED_CAST")
    override fun <T> delete(key: String): Option<T> {
        var res: Option<T> = None
        storeFlow.update {
            it.toMutableMap().apply {
                val removed = remove(key) as? T
                res = removed.toOption()
            }.toPersistentMap()
        }
        return res
    }
}


