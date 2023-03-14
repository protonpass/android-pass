package proton.android.pass.featureitemcreate.impl.note

import androidx.compose.runtime.Immutable
import proton.pass.domain.ItemContents

@Immutable
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
