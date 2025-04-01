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

package proton.android.pass.features.attachments.attachmentoptionsondetail.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.commonui.api.toClassHolder
import proton.android.pass.features.attachments.attachmentoptionsondetail.navigation.AttachmentOptionsOnDetailNavigation
import proton.android.pass.features.attachments.attachmentoptionsondetail.presentation.AttachmentOptionsOnDetailEvent
import proton.android.pass.features.attachments.attachmentoptionsondetail.presentation.AttachmentOptionsOnDetailViewModel
import proton.android.pass.log.api.PassLogger

@Composable
fun AttachmentOptionsOnDetailBottomsheet(
    modifier: Modifier = Modifier,
    viewmodel: AttachmentOptionsOnDetailViewModel = hiltViewModel(),
    onNavigate: (AttachmentOptionsOnDetailNavigation) -> Unit
) {
    val context = LocalContext.current
    val state by viewmodel.state.collectAsStateWithLifecycle()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val toUri = result.data?.data.toOption()
        viewmodel.copyFile(context.toClassHolder(), toUri)
    }

    LaunchedEffect(state.event) {
        when (val event = state.event) {
            AttachmentOptionsOnDetailEvent.Close ->
                onNavigate(AttachmentOptionsOnDetailNavigation.CloseBottomsheet)

            is AttachmentOptionsOnDetailEvent.SaveToLocation -> {
                try {
                    launcher.launch(createIntent(event.fileName))
                } catch (e: ActivityNotFoundException) {
                    PassLogger.w(TAG, e)
                }
            }

            AttachmentOptionsOnDetailEvent.Idle -> {}
        }
        viewmodel.onConsumeEvent(state.event)
    }

    AttachmentOptionsOnDetailContent(
        modifier = modifier.bottomSheet(),
        isDownloading = state.isSavingToLocation,
        isSharing = state.isSharing,
        onEvent = {
            when (it) {
                AttachmentOptionsOnDetailUIEvent.SaveToLocation -> viewmodel.saveToLocation()
                AttachmentOptionsOnDetailUIEvent.Share -> viewmodel.share(context.toClassHolder())
            }
        }
    )
}

private const val TAG = "AttachmentOptionsOnDetailBottomsheet"

private fun createIntent(fileTitle: String): Intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
    addCategory(Intent.CATEGORY_OPENABLE)
    type = "*/*"
    putExtra(Intent.EXTRA_TITLE, fileTitle)
    addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
}
