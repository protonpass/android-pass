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

package proton.android.pass.featureitemcreate.impl.common.attachments

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.Attachment
import java.net.URI

interface AttachmentsHandler {
    val isUploadingAttachment: Flow<Set<URI>>
    val attachmentsFlow: Flow<AttachmentsState>

    fun openDraftAttachment(
        context: Context,
        uri: URI,
        mimetype: String
    )
    fun uploadNewAttachment(uri: URI, scope: CoroutineScope)
    fun onClearAttachments()
    fun observeNewAttachments(scope: CoroutineScope, onNewAttachment: (Set<URI>) -> Unit)
    fun openAttachment(
        context: Context,
        shareId: ShareId,
        itemId: ItemId,
        attachment: Attachment,
        scope: CoroutineScope
    )
}
