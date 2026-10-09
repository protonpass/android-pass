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

package proton.android.pass.commonrust.impl

import proton.android.pass.crypto.fakes.context.FakeEncryptionContext
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.ItemContents
import proton.android.pass.types.AllowedAndroidApp
import proton.android.pass.types.AndroidSpecific
import proton.android.pass.types.ItemContent
import proton.android.pass.types.ItemData
import proton.android.pass.types.NoteItem
import proton.android.pass.types.PlatformSpecific

internal object ItemTestData {

    val encryptionContext = FakeEncryptionContext

    fun concealed(value: String): HiddenState = HiddenState.Concealed(encryptionContext.encrypt(value))

    fun libraryNote(
        platformSpecific: PlatformSpecific? = null,
        customIcon: ByteArray? = null,
        itemUuid: String = "uuid"
    ): ItemData = ItemData(
        title = "Test Item",
        note = "",
        itemUuid = itemUuid,
        content = ItemContent.Note(NoteItem()),
        extraFields = emptyList(),
        platformSpecific = platformSpecific,
        customIcon = customIcon
    )

    fun libraryNoteWithPackage(packageName: String?, hashes: List<String> = emptyList()): ItemData {
        val allowedApps = packageName
            ?.let { listOf(AllowedAndroidApp(packageName = it, hashes = hashes, appName = "")) }
            .orEmpty()
        return libraryNote(platformSpecific = PlatformSpecific(android = AndroidSpecific(allowedApps = allowedApps)))
    }

    fun domainNote(title: String = "Updated title"): ItemContents.Note = ItemContents.Note(
        title = title,
        note = "note",
        customFields = emptyList()
    )
}
