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

package proton.android.pass.data.impl.repositories

import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.update
import proton.android.pass.data.api.repositories.DraftAttachmentRepository
import java.net.URI
import javax.inject.Inject

class DraftAttachmentRepositoryImpl @Inject constructor() : DraftAttachmentRepository {
    private val uriSetFlow: MutableStateFlow<PersistentSet<URI>> = MutableStateFlow(persistentSetOf())

    override fun add(uri: URI) {
        uriSetFlow.update { currentSet ->
            currentSet.add(uri).toPersistentSet()
        }
    }

    override fun observeAll(): StateFlow<Set<URI>> = uriSetFlow

    override fun observeNew(): Flow<Set<URI>> = uriSetFlow
        .scan(emptySet<URI>() to emptySet<URI>()) { (previouslySeen, _), currentSet ->
            val newUris = currentSet - previouslySeen
            currentSet to newUris
        }
        .map { (_, newUris) -> newUris }
        .filter(Set<URI>::isNotEmpty)

    override fun remove(uri: URI): Boolean {
        var removedSuccessfully = false
        uriSetFlow.update { currentSet ->
            if (uri in currentSet) {
                removedSuccessfully = true
                currentSet.remove(uri).toPersistentSet()
            } else {
                currentSet
            }
        }
        return removedSuccessfully
    }

    override fun clear(): Boolean {
        var clearedSuccessfully = false
        uriSetFlow.update { currentSet ->
            if (currentSet.isNotEmpty()) {
                clearedSuccessfully = true
                persistentSetOf()
            } else {
                currentSet
            }
        }
        return clearedSuccessfully
    }

    override fun contains(uri: URI): Flow<Boolean> = uriSetFlow
        .map { it.contains(uri) }
        .distinctUntilChanged()
}
