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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import proton.android.pass.data.api.repositories.DraftAttachmentRepository
import proton.android.pass.domain.attachments.DraftAttachment
import java.net.URI
import javax.inject.Inject

class DraftAttachmentRepositoryImpl @Inject constructor() : DraftAttachmentRepository {

    private val draftAttachmentsStateFlow = MutableStateFlow<Map<URI, DraftAttachment>>(emptyMap())

    override fun add(attachment: DraftAttachment) {
        val uri = attachment.metadata.uri
        draftAttachmentsStateFlow.update { currentMap ->
            if (!currentMap.containsKey(uri)) {
                currentMap + (uri to attachment)
            } else {
                currentMap
            }
        }
    }

    override fun update(attachment: DraftAttachment) {
        draftAttachmentsStateFlow.update { currentMap ->
            if (currentMap.containsKey(attachment.metadata.uri)) {
                currentMap + (attachment.metadata.uri to attachment)
            } else {
                currentMap
            }
        }
    }

    override fun get(uri: URI): DraftAttachment = draftAttachmentsStateFlow.value[uri]
        ?: throw NoSuchElementException("No draft attachment found for URI: $uri")

    override fun observeAll(): Flow<List<DraftAttachment>> = draftAttachmentsStateFlow.map { it.values.toList() }

    override fun getAll(): List<DraftAttachment> = draftAttachmentsStateFlow.value.values.toList()

    override fun observeNew(): Flow<DraftAttachment> {
        val seenUris = mutableSetOf<URI>()
        return draftAttachmentsStateFlow
            .map { currentMap ->
                currentMap.filterKeys { it !in seenUris }
            }
            .onEach { newEntries ->
                seenUris.addAll(newEntries.keys)
            }
            .flatMapConcat { newEntries ->
                flow {
                    for (state in newEntries.values) {
                        emit(state)
                    }
                }
            }
    }

    override fun remove(uri: URI): Boolean {
        var removedSuccessfully = false
        draftAttachmentsStateFlow.update { currentMap ->
            if (uri in currentMap) {
                removedSuccessfully = true
                currentMap - uri
            } else {
                currentMap
            }
        }
        return removedSuccessfully
    }

    override fun clearAll(): Boolean {
        var clearedSuccessfully = false
        draftAttachmentsStateFlow.update { currentMap ->
            if (currentMap.isNotEmpty()) {
                clearedSuccessfully = true
                emptyMap()
            } else {
                currentMap
            }
        }
        return clearedSuccessfully
    }

    override fun contains(uri: URI): Flow<Boolean> =
        draftAttachmentsStateFlow.map { it.containsKey(uri) }.distinctUntilChanged()
}
