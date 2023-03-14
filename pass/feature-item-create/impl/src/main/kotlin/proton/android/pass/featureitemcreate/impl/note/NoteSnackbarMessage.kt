package proton.android.pass.featureitemcreate.impl.note

import androidx.annotation.StringRes
import proton.android.pass.featureitemcreate.impl.R
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.notifications.api.SnackbarType

enum class NoteSnackbarMessage(
    @StringRes override val id: Int,
    override val type: SnackbarType,
    override val isClipboard: Boolean = false
) : SnackbarMessage {
    EmptyShareIdError(R.string.create_note_empty_share_id, SnackbarType.ERROR),
    InitError(R.string.create_note_init_error, SnackbarType.ERROR),
    ItemCreationError(R.string.create_note_item_creation_error, SnackbarType.ERROR),
    ItemUpdateError(R.string.create_note_item_update_error, SnackbarType.ERROR),
    NoteCreated(R.string.note_created, SnackbarType.SUCCESS),
    NoteUpdated(R.string.changes_saved, SnackbarType.SUCCESS),
    NoteMovedToTrash(R.string.edit_note_snackbar_note_moved_to_trash, SnackbarType.SUCCESS),
    NoteMovedToTrashError(R.string.edit_note_snackbar_note_moved_to_trash_error, SnackbarType.ERROR)
}
