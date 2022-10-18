package me.proton.core.pass.presentation.create.note

import androidx.annotation.StringRes
import me.proton.core.pass.presentation.R

enum class NoteSnackbarMessage(@StringRes val id: Int) {
    EmptyShareIdError(R.string.create_note_empty_share_id),
    InitError(R.string.create_note_init_error),
    ItemCreationError(R.string.create_note_item_creation_error),
    ItemUpdateError(R.string.create_note_item_update_error)
}
