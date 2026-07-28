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

package proton.android.pass.domain.attachments

// Persisted indices. These are the single source of truth: the DB TypeConverter stores `index`
// and DAO queries that need raw literals (Room can't bind params everywhere) reference these
// constants. Changing a value is a DB-breaking change requiring a migration.
// They live outside the enum because entry initializers cannot read its companion object.
object AttachmentDownloadStatusIndex {
    const val IDLE = 0
    const val PENDING = 1
    const val DOWNLOADING = 2
    const val DOWNLOADED = 3
    const val FAILED = 4
    const val PAUSED = 5
}

enum class AttachmentDownloadStatus(val index: Int) {
    Idle(AttachmentDownloadStatusIndex.IDLE),
    Pending(AttachmentDownloadStatusIndex.PENDING),
    Downloading(AttachmentDownloadStatusIndex.DOWNLOADING),
    Downloaded(AttachmentDownloadStatusIndex.DOWNLOADED),
    Failed(AttachmentDownloadStatusIndex.FAILED),
    Paused(AttachmentDownloadStatusIndex.PAUSED);

    companion object {
        private val indexMap = entries.associateBy { it.index }
        fun fromIndex(index: Int): AttachmentDownloadStatus = indexMap[index] ?: Idle
    }
}
