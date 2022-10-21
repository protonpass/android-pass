package me.proton.core.pass.presentation.create.note

import androidx.annotation.StringRes
import me.proton.android.pass.notifications.api.SnackbarMessage
import me.proton.android.pass.notifications.api.SnackbarType
import me.proton.core.pass.presentation.R

enum class NoteSnackbarMessage(
    @StringRes override val id: Int,
    override val type: SnackbarType
) : SnackbarMessage {
    EmptyShareIdError(R.string.create_note_empty_share_id, SnackbarType.ERROR),
    InitError(R.string.create_note_init_error, SnackbarType.ERROR),
    ItemCreationError(R.string.create_note_item_creation_error, SnackbarType.ERROR),
    ItemUpdateError(R.string.create_note_item_update_error, SnackbarType.ERROR),
    NoteCreated(R.string.note_created, SnackbarType.SUCCESS),
    NoteUpdated(R.string.changes_saved, SnackbarType.SUCCESS)
}
