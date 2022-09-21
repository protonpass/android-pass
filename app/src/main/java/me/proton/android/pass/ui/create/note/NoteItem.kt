package me.proton.android.pass.ui.create.note

import me.proton.core.pass.domain.ItemContents

data class NoteItem(
    val title: String,
    val note: String
) {
    fun validate(): Set<NoteItemValidationErrors> {
        val mutableSet = mutableSetOf<NoteItemValidationErrors>()
        if (title.isBlank()) mutableSet.add(NoteItemValidationErrors.BlankTitle)
        return mutableSet.toSet()
    }

    fun toItemContents(): ItemContents = ItemContents.Note(
        title = title,
        note = note
    )

    companion object {
        val Empty = NoteItem(
            title = "",
            note = ""
        )
    }
}

sealed interface NoteItemValidationErrors {
    object BlankTitle : NoteItemValidationErrors
}
